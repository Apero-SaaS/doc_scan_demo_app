package com.apero.app.poc_ml_docscan

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import java.nio.ByteBuffer

@ColorInt
private fun maskColorsFromByteBuffer(
    byteBuffer: ByteBuffer,
    maskWidth: Int,
    maskHeight: Int,
): IntArray {
    val colors = IntArray(maskWidth * maskHeight)
    for (i in 0 until maskWidth * maskHeight) {
        val backgroundLikelihood = 1 - byteBuffer.float
        if (backgroundLikelihood > 0.9) {
            colors[i] = Color.argb(128, 255, 0, 255)
        } else if (backgroundLikelihood > 0.2) {
            // Linear interpolation to make sure when backgroundLikelihood is 0.2, the alpha is 0 and
            // when backgroundLikelihood is 0.9, the alpha is 128.
            // +0.5 to round the float value to the nearest int.
            val alpha = (182.9 * backgroundLikelihood - 36.6 + 0.5).toInt()
            colors[i] = Color.argb(alpha, 255, 0, 255)
        }
    }
    return colors
}

fun maskToBitmap(mask: ByteBuffer, maskWidth: Int, maskHeight: Int): Bitmap {
    mask.rewind()
    val bitmap = Bitmap.createBitmap(
        maskColorsFromByteBuffer(mask, maskWidth, maskHeight),
        maskWidth,
        maskHeight,
        Bitmap.Config.ARGB_8888
    )
    return bitmap
}
