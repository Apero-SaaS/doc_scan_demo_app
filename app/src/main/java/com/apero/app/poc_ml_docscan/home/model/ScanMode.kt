package com.apero.app.poc_ml_docscan.home.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ScanMode : Parcelable {
    DOCUMENTS,
    TO_TEXT,
    ID_CARD
}
