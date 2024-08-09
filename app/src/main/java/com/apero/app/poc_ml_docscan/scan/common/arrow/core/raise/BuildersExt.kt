package com.apero.app.poc_ml_docscan.scan.common.arrow.core.raise

import arrow.core.Either
import arrow.core.raise.Raise
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
public suspend inline fun <Error, A> either(
    context: CoroutineContext,
    @BuilderInference crossinline block: suspend Raise<Error>.() -> A,
): Either<Error, A> = withContext(context) {
    arrow.core.raise.either { block.invoke(this) }
}
