package com.apero.app.poc_ml_docscan.home.model

import com.documentscan.simplescan.scanpdf.ui.camera.model.ScanModeBadges

data class ScanModeSelector(
    val scanMode: ScanMode,
    val isSelected: Boolean,
    val scanModeBadge: ScanModeBadges = ScanModeBadges.DEFAULT,
)
