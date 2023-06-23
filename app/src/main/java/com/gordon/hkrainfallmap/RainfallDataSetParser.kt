package com.gordon.hkrainfallmap

interface RainfallDataSetParserType {
    fun parse(rawDataString : String?) : RainfallDataSet?
}

class RainfallDataSetParser : RainfallDataSetParserType {

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

            var latLngMap = parsedDataArrayMap[rainfallData.forecastDate] ?: mutableMapOf()

            if(latLngMap.isEmpty()) {
                parsedDataArrayMap[rainfallData.forecastDate] = latLngMap
            }

            latLngMap[rainfallData.position] = rainfallData

        }

        return  parsedDataArrayMap
    }
}