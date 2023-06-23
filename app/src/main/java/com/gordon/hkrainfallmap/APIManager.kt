package com.gordon.hkrainfallmap

import okhttp3.*
import java.io.BufferedReader
import java.io.File

interface APIManagerType {
    fun  getRainfallDataSet( callback: RainfallAPICallback)

}

class APIManager : APIManagerType {

    var client = OkHttpClient()

     override fun getRainfallDataSet(callback: RainfallAPICallback ) {
         val url = "https://data.weather.gov.hk/weatherAPI/hko_data/F3/Gridded_rainfall_nowcast.csv"

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

}