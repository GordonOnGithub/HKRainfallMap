package com.gordon.hkrainfallmap

import kotlinx.serialization.Serializable

@Serializable
 class WeatherWarningData(val name : String,
                             val code : String,
                             val actionCode : String,
                             val issueTime : String,
                             val updateTime: String) {


     fun description() : String {
         val readableCode = when (code) {
             "TC1" -> "No. 1 "
             "TC3" -> "No. 3 "
             "TC8NE" -> "No. 8 North East "
             "TC8SE" -> "No. 8 South East "
             "TC8SW" -> "No. 8 South West "
             "TC8NW" -> "No. 8 North West "
             "TC9" -> "No. 9 "
             "TC10" -> "No. 10 "
             "WRAINA" -> "Amber "
             "WRAINR"->" Red "
             "WRAINB" -> "Black "
             else -> ""
         }

         return readableCode + name


     }
 }