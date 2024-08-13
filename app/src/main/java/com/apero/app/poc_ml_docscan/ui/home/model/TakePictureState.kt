package com.apero.app.poc_ml_docscan.ui.home.model

import android.graphics.Bitmap
import androidx.camera.core.ImageCaptureException
import androidx.compose.runtime.Stable
import java.io.File

/**
 * [Requesting] -> [Saving] -> [Saved]
 */
@Stable
sealed class TakePictureState {
    abstract val target: File
    abstract val previewBitmap: Bitmap?

    data class Requesting(
        override val target: File,
        override val previewBitmap: Bitmap?,
    ) : TakePictureState()

    data class Saving(
        override val target: File,
        override val previewBitmap: Bitmap,
    ) : TakePictureState()

    data class Saved(
        override val target: File,
        override val previewBitmap: Bitmap?,
    ) : TakePictureState()

    data class Error(val exception: ImageCaptureException) : TakePictureState() {
        override val target: File
            get() = throw UnsupportedOperationException("querying target File makes no sense")
        override val previewBitmap: Bitmap? = null
    }
}
