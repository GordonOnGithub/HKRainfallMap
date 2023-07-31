package com.gordon.hkrainfallmap

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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

        mapViewModel.handleAppResume()
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

                            rainfallInfoRow()


                            rainfallMap(modifier = Modifier.weight(15f, fill = true))

                            WeatherWarningInfoRow()

                            ForecastTimeSwitch( modifier = Modifier
                                .weight(3f, fill = true)
                                .fillMaxWidth())


                            val lastUpdateTimestamp : Date? by mapViewModel.lastRainfallDataUpdateTimestamp.observeAsState(
                                null
                            )

                            val displayedDataSet : LatLngRainfallDataMap by mapViewModel.displayedRainfallDataMap.observeAsState(
                                mutableMapOf()
                            )

                            Row(modifier = Modifier.height(30.dp)){
                                if (lastUpdateTimestamp != null) {
                                    Text(text = "${getString(R.string.map_view_last_update_timestamp)}: ${displayedDataSet.values.first().updateTimeString.getNiceFormattedDateTimeStringFromDatetimeString()}", modifier = Modifier.padding(5.dp))
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

                        val showWeatherWarningSummary : Boolean by mapViewModel.showWeatherWarningSummary.observeAsState(
                            false
                        )

                        if(showWeatherWarningSummary) {
                            weatherWarningSummaryDialog()
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
                Text(getString(R.string.map_view_map_legend_detail_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.width(180.dp)) {

                    Row(modifier = Modifier.background(MapConstants.blueTileColor())){
                        Text("0.5${getString(R.string.map_view_mm)} - 2.5${getString(R.string.map_view_mm)}", fontSize = 20.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.greenTileColor())){
                        Text("2.5${getString(R.string.map_view_mm)} - 5${getString(R.string.map_view_mm)}", fontSize = 20.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.yellowTileColor())){
                        Text("5${getString(R.string.map_view_mm)} - 10${getString(R.string.map_view_mm)}", fontSize = 20.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.orangeTileColor())){
                        Text("10${getString(R.string.map_view_mm)} - 20${getString(R.string.map_view_mm)}", fontSize = 20.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.redTileColor())){
                        Text("20${getString(R.string.map_view_mm)}+", fontSize = 20.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp))
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
                    mapViewModel.startRainfallDataUpdateTicker()
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
                Text(text = "ℹ️ ${getString(R.string.map_view_map_legend)}")
            }
        }

    }


    @Composable
    fun ForecastTimeSwitch(modifier: Modifier){

        val selectedDateTimeString : String by mapViewModel.selectedDateTimeString.observeAsState(
            ""
        )

        val showTimeMenu : Boolean by mapViewModel.showTimeMenu.observeAsState(
            false
        )

        val isFetchingData : Boolean by mapViewModel.isFetchingData.observeAsState(
            false
        )

        val autoplay : Boolean by mapViewModel.autoplayRainfallData.observeAsState(
            false
        )

        val buttonTitle = selectedDateTimeString.getNiceFormattedTimeStringFromDatetimeString()

        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {

            if (!isFetchingData &&
                !selectedDateTimeString.isNullOrEmpty() &&
                !buttonTitle.isNullOrEmpty()
            ) {

            Row ( horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                 modifier = Modifier.width(100.dp) ){

                for (dateTime in mapViewModel.sortedDatetimeString) {

                    val color = if (dateTime == selectedDateTimeString) Color.Blue else Color.LightGray

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                            .padding(5.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {


                Box() {

                    Button(
                        onClick = {
                            if (autoplay) {
                                return@Button
                            }

                            mapViewModel.showTimeMenu.postValue(true)
                        },
                        colors = ButtonDefaults.buttonColors(
                            disabledBackgroundColor = Color.Transparent,
                            disabledContentColor = Color.Black
                        ),
                        enabled = !autoplay
                    ) {
                        Text("🕑 $buttonTitle", fontSize = 24.sp)
                    }


                    DropdownMenu(expanded = !autoplay && showTimeMenu, onDismissRequest = {
                        mapViewModel.showTimeMenu.postValue(false)
                    }) {
                        for (dateTime in mapViewModel.sortedDatetimeString.asReversed()) {
                            DropdownMenuItem(onClick = {
                                mapViewModel.updateDisplayedDataMap(dateTime)
                                mapViewModel.showTimeMenu.postValue(false)
                            }) {
                                Text(
                                    dateTime.getNiceFormattedTimeStringFromDatetimeString()
                                        ?: "",
                                    fontSize = 24.sp,
                                    fontWeight = if(dateTime == selectedDateTimeString) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(text = (if (autoplay) "⏹️" else "▶️"),
                    fontSize = 38.sp,
                    modifier = Modifier
                        .clickable {
                            mapViewModel.toggleRainfallDataAutoplay(!autoplay)
                        }
                        .padding(10.dp))

            }
            }
        }
    }

    @Composable
    fun rainfallInfoRow() {
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
                Text(text = getString(R.string.map_view_fetching_data), fontSize = 20.sp)
            } else if ( displayedDataSet.isEmpty() && lastUpdateTimestamp != null){
                Row(horizontalArrangement = Arrangement.End) {
                    Text(text = getString(R.string.map_view_fetch_data_failed), fontSize = 20.sp)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        mapViewModel.updateRainfallDataSet()
                        mapViewModel.updateWeatherWarningDataSet()
                    }, modifier = Modifier.width(100.dp)) {
                        Text(text = getString(R.string.map_view_retry_fetching))
                    }
                }
            } else if (!isLocationAccessGranted) {
                Text(text = getString(R.string.map_view_location_permission_denied), fontSize = 20.sp)
            } else if (location == null || !locationEnabled) {
                Text(text = getString(R.string.map_view_location_unavailable),  fontSize = 20.sp)
            } else if(currentLocationRainfallRange != null) {
                Row {
                    Column (modifier = Modifier.padding(5.dp)){
                        Text(text = getString(R.string.map_view_rainfall_in_next_2_hours),  maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("${currentLocationRainfallRange!!.first}${getString(R.string.map_view_mm)} - ${currentLocationRainfallRange!!.second}${getString(R.string.map_view_mm)} ",
                            style = TextStyle(background = currentLocationRainfallRange?.second?.getRainfallTileColor() ?: Color.Transparent),
                            fontSize = 20.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        mapViewModel.updateRainfallDataSet()
                        mapViewModel.updateWeatherWarningDataSet()
                    }, modifier = Modifier.width(120.dp)) {
                        Text(text = getString(R.string.map_view_refresh_data))
                    }
                }

            }
        }
    }

    @Composable
    fun WeatherWarningInfoRow(){

        val weatherWarningDataList : List<WeatherWarningData> by mapViewModel.weatherWarningDataList.observeAsState(
            initial = listOf()
        )
        if (!weatherWarningDataList.isNullOrEmpty()) {

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier
                .height(60.dp)
                .background(MapConstants.yellowTileColor())
                .fillMaxWidth()
                ){

                Text(text = weatherWarningDataList.summary() ?: "",
                    modifier = Modifier.padding(10.dp).clickable {
                        mapViewModel.showWeatherWarningSummary.postValue(true)
                    })

            }
        }
    }

    @Composable
    fun weatherWarningSummaryDialog(){

        val weatherWarningDataList : List<WeatherWarningData> by mapViewModel.weatherWarningDataList.observeAsState(
            initial = listOf()
        )

        AlertDialog(onDismissRequest = {
            mapViewModel.showWeatherWarningSummary.postValue(false)
        },
            title = {
                Text("Weather Warning(s) in force:")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    for (warning in weatherWarningDataList ) {

                        Row() {
                            Text(
                                warning.description(),
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                            )
                        }
                    }

                }
            },
            confirmButton = {
                Button(onClick = {
                    mapViewModel.showWeatherWarningSummary.postValue(false)
                }) {
                    Text("OK")
                }
            }
        )

    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HKRainfallMapTheme {

    }
}