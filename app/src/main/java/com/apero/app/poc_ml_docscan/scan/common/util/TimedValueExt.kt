package com.apero.app.poc_ml_docscan.scan.common.util

import kotlin.time.TimedValue

public inline fun <T, R> TimedValue<T>.map(transform: (T) -> R): TimedValue<R> {
    return TimedValue(transform(value), duration)
}
