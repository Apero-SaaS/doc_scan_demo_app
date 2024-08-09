package com.apero.app.poc_ml_docscan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun readBitmapFromResource(context: Context, resourceId: Int): Bitmap {
    val bitmapFactory = BitmapFactory.decodeResource(context.resources, resourceId)
    return bitmapFactory
}
