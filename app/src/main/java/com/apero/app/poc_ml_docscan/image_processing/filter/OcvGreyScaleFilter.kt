package com.apero.app.poc_ml_docscan.image_processing.filter

import arrow.core.Either
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.computeOcv
import com.apero.app.poc_ml_docscan.utils.cvtColor
import org.koin.core.annotation.Single
import org.opencv.imgproc.Imgproc

@Single
internal class OcvGreyScaleFilter : ImageFilter {
    override suspend fun invoke(bitmapIR: BitmapIR): Either<Exception, BitmapIR> {
        return Either.catchOrThrow {
            bitmapIR.computeOcv {
                it.cvtColor(Imgproc.COLOR_BGR2GRAY)
            }
        }
    }
}
