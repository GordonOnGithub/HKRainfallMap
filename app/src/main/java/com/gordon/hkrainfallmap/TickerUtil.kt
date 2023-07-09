package com.gordon.hkrainfallmap

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class TickerUtil {
    companion object {
        fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
            delay(initialDelay)
            while (true) {
                emit(Unit)
                delay(period)
            }
        }
    }

}