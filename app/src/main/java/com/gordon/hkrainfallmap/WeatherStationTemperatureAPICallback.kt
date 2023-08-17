package com.gordon.hkrainfallmap

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response


typealias WeatherStationTemperatureAPIReceivedBlock =  (Map<String, WeatherStationData>) -> Unit
typealias WeatherStationTemperatureAPIFailureBlock =  () -> Unit

class WeatherStationTemperatureAPICallback( dataParser: WeatherStationsAirTemperatureDataParserType =  WeatherStationsAirTemperatureDataParser(), onDataReceived : WeatherStationTemperatureAPIReceivedBlock, onFailure: WeatherStationTemperatureAPIFailureBlock) :
    Callback {

    var onDataReceivedBlock : WeatherStationTemperatureAPIReceivedBlock? = onDataReceived

    var onFailureBlock : WeatherStationTemperatureAPIFailureBlock? = onFailure

    val dataParser : WeatherStationsAirTemperatureDataParserType = dataParser

    override fun onFailure(call: Call, e: java.io.IOException) {
        println(e.message)
        onFailureBlock?.let { it() }
    }

    override fun onResponse(call: Call, response: Response) {
        val body = response.body?.string()

        val data = dataParser.parse(body)

        if (data.isNullOrEmpty()) {
            onFailureBlock?.let { it() }
        }

        onDataReceivedBlock?.let {
            it(data ?: mapOf())
        }
    }
}