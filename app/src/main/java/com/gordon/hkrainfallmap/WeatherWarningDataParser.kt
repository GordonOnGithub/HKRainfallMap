package com.gordon.hkrainfallmap

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

interface  WeatherWarningDataParserType {
    fun parse(rawDataString : String?) : List<WeatherWarningData>?
    }

class WeatherWarningDataParser : WeatherWarningDataParserType {

    private val priorityMapping = mapOf( "WTCSGNL" to 4, "WRAIN" to 3, "WTCPRE8" to 2, "WTS" to 1 )

    override fun parse(rawDataString : String?) : List<WeatherWarningData>? {

        if (rawDataString.isNullOrEmpty()) { return null }

        try {
            val dataSet: Map<String, WeatherWarningData> =
                Json { ignoreUnknownKeys = true }.decodeFromString(string = rawDataString)

            val sortedWarningData = dataSet.entries.sortedWith { a, b ->
                when {
                    (priorityMapping[a.key] ?: 0) < (priorityMapping[b.key] ?: 0) -> 1
                    (priorityMapping[a.key] ?: 0) > (priorityMapping[b.key] ?: 0) -> -1
                    else -> 0
                }

            }.map { it.value }

            return sortedWarningData.filter { it.actionCode != "CANCEL" }
        } catch (e : Exception){
            return null
        }
    }
}