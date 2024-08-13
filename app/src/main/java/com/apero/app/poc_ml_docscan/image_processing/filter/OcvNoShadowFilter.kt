package com.apero.app.poc_ml_docscan.image_processing.filter

import arrow.core.Either
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.computeOcv
import com.apero.app.poc_ml_docscan.utils.absDiff
import com.apero.app.poc_ml_docscan.utils.dilate
import com.apero.app.poc_ml_docscan.utils.medianBlur
import com.apero.app.poc_ml_docscan.utils.minus
import com.apero.app.poc_ml_docscan.utils.normalize
import com.apero.app.poc_ml_docscan.utils.subtract
import com.apero.app.poc_ml_docscan.utils.threshold
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.annotation.Single
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc


@Single
internal class OcvNoShadowFilter : ImageFilter {
    override suspend fun invoke(bitmapIR: BitmapIR): Either<Exception, BitmapIR> {
        return Either.catchOrThrow {
            bitmapIR.computeOcv(::execute)
        }
    }

    @VisibleForTesting
    fun execute(originalImg: Mat): Mat {
        val dilatedImg = originalImg
            .dilate(Mat.ones(Size(17.0, 17.0), CvType.CV_8UC1))

        val bgImg = dilatedImg.medianBlur(71)

        val diffImg = run {
            val diff = originalImg.absDiff(bgImg)
            val dst =
                Mat(
                    diff.rows(),
                    diff.cols(),
                    diff.type(),
                    Scalar(255.0, 255.0, 255.0, 255.0)
                ) - diff
            dst
        }

        val normImg = diffImg.normalize(
            0.0,
            255.0,
            Core.NORM_MINMAX,
            CvType.CV_8UC1,
        )

        val d = 127.0
        val thrImg = normImg
            .subtract(Scalar(d, d, d, 0.0))
            .threshold(230.0 - d, 0.0, Imgproc.THRESH_TRUNC)

        return thrImg.normalize(
            0.0,
            255.0,
            Core.NORM_MINMAX,
            CvType.CV_8UC1
        )
    }
}
