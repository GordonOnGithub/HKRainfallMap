package com.gordon.hkrainfallmap

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class MapConstants {

    companion object {

        val HKLatLng = LatLng(22.2765473,114.1878291)
        val HKBoundary = LatLngBounds(LatLng(22.1, 113.8), LatLng(22.65,114.44))
        val HKBoundaryPolygonPoints : List<LatLng> = listOf( LatLng(22.1, 113.8), LatLng(22.1, 114.44),
            LatLng(22.65, 114.44), LatLng(22.65,113.8)
        )

    }

}