package com.gordon.hkrainfallmap

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

interface RainfallDataSetParserType {
    fun parse(rawDataString : String?) : RainfallDataSet?
}

class RainfallDataSetParser(mapBoundaryfilter : LatLngBounds = MapConstants.HKBoundary) : RainfallDataSetParserType {

    var boundaryFilter = mapBoundaryfilter

    override fun parse(rawDataString : String?) : RainfallDataSet? {

        if (rawDataString.isNullOrEmpty()) { return null }

        var dataArray = rawDataString.split('\n')
        if (dataArray.count() < 2) {
            return null
        }

        dataArray = dataArray.subList(1, dataArray.count() - 1)

        var parsedDataArrayMap: RainfallDataSet = mutableMapOf()

        for (dataString in dataArray) {
            val parametersArray = dataString.split(',')
            val rainfallData = RainfallData.fromCSVLine(parametersArray) ?: continue

            if(! boundaryFilter.contains(rainfallData.position)) { continue }


            var latLngMap = parsedDataArrayMap[rainfallData.forecastDate] ?: mutableMapOf()

            if(latLngMap.isEmpty()) {
                parsedDataArrayMap[rainfallData.forecastDate] = latLngMap
            }

            latLngMap[rainfallData.position] = rainfallData

        }

        return  parsedDataArrayMap
    }
}