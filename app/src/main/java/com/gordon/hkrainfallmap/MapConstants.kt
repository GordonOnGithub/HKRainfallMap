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

        val mapMinZoomLevel = 10.0f
        val mapMaxZoomLevel = 15.0f

        fun blueTileColor(forMap: Boolean = true) : Color {
            return if (forMap) {
                Color(0x640000FF)
            }else {
                Color(0xFF0000FF)
            }
        }

        fun greenTileColor(forMap: Boolean = true) : Color {
            return if (forMap) {
                Color(0x6400FF00)
            }else {
                Color(0xFF00FF00)
            }
        }

        fun yellowTileColor(forMap: Boolean = true) : Color {
            return if (forMap) {
                Color(0x64FFFF00)
            }else {
                Color(0xFFFFFF00)
            }
        }

        fun orangeTileColor(forMap: Boolean = true) : Color {
            return if (forMap) {
                Color(0x64FF8000)
            }else {
                Color(0xFFFF8000)
            }
        }

        fun redTileColor(forMap: Boolean = true) : Color {
            return if (forMap) {
                Color(0x64FF0000)
            }else {
                Color(0xFFFF0000)
            }
        }

        val rainfallDataRefreshInterval = 300_000 // in milliseconds

    }

}