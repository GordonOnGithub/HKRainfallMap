package com.gordon.hkrainfallmap

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

fun Double.getRainfallTileColor() : Color? {
    if (this >= 20) {
        return MapConstants.redTileColor()
    }

    if (this >= 10) {
        return MapConstants.orangeTileColor()
    }

    if (this >= 5) {
        return MapConstants.yellowTileColor()
    }

    if (this >= 2.5) {
        return MapConstants.greenTileColor()
    }

    if (this >= 0.5) {
        return MapConstants.blueTileColor()
    }

    return null
}

fun String.getNiceFormattedTimeStringFromDatetimeString() : String? {

    if (this.length != 12) {
        return  null
    }

    return this.substring(
        this.length - 4,
        this.length - 2
    ) + " : " + this.substring(
        this.length - 2,
        this.length
    )

}

fun String.getNiceFormattedDateTimeStringFromDatetimeString() : String? {

    if (this.length != 12) {
        return  null
    }

    return this.substring(0, this.length - 8 ) + "/" +
    this.substring(this.length - 8, this.length - 6 ) +"/" +
    this.substring(this.length - 6, this.length - 4 ) + " " +

    this.substring(
        this.length - 4,
        this.length - 2
    ) + ":" + this.substring(
        this.length - 2,
        this.length
    )

}

fun List<WeatherWarningData>.summary(context : Context): String?{

    if (this.isNullOrEmpty()) { return  "" }


    val filteredSelf = this.filter { it.actionCode != "CANCEL" }

    if (filteredSelf.isNullOrEmpty()) { return  "" }

    val first = filteredSelf.first().description(context)

    val otherWarningCount = filteredSelf.count() - 1

    val otherWarningLabel =
        if(otherWarningCount > 0) String.format( context.getString(R.string.map_view_weather_warning_suffix), otherWarningCount)
    else ""

    return "⚠️ ${context.getString(R.string.map_view_weather_warning_prefix)} $first $otherWarningLabel"
}

fun Date.getNiceFormattedString() : String {
    return SimpleDateFormat("yyyy/MM/dd HH:mm").format(this)
}