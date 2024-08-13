package com.apero.app.poc_ml_docscan.utils

import android.graphics.Bitmap
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.sqrt

public fun Mat.toGray(bitmap: Bitmap) {
    Utils.bitmapToMat(bitmap, this)
    Imgproc.cvtColor(this, this, Imgproc.COLOR_RGB2GRAY)
}

public inline fun Mat.gaussianBlur(
    bitmap: Bitmap,
    kSize: Size = Size(125.toDouble(), 125.toDouble()),
    sigmaX: Double = 0.toDouble(),
    block: (Bitmap) -> Unit,
) {
    Utils.bitmapToMat(bitmap, this)
    Imgproc.GaussianBlur(this, this, kSize, sigmaX)
    return block(this.toBitmap())
}

public inline fun Mat.canny(
    bitmap: Bitmap,
    threshold1: Double = 20.toDouble(),
    threshold2: Double = 255.toDouble(),
    block: (Bitmap) -> Unit,
) {
    this.toGray(bitmap)
    Imgproc.Canny(this, this, threshold1, threshold2)
    return block(this.toBitmap())
}

public fun Mat.threshold(
    bitmap: Bitmap,
    thresh: Double = 50.toDouble(),
    maxVal: Double = 255.toDouble(),
    type: Int = Imgproc.THRESH_BINARY,
    block: (Bitmap) -> Unit,
) {
    this.toGray(bitmap)
    Imgproc.threshold(this, this, thresh, maxVal, type)
    return block(this.toBitmap())
}

public fun Mat.adaptiveThreshold(
    bitmap: Bitmap, maxValue: Double = 255.toDouble(),
    adaptiveMethod: Int = Imgproc.ADAPTIVE_THRESH_MEAN_C,
    thresholdType: Int = Imgproc.THRESH_BINARY,
    blockSize: Int = 11,
    c: Double = 12.toDouble(),
    block: (Bitmap) -> Unit,
) {
    this.toGray(bitmap)
    Imgproc.adaptiveThreshold(this, this, maxValue, adaptiveMethod, thresholdType, blockSize, c)
    return block(this.toBitmap())
}

public fun Mat.dilate(kernel: Mat): Mat {
    val dst = Mat()
    Imgproc.dilate(this, dst, kernel)
    return dst
}

public fun Mat.erode(kernel: Mat): Mat {
    val dst = Mat()
    Imgproc.erode(this, dst, kernel)
    return dst
}

public fun Mat.medianBlur(ksize: Int): Mat {
    val dst = Mat()
    Imgproc.medianBlur(this, dst, ksize)
    return dst
}

public fun Mat.threshold(thresh: Double, maxval: Double, @ThresholdInt type: Int): Mat {
    val dst = Mat()
    Imgproc.threshold(this, dst, thresh, maxval, type)
    return dst
}

public fun Mat.resize(targetSize: Size): Mat {
    val dst = Mat()
    Imgproc.resize(this, dst, targetSize)
    return dst
}

public fun Mat.resize(@FloatRange(from = 0.001, to = 1.0) scaleDown: Float): Mat {
    val dst = Mat()
    val currentSize = size()
    val newSize = Size(currentSize.width * scaleDown, currentSize.height * scaleDown)
    Imgproc.resize(this, dst, newSize)
    return dst
}

public fun Mat.resizeToMaxMB(maxLength: Float): Mat {
    val dst = this
    val numBytes = dst.total() * dst.elemSize()
    val maxBytes = maxLength * 1024 * 1024
    if (numBytes > maxBytes) {
        val scaleFactor = sqrt(maxBytes.toDouble() / numBytes)
        val finalSize = Size(dst.width() * scaleFactor, dst.height() * scaleFactor)
        Imgproc.resize(dst, dst, finalSize)
    }
    return dst
}


@IntDef(
    value = [
        Imgproc.THRESH_BINARY,
        Imgproc.THRESH_BINARY_INV,
        Imgproc.THRESH_TRUNC,
        Imgproc.THRESH_TOZERO,
        Imgproc.THRESH_TOZERO_INV,
        Imgproc.THRESH_MASK,
        Imgproc.THRESH_OTSU,
        Imgproc.THRESH_TRIANGLE,
    ]
)
public annotation class ThresholdInt

/**
 * @param colorCode see #ColorConversionCodes
 */
public fun Mat.cvtColor(colorCode: Int): Mat {
    val dst = Mat()
    Imgproc.cvtColor(this, dst, colorCode)
    return dst
}
