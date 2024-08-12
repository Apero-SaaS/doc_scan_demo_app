package com.apero.app.poc_ml_docscan.image_processing.model

import android.graphics.Bitmap

/**
 * A Bitmap Intermediate Representation that abstracts away the resulting bitmap.
 * In our program, majority of the time, we pass [Bitmap] around UseCases your image computation but
 * we don't actually use [Bitmap] to display it to users. Second, some UseCases don't use [Bitmap]
 * to compute, but use Mat from opencv. Thus, if we still want to use [Bitmap], it will be memory
 * extensive when we want to pass [Bitmap] from an UseCase to another UseCase's [Bitmap] input.
 * Because we have to convert back and ford between [Bitmap] and opencv Mat.
 */
public interface BitmapIR {
    public val bitmap: Bitmap
}
