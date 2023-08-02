package com.gordon.hkrainfallmap

import android.content.Context
import kotlinx.serialization.Serializable

@Serializable
 class WeatherWarningData(val name : String,
                             val code : String,
                             val actionCode : String,
                             val issueTime : String,
                             val updateTime: String) {


     fun description(context : Context) : String {
         val readableCode = when (code) {
             "TC1" -> context.getString(R.string.weather_warning_code_tc1)
             "TC3" -> context.getString(R.string.weather_warning_code_tc3)
             "TC8NE" -> context.getString(R.string.weather_warning_code_tc8ne)
             "TC8SE" -> context.getString(R.string.weather_warning_code_tc8se)
             "TC8SW" -> context.getString(R.string.weather_warning_code_tc8sw)
             "TC8NW" -> context.getString(R.string.weather_warning_code_tc8nw)
             "TC9" -> context.getString(R.string.weather_warning_code_tc9)
             "TC10" -> context.getString(R.string.weather_warning_code_tc10)
             "WRAINA" -> context.getString(R.string.weather_warning_code_amber_rain)
             "WRAINR"-> context.getString(R.string.weather_warning_code_red_rain)
             "WRAINB" -> context.getString(R.string.weather_warning_code_black_rain)
             else -> ""
         }

         return readableCode + name


     }
 }