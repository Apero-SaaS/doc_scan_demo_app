package com.apero.app.poc_ml_docscan.utils

import androidx.annotation.IntDef
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar

public fun Mat.absDiff(bgImage: Mat): Mat {
    val dst = Mat()
    Core.absdiff(this, bgImage, dst)
    return dst
}

public fun Mat.subtract(other: Mat): Mat {
    val dst = Mat()
    Core.subtract(this, other, dst)
    return dst
}

public operator fun Mat.minus(other: Mat): Mat = subtract(other)

public fun Mat.subtract(other: Scalar): Mat {
    val dst = Mat()
    Core.subtract(this, other, dst)
    return dst
}

public operator fun Mat.minus(other: Scalar): Mat = subtract(other)

public fun Mat.multiply(other: Mat): Mat {
    val dst = Mat()
    Core.multiply(this, other, dst)
    return dst
}

public operator fun Mat.times(other: Mat): Mat = multiply(other)

public fun Mat.multiply(other: Scalar): Mat {
    val dst = Mat()
    Core.multiply(this, other, dst)
    return dst
}

public operator fun Mat.times(other: Scalar): Mat = multiply(other)

public fun Mat.normalize(
    alpha: Double,
    beta: Double,
    @NormInt normType: Int,
    dtype: Int? = null,
): Mat {
    val dst = Mat()
    if (dtype != null) {
        Core.normalize(this, dst, alpha, beta, normType, dtype)
    } else {
        Core.normalize(this, dst, alpha, beta, normType)
    }
    return dst
}

@IntDef(
    value = [
        Core.NORM_INF,
        Core.NORM_L1,
        Core.NORM_L2,
        Core.NORM_L2SQR,
        Core.NORM_HAMMING,
        Core.NORM_HAMMING2,
        Core.NORM_RELATIVE,
        Core.NORM_MINMAX,
    ]
)
public annotation class NormInt

public fun Mat.rotate(@RotateCodeInt rotateCode: Int): Mat {
    val dst = Mat()
    Core.rotate(this, dst, rotateCode)
    return dst
}

@IntDef(
    value = [
        Core.ROTATE_90_CLOCKWISE,
        Core.ROTATE_180,
        Core.ROTATE_90_COUNTERCLOCKWISE,
    ]
)
public annotation class RotateCodeInt
