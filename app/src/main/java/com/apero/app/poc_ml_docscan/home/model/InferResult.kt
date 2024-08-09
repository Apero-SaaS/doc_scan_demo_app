package com.apero.app.poc_ml_docscan.home.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import com.apero.app.poc_ml_docscan.scan.api.model.Corners
import com.apero.core.scan.model.SensorRotationDegrees

@Immutable
data class InferResult(
    val corners: Corners?,
    val bitmap: Bitmap?,
    val originalSize: Size,
    val rotationDegree: SensorRotationDegrees,
    val outputSize: Size,
    val index: Int = 0,
)

fun com.apero.app.poc_ml_docscan.scan.common.model.Size.toComposeSize(): Size {
    return Size(width, height)
}