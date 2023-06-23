package com.gordon.hkrainfallmap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.gordon.hkrainfallmap.ui.theme.HKRainfallMapTheme

import com.google.android.gms.maps.model.CameraPosition

import com.google.maps.android.compose.*

class MainActivity : ComponentActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    val mapViewModel : MapViewModel = MapViewModel()

    val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when {
            granted -> {
                print("granted")
                mapViewModel.isLocationAccessGranted.postValue(true)
            }
            !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                print("do not show again")
            }
            else -> {
                print("denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(getApplicationContext())

        locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        setContent {
            HKRainfallMapTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    Box(contentAlignment = Alignment.Center) {
                        Column(verticalArrangement = Arrangement.Top) {
                            rainfallMap(modifier = Modifier.weight(5f, fill = true))
                            predictionTimeSwitch( modifier = Modifier.weight(1f, fill = true))

                            val isLocationAccessGranted : Boolean by mapViewModel.isLocationAccessGranted.observeAsState(
                                false
                            )
                            
                            if(isLocationAccessGranted){
                                Text(text = "Location access granted")
                            }
                        
                        
                        }

                        val isFetchingData : Boolean by mapViewModel.isFetchingData.observeAsState(
                            false
                        )

                        if (isFetchingData) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun rainfallMap(modifier: Modifier) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(mapViewModel.HKLatLng, 10f)
        }

        val displayedDataSet : LatLngRainfallDataMap by mapViewModel.displayedRainfallDataMap.observeAsState(
            mutableMapOf()
        )

        val isLocationAccessGranted : Boolean by mapViewModel.isLocationAccessGranted.observeAsState(
            false
        )

        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = isLocationAccessGranted,
                latLngBoundsForCameraTarget = mapViewModel.HKBoundary,
                minZoomPreference = mapViewModel.mapMinZoomLevel,
                maxZoomPreference = mapViewModel.mapMaxZoomLevel),
            onMapLoaded = {
                        mapViewModel.updateRainfallDataSet()
                    }
        ) {
            for (data in displayedDataSet.values) {
                val tile =
                    getRainfallTileBitmapDescriptor(data) ?: continue

                GroundOverlay(
                    position = GroundOverlayPosition.create(
                        data.position,
                        2000f,
                        2000f
                    ),
                    image = tile ,
                    transparency = 0.5f
                )
            }
        }
    }

    @Composable
    fun predictionTimeSwitch(modifier: Modifier){

        val selectedDateTimeString : String by mapViewModel.selectedDateTimeString.observeAsState(
            ""
        )

        val isFetchingData : Boolean by mapViewModel.isFetchingData.observeAsState(
            false
        )

        Row(horizontalArrangement = Arrangement.Center, modifier = modifier) {

            for(dateTime in mapViewModel.sortedDatetimeString) {
                Button(
                    onClick = {
                        mapViewModel.updateDisplayedDataMap(dateTime)

                    }, colors = ButtonDefaults.textButtonColors(
                        backgroundColor = if(dateTime == selectedDateTimeString)  Color.Blue else Color.White
                    )
                ) {
                    Text(dateTime.substring(dateTime.length -  4 , dateTime.length ),
                        color =  if(dateTime == selectedDateTimeString)  Color.White else Color.Blue )
                }
            }

            if(!isFetchingData) {
                Button(onClick = {
                    mapViewModel.updateRainfallDataSet()
                }) {
                    Text(text = "Reload")
                }
            }
        }
    }

    var redTile : BitmapDescriptor? = null
    var orangeTile : BitmapDescriptor? = null
    var yellowTile : BitmapDescriptor? = null
    var greenTile: BitmapDescriptor? = null
    var blueTile: BitmapDescriptor? = null

    @Composable
    fun getRainfallTileBitmapDescriptor(data : RainfallData) : BitmapDescriptor? {
        if (redTile == null) {
            redTile = BitmapDescriptorFactory.fromResource(R.drawable.red_tile)
        }

        if(orangeTile == null ) {
            orangeTile = BitmapDescriptorFactory.fromResource(R.drawable.orange_tile)
        }

        if(yellowTile == null) {
            yellowTile = BitmapDescriptorFactory.fromResource(R.drawable.yellow_tile)
        }

        if (greenTile == null) {
            greenTile = BitmapDescriptorFactory.fromResource(R.drawable.green_tile)
        }

        if(blueTile == null) {
            blueTile = BitmapDescriptorFactory.fromResource(R.drawable.blue_tile)
        }

        if (!mapViewModel.HKBoundary.contains(data.position)) { return null }

        if (data.rainfall > 20) {
            return redTile
        }

        if (data.rainfall > 10) {
            return orangeTile
        }

        if (data.rainfall > 5) {
            return yellowTile
        }

        if (data.rainfall > 2.5) {
            return greenTile
        }

        if (data.rainfall > 0.5) {
            return blueTile
        }

        return null
    }

}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HKRainfallMapTheme {

    }
}