package com.apero.app.poc_ml_docscan.scan.common.util

import arrow.core.Either
import arrow.core.raise.Raise
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalTypeInference

public interface CrashlyticsReporter {
    public fun recordException(exception: Throwable)
}

@OptIn(ExperimentalTypeInference::class)
public suspend inline fun <Error : Throwable, A> CrashlyticsReporter.either(
    context: CoroutineContext,
    msg: String? = null,
    @BuilderInference crossinline block: suspend Raise<Error>.() -> A,
): Either<Error, A> =
    com.apero.app.poc_ml_docscan.scan.common.arrow.core.raise.either(context, block)
        .onLeft {
            val e = Exception("CrashlyticsReporter.either(msg=$msg)", it)

            recordException(e)
        }

@OptIn(ExperimentalTypeInference::class)
public inline fun <Error : Throwable, A> CrashlyticsReporter.either(
    msg: String? = null,
    @BuilderInference crossinline block: Raise<Error>.() -> A,
): Either<Error, A> = arrow.core.raise.either(block)
    .onLeft {
        val e = Exception("CrashlyticsReporter.either(msg=$msg)", it)

        recordException(e)
    }

public inline fun <E : Throwable, R> Either<E, R>.reportLeftTo(
    reporter: CrashlyticsReporter,
): Either<E, R> = onLeft {
    reporter.recordException(it)
}
