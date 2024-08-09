package com.apero.app.poc_ml_docscan.scan.common.model

import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.io.File

/**
 * this could be in internal (fileDir) or cache (cacheDir) Image
 */
@Serializable
@Parcelize
@JvmInline
public value class InternalImage(public val path: String) : Parcelable {
    @Deprecated("don't know how to handle this. source code compilable & backward compatible only")
    public constructor(uri: Uri) : this(uri.toString())
}

public val InternalImage.file: File get() = File(path)

public fun File.toInternalImage(): InternalImage = InternalImage(absolutePath)

/* TODO 11/01/2024 make it cached by property delegate */
/**
 * WARN: this api does not consider bitmap [rotation]. So in case of a bitmap [size] is 100x200 and
 * [rotation] is 90 degrees. We have to rotate [rotation] to get it's true size
 */
@Deprecated("this api does not consider bitmap rotation", ReplaceWith("trueSize"))
public val InternalImage.size: Size
    get() {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        BitmapFactory.decodeFile(path, options)

        return Size(options.outWidth.toFloat(), options.outHeight.toFloat())
    }

private const val HALF_ROTATION = 180

public val InternalImage.trueSize: Size
    get() {
        val bitmapSize = size
        return if (rotation % HALF_ROTATION == 0) {
            bitmapSize
        } else {
            Size(bitmapSize.height, bitmapSize.width)
        }
    }

/**
 * forked from camera/camera-core/src/main/java/androidx/camera/core/impl/utils/Exif.java
 */
public val InternalImage.rotation: Int
    get() {
        val orientation = ExifInterface(path)
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        return when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> 0
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
            ExifInterface.ORIENTATION_TRANSPOSE -> 270
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_TRANSVERSE -> 90
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> 0
            else -> 0
        }
    }
