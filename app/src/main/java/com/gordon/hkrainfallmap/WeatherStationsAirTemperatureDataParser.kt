package com.gordon.hkrainfallmap

interface WeatherStationsAirTemperatureDataParserType {
    fun parse(rawDataString : String?) : Map<String, WeatherStationData>?

}

class WeatherStationsAirTemperatureDataParser : WeatherStationsAirTemperatureDataParserType{

    override fun parse(rawDataString : String?) : Map<String, WeatherStationData>? {

        if (rawDataString.isNullOrEmpty()) { return null }

        var dataArray = rawDataString.split('\n')
        if (dataArray.count() < 2) {
            return null
        }

        dataArray = dataArray.subList(1, dataArray.count() - 1)

        var parsedDataArrayMap: MutableMap<String, WeatherStationData> = mutableMapOf()

        for (dataString in dataArray) {
            val parametersArray = dataString.split(',')

            if (parametersArray.count() != 3) { continue }

            val data = WeatherStationData.fromCSVLine(parameterArray = parametersArray)
            data?.let {
                parsedDataArrayMap[it.name] = it
            }
        }

        return  parsedDataArrayMap
    }

}