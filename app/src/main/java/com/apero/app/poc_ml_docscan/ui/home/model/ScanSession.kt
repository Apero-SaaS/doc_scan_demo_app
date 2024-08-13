package com.apero.app.poc_ml_docscan.ui.home.model

import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
data class ScanSession(
    val id: SessionId,
    val capturedImageFolder: File,
)
