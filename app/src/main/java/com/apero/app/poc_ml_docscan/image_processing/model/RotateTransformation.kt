package com.apero.app.poc_ml_docscan.image_processing.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
public value class RotateTransformation(public val degrees: Float) : ImageTransformation {
    public companion object {
        public const val FULL_CIRCLE_ROTATION: Float = 360f
    }
}

public val RotateTransformation.isEvenFullRotation: Boolean
    get() = degrees % RotateTransformation.FULL_CIRCLE_ROTATION == 0F

public val RotateTransformation.remDegrees: Float
    get() = degrees.rem(RotateTransformation.FULL_CIRCLE_ROTATION)
