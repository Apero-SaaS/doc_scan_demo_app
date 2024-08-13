package com.apero.app.poc_ml_docscan.image_processing.filter

import arrow.core.Either
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.computeOcv
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.annotation.Single
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

@Single
internal class OcvNoShadowProFilter : ImageFilter {
    override suspend fun invoke(bitmapIR: BitmapIR): Either<Exception, BitmapIR> {
        return Either.catchOrThrow {
            bitmapIR.computeOcv(::execute)
        }
    }

    @VisibleForTesting
    fun execute(originalImg: Mat): Mat {
        val rgbPlanes = ArrayList<Mat>()
        Core.split(originalImg, rgbPlanes)

        val resultPlanes = ArrayList<Mat>()
        val resultNormPlanes = ArrayList<Mat>()

        for (plane in rgbPlanes) {
            val dilatedImg = Mat()
            Imgproc.dilate(
                plane,
                dilatedImg,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(7.0, 7.0))
            )

            val bgImg = Mat()
            Imgproc.medianBlur(dilatedImg, bgImg, 21)

            val diffImg = Mat()
            Core.absdiff(plane, bgImg, diffImg)
            Core.subtract(
                Mat.ones(diffImg.size(), diffImg.type()).setTo(Scalar(255.0)),
                diffImg,
                diffImg
            )

            val normImg = Mat()
            Core.normalize(diffImg, normImg, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1)

            resultPlanes.add(diffImg)
            resultNormPlanes.add(normImg)
        }

        val result = Mat()
        Core.merge(resultPlanes, result)

        val resultNorm = Mat()
        Core.merge(resultNormPlanes, resultNorm)
        return resultNorm
    }
}