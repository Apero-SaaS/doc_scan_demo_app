package com.apero.app.poc_ml_docscan.scan.api

import android.graphics.Bitmap
import arrow.core.Either
import com.apero.app.poc_ml_docscan.scan.common.model.Offset
import com.apero.app.poc_ml_docscan.scan.common.model.Size
import com.apero.app.poc_ml_docscan.scan.api.model.Corners
import com.apero.app.poc_ml_docscan.scan.api.model.Point2F
import com.apero.app.poc_ml_docscan.scan.api.model.SensorRotationDegrees
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface FindPaperSheetContoursRealtimeUseCase {

    data class Contours(
        val corners: Corners?,
        val inferTime: Duration,
        val findContoursTime: Duration,
        val debugMask: Bitmap?,
        /**
         * output dimension of TfLite model
         */
        val outputSize: Size,
    )

    val modelReady: StateFlow<Boolean>

    /**
     * @param debug if true, this will return [Contours] with debug [Contours.debugMask]
     */
    suspend operator fun invoke(
        bitmap: Bitmap,
        degrees: SensorRotationDegrees,
        debug: Boolean = false,
    ): Either<Exception, Contours>
}

fun FindPaperSheetContoursRealtimeUseCase.Contours.normalizeToSize(
    originalSize: Size,
): FindPaperSheetContoursRealtimeUseCase.Contours {
    val contourSize = outputSize
    val scaleX = originalSize.width / contourSize.width
    val scaleY = originalSize.height / contourSize.height

    val newCorners = corners
        ?.points
        ?.map { (x, y) ->
            Point2F(x * scaleX, y * scaleY).coerceIn(originalSize)
        }
        ?.let(::Corners)

    return this.copy(
        outputSize = originalSize,
        corners = newCorners,
    )
}

internal fun Offset.coerceIn(size: Size): Offset {
    return Offset(
        x.coerceIn(0f, size.width),
        y.coerceIn(0f, size.height),
    )
}
