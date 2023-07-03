package com.gordon.hkrainfallmap

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.util.Date

class RainfallData(val updateTimeString: String, val forecastTimestring: String, val lat: Double, val lng: Double, val rainfall: Double) {



    val updateDate: String = updateTimeString
    val forecastDate: String = forecastTimestring
    val position: LatLng = LatLng(lat, lng)
    val expectedRainfall: Double = rainfall

    fun tilePolygonPoints () : List<LatLng>{

        var list = mutableListOf<LatLng>()
        list.add(LatLng(position.latitude - 0.009f, position.longitude - 0.01f))
        list.add(LatLng(position.latitude - 0.009f, position.longitude + 0.01f))
        list.add(LatLng(position.latitude + 0.009f, position.longitude + 0.01f))
        list.add(LatLng(position.latitude + 0.009f, position.longitude - 0.01f))


        return list
    }

    fun contain(location : LatLng) : Boolean {
        val boundary = LatLngBounds(LatLng(position.latitude - 0.01f, position.longitude - 0.01f), LatLng(position.latitude + 0.01f, position.longitude + 0.01f))
        return  boundary.contains(location)
    }

    companion object {
        fun fromCSVLine(data: List<String>) : RainfallData? {
            if (data.count() != 5) { return null }

            val updateTime = data[0]

            val forecastTime = data[1]

            val lat = data[2].toDoubleOrNull()?: return null

            val lng = data[3].toDoubleOrNull()?: return null

            val rainfall = data[4].toDoubleOrNull()?: return null

            return RainfallData(updateTime, forecastTime, lat, lng, rainfall)

     }
    }


}