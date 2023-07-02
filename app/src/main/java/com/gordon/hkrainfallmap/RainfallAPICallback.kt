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

        val dataSet = dataSetParser.parse(body)

        if (dataSet.isNullOrEmpty()){
            onFailureBlock?.let { it() }
            return
        }

        onDataReceivedBlock?.let { it(dataSet) }
    }


}