package com.gordon.hkrainfallmap

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class MapConstants {

    companion object {

        private val HKSouthWestLat = 22.1
        private val HKSouthWestLng = 113.8

        private val HKNorthEastLat = 22.65
        private val HKNorthEastLng = 114.44

        val HKLatLng = LatLng((HKNorthEastLat + HKSouthWestLat) / 2,(HKSouthWestLng + HKNorthEastLng) / 2 )
        val HKBoundary = LatLngBounds(LatLng(HKSouthWestLat, HKSouthWestLng), LatLng(HKNorthEastLat,HKNorthEastLng))
        val HKBoundaryPolygonPoints : List<LatLng> = listOf( LatLng(HKSouthWestLat, HKSouthWestLng), LatLng(HKSouthWestLat, HKNorthEastLng),
            LatLng(HKNorthEastLat, HKNorthEastLng), LatLng(HKNorthEastLat, HKSouthWestLng)
        )

        val mapMinZoomLevel = 9.0f
        val mapMaxZoomLevel = 15.0f

        val blueTileColor : Color = Color(0x480000FF)

        val greenTileColor : Color = Color(0x4800FF00)

        val yellowTileColor : Color = Color(0x48FFFF00)

        val orangeTileColor : Color = Color(0x48FF8000)

        val redTileColor : Color = Color(0x48FF0000)

        val rainfallDataRefreshInterval = 300_000 // in milliseconds

    }

}