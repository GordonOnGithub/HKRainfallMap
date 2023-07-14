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