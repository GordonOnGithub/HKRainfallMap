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

        val weatherStationDataRefreshInterval = 60_000 // in milliseconds


        val automaticWeatherStationsLocation = mapOf<String, LatLng>(
            "Chek Lap Kok" to LatLng( 22.3094444, 113.9219444),
            "Cheung Chau" to LatLng(22.2011111, 114.0266667),
            "Clear Water Bay" to LatLng(22.2633333, 114.2997222),
            "Happy Valley" to LatLng(22.2705556, 114.1836111),
            "HK Observatory" to LatLng(22.3019444, 114.1741667),
            "HK Park" to  LatLng( 22.2783333, 114.1622222),
            "Kai Tak Runway Park" to LatLng(22.3047222, 114.2169444),
            "Kau Sai Chau" to LatLng(22.3702778, 114.3125),
            "King's Park" to LatLng(22.3119444, 114.1727778),
            "Kowloon City" to LatLng(22.335, 114.1847222),
            "Kwun Tong" to LatLng( 22.3186111, 114.2247222),
            "Lau Fau Shan" to LatLng(22.4688889, 113.9836111),
            "Ngong Ping" to LatLng(22.2586111, 113.9127778 ),
            "Pak Tam Chung" to LatLng(22.4027778, 114.3230556),
            "Peng Chau" to LatLng(22.2911111, 114.0433333),
            "Sai Kung" to LatLng(22.3755556, 114.2744444),
            "Sha Tin" to LatLng(22.4025, 114.21),
            "Sham Shui Po" to LatLng( 22.3358333, 114.1369444),
            "Shau Kei Wan" to LatLng(22.2816667, 114.2361111),
            "Shek Kong" to LatLng(22.4361111, 114.0847222),
            "Sheung Shui" to LatLng(22.5019444, 114.1111111),
            "Stanley" to LatLng(22.2141667, 114.2186111),
            "Ta Kwu Ling" to LatLng(22.5286111, 114.1566667 ),
            "Tai Lung" to LatLng(22.4847222, 114.1175),
            "Tai Mei Tuk" to LatLng(22.4752778, 114.2375),
            "Tai Mo Shan" to LatLng(22.4105556, 114.1244444),
            "Tai Po" to LatLng(22.4461111, 114.1788889),
            "Tate's Cairn" to LatLng(22.3577778, 114.2177778),
            "The Peak" to LatLng(22.2641667, 114.155),
            "Tseung Kwan O" to LatLng(22.3158333, 114.2555556),
            "Tsing Yi" to LatLng(22.3441667, 114.11),
            "Tsuen Wan Ho Koon" to LatLng( 22.3836111, 114.1077778),
            "Tsuen Wan Shing Mun Valley" to LatLng(22.3755556, 114.1266667),
            "Tuen Mun" to LatLng(22.3858333, 113.9641667),
            "Waglan Island" to LatLng(22.1822222, 114.3033333),
            "Wetland Park" to LatLng(22.4666667, 114.0088889),
            "Wong Chuk Hang" to LatLng(22.2477778, 114.1736111 ),
            "Wong Tai Sin" to LatLng( 22.3394444, 114.2052778),
            "Yuen Long Park" to LatLng(22.4408333, 114.0183333)
        )
    }

}