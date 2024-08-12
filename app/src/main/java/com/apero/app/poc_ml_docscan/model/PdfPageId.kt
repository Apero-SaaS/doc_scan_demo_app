package com.apero.app.poc_ml_docscan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
@Parcelize
@Serializable
@JvmInline
public value class PdfPageId(public val id: String): Parcelable
