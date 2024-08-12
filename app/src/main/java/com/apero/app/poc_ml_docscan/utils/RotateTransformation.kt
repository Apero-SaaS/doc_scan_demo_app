package com.apero.app.poc_ml_docscan.utils

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

/* TODO 19/02/2024 move to .ui.utils */
class RotateTransformation(private val degrees: Float) : Transformation {
    override val cacheKey: String
        get() ="${this::class.simpleName}-$degrees"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)

        // Create a new Bitmap with the rotated image
        val rotatedBitmap = Bitmap.createBitmap(input, 0, 0, input.width, input.height, matrix, true)

        // Recycle the original input Bitmap to avoid memory leaks
        if (rotatedBitmap != input) {
            input.recycle()
        }

        return rotatedBitmap
    }
}
