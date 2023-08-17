package com.gordon.hkrainfallmap

import com.google.android.gms.maps.model.LatLng

class WeatherStationData(val name: String, val temperature : Double, val updateDateTimeString: DateTimeString, val position : LatLng) {


    companion object {

        fun fromCSVLine(parameterArray: List<String>) : WeatherStationData? {

            if (parameterArray.count() != 3) { return null }

            val dataTime = parameterArray[0]
            val name = parameterArray[1]
            val temperature = parameterArray[2].toDoubleOrNull() ?: return null
            val position = MapConstants.automaticWeatherStationsLocation[name] ?: return null

            return  WeatherStationData(name = name, temperature = temperature, updateDateTimeString = dataTime, position = position)
        }
    }


}