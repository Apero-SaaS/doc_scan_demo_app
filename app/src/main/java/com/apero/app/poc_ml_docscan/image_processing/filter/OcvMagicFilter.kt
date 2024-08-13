package com.apero.app.poc_ml_docscan.image_processing.filter

import arrow.core.Either
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.computeOcv
import org.koin.core.annotation.Single
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * Created by KO Huyn on 18/05/2024.
 */
@Single
internal class OcvMagicFilter : ImageFilter {
    override suspend fun invoke(bitmapIR: BitmapIR): Either<Exception, BitmapIR> {
        return Either.catchOrThrow { bitmapIR.computeOcv(::execute) }
    }

    fun execute(originImg: Mat): Mat {
        if (originImg.channels() >= 3) {

            // READ RGB color image and convert it to HSV
            val channel = Mat()
            Imgproc.cvtColor(originImg, originImg, Imgproc.COLOR_BGR2HSV)

            // Extract the V channel
            Core.extractChannel(originImg, channel, 2)

            // apply the CLAHE algorithm to the L channel
            val clahe = Imgproc.createCLAHE()
            clahe.clipLimit = 1.0
            clahe.apply(channel, channel)

//        // Merge the color planes back into an HSV image
            Core.insertChannel(channel, originImg, 2)

            // Extract the S channel
            Core.extractChannel(originImg, channel, 1)

            // apply the CLAHE algorithm to the S channel
            val clahe2 = Imgproc.createCLAHE()
            clahe2.clipLimit = 1.0
            clahe2.apply(channel, channel)

            // Merge the color planes back into an HSV image
            Core.insertChannel(channel, originImg, 1)

            // convert back to RGB
            Imgproc.cvtColor(originImg, originImg, Imgproc.COLOR_HSV2BGR)

            // Temporary Mat not reused, so release from memory.
            channel.release()
        }
        originImg.convertTo(originImg, -1, 1.0, 40.0)
        return originImg
    }
}