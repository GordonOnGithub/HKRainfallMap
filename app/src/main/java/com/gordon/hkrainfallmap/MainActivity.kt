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
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    val settingsViewModel by lazy {
        SettingsViewModel(context = this)
    }

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

                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = NavigationRoutes.map
                    ){
                        composable(NavigationRoutes.map){
                            RainfallNowcastMapScreen(mapViewModel = mapViewModel, context = applicationContext, navigationController = navController)
                        }

                        composable(NavigationRoutes.settings){
                            SettingsScreen(settingsViewModel = settingsViewModel ,context = applicationContext, navigationController = navController)
                        }
                    }

                }
            }
        }
    }
}

class NavigationRoutes {
    companion object {
        val map = "map"
        val settings = "settings"
    }

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HKRainfallMapTheme {

    }
}