package com.gordon.hkrainfallmap

import android.content.Context
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.util.Date

@Composable
fun RainfallNowcastMapScreen(mapViewModel: MapViewModel, context: Context, navigationController : NavHostController){

    @Composable
    fun topAppBar() {

        val mapMode : MapMode by mapViewModel.mapMode.observeAsState(initial = MapMode.RAINFALL)

        TopAppBar(title = { Text(text = mapViewModel.getScreenTitle(mapMode = mapMode)) },
            actions = {
                Button(onClick = {
                    if (mapMode == MapMode.RAINFALL) {
                        mapViewModel.setMapMode(MapMode.WEATHERSTATION)
                    }else if (mapMode == MapMode.WEATHERSTATION) {
                        mapViewModel.setMapMode(MapMode.RAINFALL)
                    }

                }) {
                    Text( if (mapMode == MapMode.RAINFALL) "\uD83C\uDF21️" else "\uD83C\uDF27️", fontSize = 20.sp)
                }

                Button(onClick = {
                    navigationController.navigate(NavigationRoutes.settings)
                }) {
                    Text("⚙️", fontSize = 20.sp)
                }
            }
        )
    }

    @Composable
    fun mapLegendDialog(){

        AlertDialog(onDismissRequest = {
            mapViewModel.showMapLegend.postValue(false)
        },
            title = {
                Text(context.getString(R.string.map_view_map_legend_detail_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.width(180.dp)) {

                    Row(modifier = Modifier.background(MapConstants.blueTileColor())){
                        Text("0.5${context.getString(R.string.map_view_mm)} - 2.5${context.getString(R.string.map_view_mm)}", fontSize = 20.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.greenTileColor())){
                        Text("2.5${context.getString(R.string.map_view_mm)} - 5${context.getString(R.string.map_view_mm)}", fontSize = 20.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.yellowTileColor())){
                        Text("5${context.getString(R.string.map_view_mm)} - 10${context.getString(R.string.map_view_mm)}", fontSize = 20.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.orangeTileColor())){
                        Text("10${context.getString(R.string.map_view_mm)} - 20${context.getString(R.string.map_view_mm)}", fontSize = 20.sp, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp))
                    }
                    Row(modifier = Modifier.background(MapConstants.redTileColor())){
                        Text("20${context.getString(R.string.map_view_mm)}+", fontSize = 20.sp, modifier = Modifier
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

        val mapMode : MapMode by mapViewModel.mapMode.observeAsState(initial = MapMode.RAINFALL)
        
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

                if (mapMode == MapMode.RAINFALL) {
                    Polygon(
                        points = MapConstants.HKBoundaryPolygonPoints,
                        fillColor = Color(0x00000000)
                    )

                    for (data in displayedDataSet.values) {
                        val color = data.rainfall.getRainfallTileColor() ?: continue

                        Polygon(
                            points = data.tilePolygonPoints(),
                            fillColor = color,
                            strokeWidth = 0f
                        )
                    }
                }

                if (mapMode == MapMode.WEATHERSTATION) {
                    val weatherStationDataMap: Map<String, WeatherStationData> by mapViewModel.weatherStationDataMap.observeAsState(
                        initial = mapOf()
                    )

                    for (data in weatherStationDataMap.values) {
                        Marker(
                            state = rememberMarkerState(position = data.position),
                            alpha = 0.8f,
                            title = data.name,
                            snippet = "\uD83C\uDF21️: ${data.temperature}°C",
                        )
                    }
                }
            }
            if (mapMode == MapMode.RAINFALL) {
                Button(onClick = {
                    mapViewModel.showMapLegend.postValue(!mapViewModel.showMapLegend.value!!)
                }, modifier = Modifier.padding(10.dp)) {
                    Text(text = "ℹ️ ${context.getString(R.string.map_view_map_legend)}")
                }
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
                                        fontWeight = if(dateTime == selectedDateTimeString) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(text = (if (autoplay) "⏹️" else "▶️"),
                        fontSize = 40.sp,
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
                Text(text = context.getString(R.string.map_view_fetching_data), fontSize = 20.sp)
            } else if ( displayedDataSet.isEmpty() && lastUpdateTimestamp != null){
                Row(horizontalArrangement = Arrangement.End) {
                    Text(text = context.getString(R.string.map_view_fetch_data_failed), fontSize = 20.sp)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        mapViewModel.updateRainfallDataSet()
                    }, modifier = Modifier.width(100.dp)) {
                        Text(text = context.getString(R.string.map_view_retry_fetching))
                    }
                }
            } else if (!isLocationAccessGranted) {
                Text(text = context.getString(R.string.map_view_location_permission_denied), fontSize = 20.sp)
            } else if (location == null || !locationEnabled) {
                Text(text = context.getString(R.string.map_view_location_unavailable),  fontSize = 20.sp)
            } else if ( !MapConstants.HKBoundary.contains(location ?: LatLng(0.0,0.0))) {
                Text(text = context.getString(R.string.map_view_location_out_of_boundary),  fontSize = 20.sp)
            } else if(currentLocationRainfallRange != null) {
                Row {
                    Column (modifier = Modifier.padding(5.dp)){
                        Text(text = context.getString(R.string.map_view_rainfall_in_next_2_hours),  maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("${currentLocationRainfallRange!!.first}${context.getString(R.string.map_view_mm)} - ${currentLocationRainfallRange!!.second}${context.getString(R.string.map_view_mm)} ",
                            style = TextStyle(background = currentLocationRainfallRange?.second?.getRainfallTileColor() ?: Color.Transparent),
                            fontSize = 20.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        mapViewModel.updateRainfallDataSet()
                    }, modifier = Modifier.width(120.dp)) {
                        Text(text = context.getString(R.string.map_view_refresh_data))
                    }
                }

            }
        }
    }

    @Composable
    fun weatherStationInfoRow() {
        val isFetchingWeatherStationData : Boolean by mapViewModel.isFetchingWeatherStationData.observeAsState(
            false
        )

        val weatherStationDataMap  : Map<String, WeatherStationData> by mapViewModel.weatherStationDataMap.observeAsState(
            initial = mapOf()
        )

        Column(modifier = Modifier
            .padding(10.0.dp)
            .height(60.dp)) {
            if(isFetchingWeatherStationData) {
                Text(text = context.getString(R.string.map_view_fetching_data), fontSize = 20.sp)
            } else if ( weatherStationDataMap.isEmpty() && mapViewModel.lastWeatherStationDataUpdateTimestamp != null){
                Row(horizontalArrangement = Arrangement.End) {
                    Text(text = context.getString(R.string.map_view_fetch_data_failed), fontSize = 20.sp)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        mapViewModel.updateWeatherStationTemperatureDataSet()
                    }, modifier = Modifier.width(100.dp)) {
                        Text(text = context.getString(R.string.map_view_retry_fetching))
                    }
                }
            }  else  {
                Row {
                    Text(text = context.getString(R.string.map_view_select_weather_station),
                        modifier = Modifier.padding(5.dp).fillMaxWidth(fraction = 0.75f),
                        softWrap = true)

                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        mapViewModel.updateWeatherStationTemperatureDataSet()
                    }, modifier = Modifier.width(120.dp)) {
                        Text(text = context.getString(R.string.map_view_refresh_data))
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
                .height(80.dp)
                .background(MapConstants.yellowTileColor())
                .fillMaxWidth()
            ){

                Text(text = weatherWarningDataList.summary(context) ?: "",
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable {
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
                Text(context.getString(R.string.map_view_weather_warning_prefix))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    for (warning in weatherWarningDataList ) {

                        Row() {
                            Text(
                                warning.description(context),
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                            )
                        }
                    }

                    mapViewModel.lastWeatherWarningUpdateTimestamp?.let {
                        Text(
                            "${context.getString(R.string.map_view_last_update_timestamp)}: ${it.getNiceFormattedString()}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        )
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


    Scaffold(
        topBar = {
            topAppBar()
        },
        content = {padding ->

            val mapMode : MapMode by mapViewModel.mapMode.observeAsState(initial = MapMode.RAINFALL)

            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(padding)) {
                Column(verticalArrangement = Arrangement.Top) {

                    if (mapMode == MapMode.RAINFALL) {
                        rainfallInfoRow()
                    } else if (mapMode == MapMode.WEATHERSTATION) {
                        weatherStationInfoRow()
                    }

                    rainfallMap(modifier = Modifier.weight(15f, fill = true))

                    WeatherWarningInfoRow()

                    if (mapMode == MapMode.RAINFALL) {
                        ForecastTimeSwitch(
                            modifier = Modifier
                                .weight(3f, fill = true)
                                .fillMaxWidth()
                        )
                    }

                    val lastUpdateTimestamp: Date? by mapViewModel.lastRainfallDataUpdateTimestamp.observeAsState(
                        null
                    )

                    val displayedDataSet: LatLngRainfallDataMap by mapViewModel.displayedRainfallDataMap.observeAsState(
                        mutableMapOf()
                    )

                    Row(modifier = Modifier.height(30.dp)) {
                        if (mapMode == MapMode.RAINFALL &&
                            lastUpdateTimestamp != null &&
                            !displayedDataSet.isNullOrEmpty()) {
                            Text(
                                text = "${context.getString(R.string.map_view_last_update_timestamp)}: ${displayedDataSet.values.first().updateTimeString.getNiceFormattedDateTimeStringFromDatetimeString()}",
                                modifier = Modifier.padding(5.dp)
                            )
                        } else if (mapMode == MapMode.WEATHERSTATION){
                            mapViewModel.lastWeatherStationDataUpdateTimestamp?.let {
                                Text(
                                    text = "${context.getString(R.string.map_view_last_update_timestamp)}: ${it.getNiceFormattedString()}",
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                        }
                    }
                }

                val isFetchingRainfallData: Boolean by mapViewModel.isFetchingData.observeAsState(
                    false
                )

                val isFetchingWeatherStationData: Boolean by mapViewModel.isFetchingWeatherStationData.observeAsState(
                    false
                )

                if (isFetchingRainfallData || isFetchingWeatherStationData) {
                    CircularProgressIndicator()
                }

                val showMapLegend: Boolean by mapViewModel.showMapLegend.observeAsState(
                    false
                )

                if (showMapLegend) {
                    mapLegendDialog()
                }

                val showWeatherWarningSummary: Boolean by mapViewModel.showWeatherWarningSummary.observeAsState(
                    false
                )

                if (showWeatherWarningSummary) {
                    weatherWarningSummaryDialog()
                }
            }
        })
}

