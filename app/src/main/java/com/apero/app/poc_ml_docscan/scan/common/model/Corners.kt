package com.apero.app.poc_ml_docscan.scan.common.model

import androidx.annotation.VisibleForTesting
import arrow.core.Nel
import arrow.core.nonEmptyListOf

@JvmInline
public value class Corners(
    public val points: Nel<Offset>,
) {
    public val topLeft: Offset get() = points[0]
    public val topRight: Offset get() = points[1]
    public val bottomRight: Offset get() = points[2]
    public val bottomLeft: Offset get() = points[3]

    override fun toString(): String = buildString {
        append("Corners(")
        append("topLeft=$topLeft, ")
        append("topRight=$topRight, ")
        append("bottomLeft=$bottomLeft, ")
        append("bottomRight=$bottomRight, ")
        append(")")
    }

    public companion object
}

// WARN: Dev shouldn't use this api in production code as it provide convenience way to construct
// objects in tests
@VisibleForTesting
public fun Corners(
    topLeft: Offset,
    topRight: Offset,
    bottomRight: Offset,
    bottomLeft: Offset,
): Corners = Corners(nonEmptyListOf(topLeft, topRight, bottomRight, bottomLeft))
