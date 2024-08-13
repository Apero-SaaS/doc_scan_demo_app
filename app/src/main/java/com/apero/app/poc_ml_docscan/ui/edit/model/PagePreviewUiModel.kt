package com.apero.app.poc_ml_docscan.ui.edit.model

import com.apero.app.poc_ml_docscan.image_processing.model.CropTransformation
import com.apero.app.poc_ml_docscan.model.PdfPageId
import com.apero.app.poc_ml_docscan.scan.common.model.InternalImage

data class PagePreviewUiModel(
    val internalImage: InternalImage,
    val imagePreview: InternalImage?,
    val pageId: PdfPageId,
    val filterSelected: FilterMode = FilterMode.ORIGINAL,
    val rotateDegrees: Float = 0f,
    val cropTransformation: CropTransformation? = null,
)