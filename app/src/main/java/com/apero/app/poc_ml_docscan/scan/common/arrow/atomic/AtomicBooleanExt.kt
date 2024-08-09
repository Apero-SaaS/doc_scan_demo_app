package com.apero.app.poc_ml_docscan.scan.common.arrow.atomic

import androidx.tracing.traceAsync
import arrow.atomic.AtomicBoolean
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import java.util.UUID

public suspend fun AtomicBoolean.guardUntilTrueWithTimeout(timeMillis: Long) {
    withTimeout(timeMillis) {
        guardUntilTrue()
    }
}

public suspend fun AtomicBoolean.guardUntilTrue(): Unit = traceAsync(
    "guardUntilTrue",
    UUID.randomUUID().mostSignificantBits.toInt()
) {
    while (!get()) {
        delay(1L)
        yield()
    }
}
