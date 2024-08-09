package com.apero.app.poc_ml_docscan.scan.common.model

import androidx.annotation.VisibleForTesting
import arrow.core.padZip
import com.apero.app.poc_ml_docscan.scan.common.util.packFloats
import com.apero.app.poc_ml_docscan.scan.common.util.unpackFloat1
import com.apero.app.poc_ml_docscan.scan.common.util.unpackFloat2
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
public value class Offset(
    public val packagedValue: Long,
) {
    public constructor(x: Float, y: Float) : this(packFloats(x, y))

    public val x: Float get() = unpackFloat1(packagedValue)
    public val y: Float get() = unpackFloat2(packagedValue)

    public operator fun component1(): Float = x
    public operator fun component2(): Float = y

    override fun toString(): String {
        return "Offset(x=$x, y=$y)"
    }

    public companion object
}

// WARN: Dev shouldn't use this api in production code as it provide convenience way to construct
// objects in tests
@VisibleForTesting
public fun Offset(x: Double, y: Double): Offset = Offset(x.toFloat(), y.toFloat())

public fun Offset.getDistanceSquared(): Float = x * x + y * y

public operator fun Offset.minus(other: Offset): Offset = Offset(x - other.x, y - other.y)

public fun List<Offset>.equalsApproximately(other: List<Offset>): Boolean = padZip(other)
    .all { (first, second) ->
        first != null && second != null && first.equalsApproximately(second)
    }

public fun Offset.equalsApproximately(other: Offset): Boolean {
    return (other - this).getDistanceSquared() < 10e-4
}
