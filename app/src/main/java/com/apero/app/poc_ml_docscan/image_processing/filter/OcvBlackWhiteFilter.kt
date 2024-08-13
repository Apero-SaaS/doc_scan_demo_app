package com.apero.app.poc_ml_docscan.image_processing.filter

import arrow.core.Either
import arrow.core.flatMap
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.OcvBitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.computeOcv
import com.apero.app.poc_ml_docscan.utils.cvtColor
import org.koin.core.annotation.Single
import org.opencv.imgproc.Imgproc

@Single
internal class OcvBlackWhiteFilter(private val noShadowFilter: OcvNoShadowProFilter) : ImageFilter {
    override suspend fun invoke(bitmapIR: BitmapIR): Either<Exception, BitmapIR> {
        return noShadowFilter(bitmapIR)
            .flatMap { bitmap ->
                Either.catchOrThrow<Exception, BitmapIR> {
                    execute(bitmap)
                }
            }
    }

    private fun execute(bitmap: BitmapIR): OcvBitmapIR {
        return bitmap.computeOcv {
            it.cvtColor(Imgproc.COLOR_BGR2GRAY)
        }
    }
}
