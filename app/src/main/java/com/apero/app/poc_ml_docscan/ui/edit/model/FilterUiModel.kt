package com.apero.app.poc_ml_docscan.ui.edit.model

import com.apero.app.poc_ml_docscan.model.PdfPageId
import com.apero.app.poc_ml_docscan.scan.common.model.InternalImage
/**
 * Created by KO Huyn on 24/07/2024.
 */
data class FilterUiModel(
    val filterMode: FilterMode,
    val imagePreview: InternalImage,
    val pageId: PdfPageId,
    val isChecked: Boolean
)