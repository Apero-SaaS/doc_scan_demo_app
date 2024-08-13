package com.apero.app.poc_ml_docscan.ui.home.model

import com.apero.app.poc_ml_docscan.model.ScanModeBadges

data class ScanModeSelector(
    val scanMode: ScanMode,
    val isSelected: Boolean,
    val scanModeBadge: ScanModeBadges = ScanModeBadges.DEFAULT,
)
