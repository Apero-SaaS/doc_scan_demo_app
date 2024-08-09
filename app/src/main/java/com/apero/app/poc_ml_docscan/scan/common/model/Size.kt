package com.apero.app.poc_ml_docscan.scan.common.model

import com.apero.app.poc_ml_docscan.scan.common.util.packFloats
import com.apero.app.poc_ml_docscan.scan.common.util.unpackFloat1
import com.apero.app.poc_ml_docscan.scan.common.util.unpackFloat2
import kotlinx.serialization.Serializable

@Serializable
public data class Size internal constructor(internal val packagedValue: Long) {
    public constructor(width: Float, height: Float) : this(packFloats(width, height))

    val width: Float get() = unpackFloat1(packagedValue)
    val height: Float get() = unpackFloat2(packagedValue)

    override fun toString(): String {
        return "Size(width=$width, height=$height)"
    }
}

public fun Size.scale(x: Float, y: Float): Size {
    return Size(width * x, height * y)
}
