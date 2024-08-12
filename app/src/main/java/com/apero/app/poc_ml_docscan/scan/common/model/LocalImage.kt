package com.apero.app.poc_ml_docscan.scan.common.model

import android.net.Uri
import java.io.File

@Deprecated("this definition doesn't describe full meaning")
typealias LocalImage = InternalImage

val LocalImage.androidUri: Uri
    get() = Uri.fromFile(File(path))

@Deprecated(
    "source code compilable & backward compatible only",
    ReplaceWith("androidUri.toString()")
)
inline val LocalImage.uri: String get() = androidUri.toString()
