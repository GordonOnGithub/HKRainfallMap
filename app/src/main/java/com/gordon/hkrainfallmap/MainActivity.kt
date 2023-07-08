package com.gordon.hkrainfallmap

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.gordon.hkrainfallmap.ui.theme.HKRainfallMapTheme
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    val mapViewModel : MapViewModel = MapViewModel()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            mapViewModel.updateCurrentLocation(LatLng(result.lastLocation.latitude, result.lastLocation.longitude))
            mapViewModel.locationEnabled.value = true
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            mapViewModel.locationEnabled.value = availability.isLocationAvailable

        }
    }

    @SuppressLint("MissingPermission")
    val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when {
            granted -> {
                mapViewModel.isLocationAccessGranted.postValue(true)
                val locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            }
            !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                mapViewModel.isLocationAccessGranted.postValue(false)
            }
            else -> {
                mapViewModel.isLocationAccessGranted.postValue(false)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val lastRainfallDataUpdateTimestamp = mapViewModel.lastRainfallDataUpdateTimestamp.value ?: return
        if (Date().time - lastRainfallDataUpdateTimestamp.time > MapConstants.rainfallDataRefreshInterval) {
            mapViewModel.updateRainfallDataSet()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

                            rainfallRangeRow(modifier = Modifier
                                .weight(1f, fill = true)
                                .fillMaxWidth())

                            rainfallMap(modifier = Modifier.weight(8f, fill = true))
                            predictionTimeSwitch( modifier = Modifier
                                .weight(1f, fill = true)
                                .fillMaxWidth())


                            val lastUpdateTimestamp : Date? by mapViewModel.lastRainfallDataUpdateTimestamp.observeAsState(
                                null
                            )
                            Row(modifier = Modifier.height(30.dp)){
                                if (lastUpdateTimestamp != null) {
                                    Text(text = "Last Update: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastUpdateTimestamp)}", modifier = Modifier.padding(5.dp))
                                }
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.width(160.dp)) {

                    Row(modifier = Modifier.background(MapConstants.blueTileColor)){
                        Text("0.5mm - 2.5mm", fontSize = 20.sp, modifier = Modifier.fillMaxWidth().padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.greenTileColor)){
                        Text("2.5mm - 5mm", fontSize = 20.sp, modifier = Modifier.fillMaxWidth().padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.yellowTileColor)){
                        Text("5mm - 10mm", fontSize = 20.sp, modifier = Modifier.fillMaxWidth().padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.orangeTileColor)){
                        Text("10mm - 20mm", fontSize = 20.sp, modifier = Modifier.fillMaxWidth().padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.redTileColor)){
                        Text("20mm+", fontSize = 20.sp, modifier = Modifier.fillMaxWidth().padding(5.dp))
                    }
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

        val locationEnabled : Boolean by mapViewModel.locationEnabled.observeAsState(initial = false)

        Box(modifier = modifier){
            GoogleMap(
                modifier = modifier,
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(rotationGesturesEnabled = false, tiltGesturesEnabled = false, scrollGesturesEnabled = !isFetchingData),
                properties = MapProperties(
                    isMyLocationEnabled = isLocationAccessGranted && locationEnabled,
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
            }, modifier = Modifier.padding(10.dp)) {
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

        val isFetchingData : Boolean by mapViewModel.isFetchingData.observeAsState(
            false
        )

        val buttonTitle = selectedDateTimeString.getNiceFormattedTimeStringFromDatetimeString()

        Row(horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier) {

        if(!isFetchingData && 
            !selectedDateTimeString.isNullOrEmpty() &&
        !buttonTitle.isNullOrEmpty()) {

                Box () {
                    Button(onClick = {
                        mapViewModel.showTimeMenu.postValue(true)
                    }) {

                        Text("🕑 $buttonTitle", fontSize = 24.sp)
                    }

                    DropdownMenu(expanded = showTimeMenu, onDismissRequest = {
                        mapViewModel.showTimeMenu.postValue(false)
                    }) {
                        for (dateTime in mapViewModel.sortedDatetimeString.asReversed()) {
                            DropdownMenuItem(onClick = {
                                mapViewModel.updateDisplayedDataMap(dateTime)
                                mapViewModel.showTimeMenu.postValue(false)
                            }) {
                                Text(dateTime.getNiceFormattedTimeStringFromDatetimeString() ?: "", fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun rainfallRangeRow(modifier: Modifier) {
        val currentLocationRainfallRange : RainfallRange? by mapViewModel.currentLocationRainFallRange.observeAsState(
            null
        )

        val isFetchingData : Boolean by mapViewModel.isFetchingData.observeAsState(
            false
        )

        val location : LatLng? by mapViewModel.location.observeAsState(
            null
        )

        val isLocationAccessGranted : Boolean by mapViewModel.isLocationAccessGranted.observeAsState(
            false
        )

        val locationEnabled : Boolean by mapViewModel.locationEnabled.observeAsState(initial = false)

        val lastUpdateTimestamp : Date? by mapViewModel.lastRainfallDataUpdateTimestamp.observeAsState(
            null
        )

        val displayedDataSet : LatLngRainfallDataMap by mapViewModel.displayedRainfallDataMap.observeAsState(
            mutableMapOf()
        )
        Column(modifier = Modifier
            .padding(10.0.dp)
            .height(60.dp)) {
            if(isFetchingData) {
                Text(text = "Fetching data...", fontSize = 20.sp)
            } else if ( displayedDataSet.isEmpty() && lastUpdateTimestamp != null){
                Row {
                    Text(text = "Failed to fetch data", fontSize = 20.sp, modifier = Modifier
                        .weight(4f, fill = true))
                    Button(onClick = {
                        mapViewModel.updateRainfallDataSet()
                    }, modifier = Modifier.weight(1f)) {
                        Text(text = "Retry")
                    }
                }
            } else if (!isLocationAccessGranted) {
                Text(text = "No permission to get location.", fontSize = 20.sp)
            } else if (location == null || !locationEnabled) {
                Text(text = "Current location is not available.",  fontSize = 20.sp)
            } else if(currentLocationRainfallRange != null) {
                Row {
                    Column( modifier = Modifier.weight(3f, fill = true)) {
                        Text(text = "Your location's rainfall in next 2 hours:",  maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${currentLocationRainfallRange!!.first}mm - ${currentLocationRainfallRange!!.second}mm ",
                            style = TextStyle(background = currentLocationRainfallRange?.second?.getRainfallTileColor() ?: Color.Transparent),
                            fontSize = 20.sp)
                    }
                    Button(onClick = {
                        mapViewModel.updateRainfallDataSet()
                    }, modifier = Modifier.weight(1f)) {
                        Text(text = "Refresh")
                    }
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