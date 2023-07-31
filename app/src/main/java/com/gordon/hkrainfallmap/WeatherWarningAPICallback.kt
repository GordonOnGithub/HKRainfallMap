package com.gordon.hkrainfallmap
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

typealias WeatherWarningAPIReceivedBlock =  (List<WeatherWarningData>) -> Unit
typealias WeatherWarningAPIFailureBlock =  () -> Unit

class WeatherWarningAPICallback( dataParser: WeatherWarningDataParserType = WeatherWarningDataParser(), onDataReceived : WeatherWarningAPIReceivedBlock, onFailure: WeatherWarningAPIFailureBlock) : Callback {

    var onDataReceivedBlock : WeatherWarningAPIReceivedBlock? = onDataReceived

    var onFailureBlock : WeatherWarningAPIFailureBlock? = onFailure

    val dataParser : WeatherWarningDataParserType = dataParser

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
            it(data ?: listOf())
        }
    }
}