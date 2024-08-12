package com.apero.app.poc_ml_docscan.image_processing.model

import android.util.Size
import androidx.annotation.FloatRange

/**
 * Created by KO Huyn on 29/03/2024.
 */
public sealed class ResizeTransformation : ImageTransformation {
    public data class ScaleDown(
        @FloatRange(from = 0.001, to = 1.0) val scale: Float
    ) : ResizeTransformation()

    public data class TargetSize(val size: Size) : ResizeTransformation()
    public data class MaxLength(val lengthMb: Float) : ResizeTransformation()
}