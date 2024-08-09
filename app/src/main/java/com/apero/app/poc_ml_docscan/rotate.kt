package com.apero.app.poc_ml_docscan

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix()

    matrix.postRotate(degrees)

    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, true)

    val rotatedBitmap = Bitmap.createBitmap(
        scaledBitmap,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height,
        matrix,
        true
    )
    return rotatedBitmap
}
