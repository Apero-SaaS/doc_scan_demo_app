package com.apero.app.poc_ml_docscan.scan.common.util

import arrow.core.Either
import arrow.core.Nel
import java.io.PrintStream
import java.io.PrintWriter

public class CompositeException(
    message: String,
    public val causes: Nel<Throwable>,
) : Exception() {
    override val message: String =
        "Composite exception containing ${causes.size} exceptions. Message: $message"

    override fun printStackTrace() {
        println(message)
        causes.forEachIndexed { index, exception ->
            println("Exception ${index + 1}:")
            exception.printStackTrace()
        }
    }

    override fun printStackTrace(s: PrintStream) {
        s.println(message)
        causes.forEachIndexed { index, exception ->
            println("Exception ${index + 1}:")
            exception.printStackTrace(s)
        }
    }

    override fun printStackTrace(s: PrintWriter) {
        s.println(message)
        causes.forEachIndexed { index, exception ->
            println("Exception ${index + 1}:")
            exception.printStackTrace(s)
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
public inline fun <R> Either<Nel<Throwable>, R>.mapLeftComposite(
    message: String,
): Either<CompositeException, R> = mapLeft {
    CompositeException(message, it)
}
