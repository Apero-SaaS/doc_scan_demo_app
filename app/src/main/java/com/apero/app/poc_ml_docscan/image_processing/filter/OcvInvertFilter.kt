package com.apero.app.poc_ml_docscan.image_processing.filter

import arrow.core.Either
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.computeOcv
import com.apero.app.poc_ml_docscan.utils.minus
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.annotation.Single
import org.opencv.core.Mat
import org.opencv.core.Scalar


@Single
internal class OcvInvertFilter : ImageFilter {
    override suspend fun invoke(bitmapIR: BitmapIR): Either<Exception, BitmapIR> {
        return Either.catchOrThrow {
            bitmapIR.computeOcv(::execute)
        }
    }

    @VisibleForTesting
    fun execute(originalImg: Mat): Mat {
        return Mat(
            originalImg.rows(),
            originalImg.cols(),
            originalImg.type(),
            Scalar(255.0, 255.0, 255.0, 255.0)
        ) - originalImg
    }
}
