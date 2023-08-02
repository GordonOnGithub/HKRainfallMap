package com.gordon.hkrainfallmap

import android.os.LocaleList
import okhttp3.*
import java.io.BufferedReader
import java.io.File
import java.util.Locale

interface APIManagerType {
    fun  getRainfallDataSet( callback: RainfallAPICallback)

    fun  getWeatherWarningDataSet( callback: WeatherWarningAPICallback)

}

class APIManager() : APIManagerType {

    var client = OkHttpClient()

    var locale : Locale = LocaleList.getDefault().get(0)

     override fun getRainfallDataSet(callback: RainfallAPICallback ) {
         val url = "https://data.weather.gov.hk/weatherAPI/hko_data/F3/Gridded_rainfall_nowcast.csv"

        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()

        val call = client.newCall(request)

         call.enqueue(callback)
    }

    override fun getWeatherWarningDataSet( callback: WeatherWarningAPICallback){
        // TODO: take locale as parameter
        val langCode = if (locale.language.contains("zh") ) "tc" else "en"

        val url = "https://data.weather.gov.hk/weatherAPI/opendata/weather.php?dataType=warnsum&lang=${langCode}"

        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()

        val call = client.newCall(request)

        call.enqueue(callback)
    }

}

class MockAPIManager : APIManagerType {

    override fun getRainfallDataSet(callback: RainfallAPICallback) {

        val fileContent = this::class.java.classLoader
            .getResource("mock_rainfall_data.csv")?.readText()

        val mockDataSet = callback.dataSetParser.parse(fileContent)

        if(fileContent.isNullOrEmpty() || mockDataSet.isNullOrEmpty()) {
            callback.onFailureBlock?.let { it() }
            return
        }

        callback.onDataReceivedBlock?.let {
            it(mockDataSet)
        }
    }

    override fun getWeatherWarningDataSet( callback: WeatherWarningAPICallback){
        // TODO
        val fileContent = this::class.java.classLoader
            .getResource("mock_warning_data.json")?.readText()

        val mockDataSet = callback.dataParser.parse(fileContent)

        if(fileContent.isNullOrEmpty() || mockDataSet.isNullOrEmpty()) {
            callback.onFailureBlock?.let { it() }
            return
        }

        callback.onDataReceivedBlock?.let {
            it(mockDataSet)
        }
    }

}