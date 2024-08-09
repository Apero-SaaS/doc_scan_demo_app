package com.apero.app.poc_ml_docscan.scan.common.util

import androidx.tracing.traceAsync as androidxTraceAsync
import java.util.UUID

public suspend inline fun <T> traceAsync(
    methodName: String,
    cookie: Int = UUID.randomUUID().leastSignificantBits.toInt(),
    crossinline block: suspend () -> T,
): T = androidxTraceAsync(methodName, cookie, block)
