package com.gordon.hkrainfallmap

import com.google.android.gms.maps.model.LatLng
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

typealias DateTimeString = String
typealias RainfallDataSet = MutableMap<DateTimeString, LatLngRainfallDataMap>
typealias LatLngRainfallDataMap = MutableMap<LatLng, RainfallData>

typealias RainfallAPIReceivedBlock =  (RainfallDataSet) -> Unit
typealias RainfallAPIFailureBlock =  () -> Unit

class RainfallAPICallback(parser: RainfallDataSetParserType = RainfallDataSetParser() ,onDataReceived: RainfallAPIReceivedBlock, onFailure: RainfallAPIFailureBlock?) : Callback{

    val dataSetParser : RainfallDataSetParserType = parser

    var onDataReceivedBlock : RainfallAPIReceivedBlock? = onDataReceived

    var onFailureBlock : RainfallAPIFailureBlock? = onFailure

    override fun onFailure(call: Call, e: java.io.IOException) {
        println(e.message)
        onFailureBlock?.let { it() }
    }

    override fun onResponse(call: Call, response: Response) {
        val body = response.body?.string()

//        if (body.isNullOrEmpty()) {
//            onFailureBlock?.let { it() }
//            return
//        }

        val dataSet = dataSetParser.parse(body)

        if (dataSet.isNullOrEmpty()){
            println("[RainfallAPICallback] failure")

            onFailureBlock?.let { it() }
            return
        }


//        var dataArray = body.split('\n')
//        if (dataArray.count() < 2) {
//            onFailureBlock?.let { it() }
//            return
//        }
//
//        dataArray = dataArray.subList(1, dataArray.count() - 1)
//
//        var parsedDataArrayMap: RainfallDataSet = mutableMapOf()
//
//        for (dataString in dataArray) {
//            val parametersArray = dataString.split(',')
//            val rainfallData = RainfallData.fromCSVLine(parametersArray) ?: continue
//
//            var latLngMap = parsedDataArrayMap[rainfallData.forecastDate] ?: mutableMapOf()
//
//            if(latLngMap.isEmpty()) {
//                parsedDataArrayMap[rainfallData.forecastDate] = latLngMap
//            }
//
//            latLngMap[rainfallData.position] = rainfallData
//
//
//        }

        println("[RainfallAPICallback] success")

        onDataReceivedBlock?.let { it(dataSet) }
    }


}