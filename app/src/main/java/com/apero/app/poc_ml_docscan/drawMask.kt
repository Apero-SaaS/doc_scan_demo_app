package com.apero.app.poc_ml_docscan

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.core.graphics.scale
import kotlin.math.roundToInt

fun DrawScope.drawMask(
    sensorRotation: Int,
    analyzeWidthHeight: Size,
    overlayBitmap: Bitmap?,
    textMeasurer: TextMeasurer,
) {
    if (overlayBitmap == null) return

    val (maskWidth, maskHeight) = if (sensorRotation % 180 == 0) {
        analyzeWidthHeight.width.toFloat() to analyzeWidthHeight.height.toFloat()
    } else {
        analyzeWidthHeight.height.toFloat() to analyzeWidthHeight.width.toFloat()
    }
    val (bitmapW, bitmapH) = if (sensorRotation % 180 == 0) {
        overlayBitmap.width.toFloat() to overlayBitmap.height.toFloat()
    } else {
        overlayBitmap.height.toFloat() to overlayBitmap.width.toFloat()
    }

    val maskRatio = maskWidth / maskHeight
    val isHeightDominant = size.maxDimension == size.height

    val scaleRatio = if (isHeightDominant) {
        size.width / maskWidth
    } else {
        size.height / maskHeight
    }

    val (originalW, originalH) = size

    val width = if (isHeightDominant) {
        size.width
    } else {
        maskHeight * scaleRatio / maskRatio
    }
    val height = if (isHeightDominant) {
        1f * analyzeWidthHeight.width * scaleRatio
    } else {
        maskHeight
    }

    val yOffset = if (isHeightDominant) {
        (size.height - height) / 2
    } else {
        0f
    }

    val xOffset = if (isHeightDominant) {
        0f
    } else {
        (size.width - width) / 2
    }

    // drawPath(
    //     Path().apply {
    //         moveTo(size.width, 0f)
    //         lineTo(0f, 0f)
    //         lineTo(0f, size.height)
    //         lineTo(size.width, size.height)
    //     },
    //     Color.Yellow,
    //     style = Stroke(5f)
    // )
    drawText(
        textMeasurer,
        "${size.width},${size.height} \n$width, $height\n $analyzeWidthHeight",
        style = TextStyle(Color.White)
    )
    withTransform(transformBlock = {
        inset(
            left = 0f,
            top = 0f,
            right = size.width - bitmapW,
            bottom = size.height - bitmapH,
        )
        // inset(
        //     left = 0f,
        //     top = 0f,
        //     right = size.width - maskWidth,
        //     bottom = size.height - maskHeight,
        // )
        // scale(
        //     (originalW / maskWidth),
        //     (originalH / maskHeight),
        //     Offset.Zero
        // )
        // translate(xOffset, yOffset)
        scale(
            originalW / bitmapW,
            originalH / bitmapH,
            Offset.Zero
        )
        scale(
            width / originalW,
            height / originalH,
            center,
        )
        rotate(sensorRotation.toFloat(), center)
        // rotate(30f, center)
    }) {
        // drawPath(
        //     Path().apply {
        //         moveTo(size.width, 0f)
        //         lineTo(0f, 0f)
        //         lineTo(0f, size.height)
        //         lineTo(size.width, size.height)
        //     },
        //     Color.Magenta,
        //     style = Stroke(10f)
        // )
        drawIntoCanvas {
            it.nativeCanvas.drawBitmap(
                overlayBitmap,
                0f,
                0f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                }
            )
            overlayBitmap.recycle()
        }
    }
    // overlayBitmap
    //     ?.rotate(sensorRotation.toFloat())
    //     ?.scale(width.roundToInt(), height.roundToInt())
    //     ?.also { overlay ->
    //         drawIntoCanvas {
    //             it.nativeCanvas.drawBitmap(
    //                 overlay,
    //                 xOffset,
    //                 yOffset,
    //                 android.graphics.Paint().apply {
    //                     color = android.graphics.Color.RED
    //                 }
    //             )
    //         }
    //     }
    //     ?.recycle()
}

fun DrawScope.drawMask2(
    sensorRotation: Int,
    analyzeWidthHeight: Size,
    overlayBitmap: Bitmap?,
) {
    if (overlayBitmap == null || overlayBitmap.isRecycled) return

    drawForcePerspective(
        sensorRotation,
        analyzeWidthHeight,
        outputSize = Size(overlayBitmap.width.toFloat(), overlayBitmap.height.toFloat())
    ) {
        drawIntoCanvas {
            it.nativeCanvas.drawBitmap(
                overlayBitmap,
                0f,
                0f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                }
            )
            overlayBitmap.recycle()
        }
    }
}

fun DrawScope.drawForcePerspective(
    sensorRotation: Int,
    inputSize: Size,
    outputSize: Size,
    drawBlock: DrawScope.() -> Unit,
) {

    val (maskWidth, maskHeight) = if (sensorRotation % 180 == 0) {
        inputSize.width to inputSize.height
    } else {
        inputSize.height to inputSize.width
    }
    val (outputWidth, outputHeight) = outputSize
    val (bitmapW, bitmapH) = if (sensorRotation % 180 == 0) {
        outputWidth to outputHeight
    } else {
        outputHeight to outputWidth
    }

    val maskRatio = maskWidth / maskHeight
    val isHeightDominant = size.maxDimension == size.height

    val scaleRatio = if (isHeightDominant) {
        size.width / maskWidth
    } else {
        size.height / maskHeight
    }

    val (originalW, originalH) = size

    val width = if (isHeightDominant) {
        size.width
    } else {
        inputSize.width * scaleRatio
    }
    val height = if (isHeightDominant) {
        1f * inputSize.width * scaleRatio
    } else {
        size.height
    }

    withTransform(
        transformBlock = {
            inset(
                left = 0f,
                top = 0f,
                right = size.width - bitmapW,
                bottom = size.height - bitmapH,
            )
            scale(
                originalW / bitmapW,
                originalH / bitmapH,
                Offset.Zero
            )
            scale(
                width / originalW,
                height / originalH,
                center,
            )
            rotate(sensorRotation.toFloat(), center)
        },
        drawBlock = drawBlock,
    )
}
