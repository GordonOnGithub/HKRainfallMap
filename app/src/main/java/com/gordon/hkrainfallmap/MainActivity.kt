package com.gordon.hkrainfallmap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Paint.Style
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.gordon.hkrainfallmap.ui.theme.HKRainfallMapTheme

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

import com.google.maps.android.compose.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

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
                            rainfallMap(modifier = Modifier.weight(6f, fill = true))
                            predictionTimeSwitch( modifier = Modifier
                                .weight(1f, fill = true)
                                .fillMaxWidth())
                            dataStatusBar(modifier = Modifier
                                .weight(1f, fill = true)
                                .fillMaxWidth())

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

                        val showMapLegend : Boolean by mapViewModel.showMapLegend.observeAsState(
                            false
                        )
                        
                        if (showMapLegend) {
                            mapLegendDialog()
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun mapLegendDialog(){

        AlertDialog(onDismissRequest = {
            mapViewModel.showMapLegend.postValue(false)
        },
            title = {
                Text("Half-hourly Nowcast Accumulated Rainfall (mm)")
            },
            text = {
                Column() {
                    Text("0.5mm - 2.5mm", style = TextStyle(background = MapConstants.blueTileColor))
                    Text("2.5mm - 5mm  ", style = TextStyle(background = MapConstants.greenTileColor))
                    Text("5mm - 10mm   ", style = TextStyle(background = MapConstants.yellowTileColor))
                    Text("10mm - 20mm  ", style = TextStyle(background = MapConstants.orangeTileColor))
                    Text("20mm+        ", style = TextStyle(background = MapConstants.redTileColor))
                }
            },
            confirmButton = {
                Button(onClick = {
                    mapViewModel.showMapLegend.postValue(false)
                }) {
                    Text("OK")
                }
            }
        )

    }

    @Composable
    fun rainfallMap(modifier: Modifier) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(MapConstants.HKLatLng, 10f)
        }

        val displayedDataSet : LatLngRainfallDataMap by mapViewModel.displayedRainfallDataMap.observeAsState(
            mutableMapOf()
        )

        val isLocationAccessGranted : Boolean by mapViewModel.isLocationAccessGranted.observeAsState(
            false
        )

        val isFetchingData : Boolean by mapViewModel.isFetchingData.observeAsState(
            false
        )

        Box(modifier = modifier){
            GoogleMap(
                modifier = modifier,
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(rotationGesturesEnabled = false, tiltGesturesEnabled = false, scrollGesturesEnabled = !isFetchingData),
                properties = MapProperties(
                    isMyLocationEnabled = isLocationAccessGranted,
                    latLngBoundsForCameraTarget = MapConstants.HKBoundary,
                    minZoomPreference = MapConstants.mapMinZoomLevel,
                    maxZoomPreference = MapConstants.mapMaxZoomLevel),
                onMapLoaded = {
                    mapViewModel.updateRainfallDataSet()
                }
            ) {

                Polygon(points = MapConstants.HKBoundaryPolygonPoints, fillColor = Color(0x00000000))

                for (data in displayedDataSet.values) {
                    val color = data.rainfall.getRainfallTileColor() ?: continue

                    Polygon(points = data.tilePolygonPoints(), fillColor = color, strokeWidth = 0f)

                }
            }

            Button(onClick = {
                mapViewModel.showMapLegend.postValue(!mapViewModel.showMapLegend.value!!)
            }) {
                Text(text = "ⓘ Legend")
            }
        }

    }


    @Composable
    fun predictionTimeSwitch(modifier: Modifier){

        val selectedDateTimeString : String by mapViewModel.selectedDateTimeString.observeAsState(
            ""
        )

        val showTimeMenu : Boolean by mapViewModel.showTimeMenu.observeAsState(
            false
        )

        val buttonTitle = selectedDateTimeString.getNiceFormattedTimeStringFromDatetimeString()

        if(!selectedDateTimeString.isNullOrEmpty() &&
        !buttonTitle.isNullOrEmpty()) {
            Row(horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier) {

                Text("Show rainfall nowcast on map at: ")

                Box () {
                    Button(onClick = {
                        mapViewModel.showTimeMenu.postValue(true)
                    }) {

                        Text(buttonTitle, fontSize = 24.sp)
                    }

                    DropdownMenu(expanded = showTimeMenu, onDismissRequest = {
                        mapViewModel.showTimeMenu.postValue(false)
                    }) {
                        for (dateTime in mapViewModel.sortedDatetimeString) {
                            DropdownMenuItem(onClick = {
                                mapViewModel.updateDisplayedDataMap(dateTime)
                                mapViewModel.showTimeMenu.postValue(false)
                            }) {
                                Text(dateTime.getNiceFormattedTimeStringFromDatetimeString() ?: "")
                            }
                        }
                    }
                }
            }


        }
    }
    @Composable
    fun dataStatusBar(modifier: Modifier) {
        val isFetchingData : Boolean by mapViewModel.isFetchingData.observeAsState(
            false
        )

        val lastUpdateTimestamp : Date? by mapViewModel.lastUpdateTimestamp.observeAsState(
            null
        )

        val rainfallDataSet : RainfallDataSet by mapViewModel.rainfallDataSet.observeAsState(
            mutableMapOf()
        )

        Row(modifier = modifier,
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically) {
            if (isFetchingData){
                Text(text = "Fetching data...")
            } else if (lastUpdateTimestamp != null
                  && rainfallDataSet.isNotEmpty()) {
                Text(text = "Last Update: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastUpdateTimestamp)}")
            } else {
                Text(text = "Failed to fetch data")
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

}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HKRainfallMapTheme {

    }
}