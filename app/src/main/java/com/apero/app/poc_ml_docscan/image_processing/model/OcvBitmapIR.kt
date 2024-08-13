package com.apero.app.poc_ml_docscan.image_processing.model

import android.graphics.Bitmap
import com.apero.app.poc_ml_docscan.utils.toBitmap
import com.apero.app.poc_ml_docscan.utils.toMat
import org.opencv.core.Mat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal data class OcvBitmapIR(val mat: Mat) : BitmapIR {
    override val bitmap: Bitmap by weakRefLazy(this) {
        mat.toBitmap()
    }
}

internal fun BitmapIR.toOpencv(): OcvBitmapIR {
    return when (this) {
        is OcvBitmapIR -> this
        else -> OcvBitmapIR(bitmap)
    }
}

internal fun OcvBitmapIR(bitmap: Bitmap): OcvBitmapIR {
    return OcvBitmapIR(bitmap.toMat())
}

@OptIn(ExperimentalContracts::class)
internal inline fun BitmapIR.computeOcv(block: (input: Mat) -> Mat): OcvBitmapIR {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return OcvBitmapIR(block(toOpencv().mat))
}
