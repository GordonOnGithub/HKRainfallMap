package com.gordon.hkrainfallmap

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class MapConstants {

    companion object {

        val HKLatLng = LatLng(22.2765473,114.1878291)
        val HKBoundary = LatLngBounds(LatLng(22.1, 113.8), LatLng(22.65,114.44))
        val HKBoundaryPolygonPoints : List<LatLng> = listOf( LatLng(22.1, 113.8), LatLng(22.1, 114.44),
            LatLng(22.65, 114.44), LatLng(22.65,113.8)
        )

        val mapMinZoomLevel = 9.0f
        val mapMaxZoomLevel = 15.0f

        val blueTileColor : Color = Color(0x480000FF)

        val greenTileColor : Color = Color(0x4800FF00)

        val yellowTileColor : Color = Color(0x48FFFF00)

        val orangeTileColor : Color = Color(0x48FF8000)

        val redTileColor : Color = Color(0x48FF0000)

    }

}