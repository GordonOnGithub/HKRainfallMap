package com.gordon.hkrainfallmap

import androidx.compose.ui.graphics.Color

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

fun List<WeatherWarningData>.summary(): String?{

    if (this.isNullOrEmpty()) { return  "" }


    val filteredSelf = this.filter { it.actionCode != "CANCEL" }

    if (filteredSelf.isNullOrEmpty()) { return  "" }

    val first = filteredSelf.first().description()

    val otherWarningCount = filteredSelf.count() - 1

    val otherWarningLabel =
        if(otherWarningCount > 0) " & " + "$otherWarningCount" + " other warnings "
    else ""

    return "Warning in force: $first$otherWarningLabel"
}