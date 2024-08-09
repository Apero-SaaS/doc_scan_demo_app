package com.apero.core.scan

import com.apero.core.scan.model.Corners
import org.koin.core.annotation.Factory
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

@Factory
internal class ComputeContourAreaUseCaseImpl : ComputeContourAreaUseCase {
    override suspend fun invoke(corners: Corners): Float {
        val points = corners.points
            .map {
                Point(it.x.toDouble(), it.y.toDouble())
            }

        val matOfPoint2f = MatOfPoint2f()
        matOfPoint2f.fromList(points)
        val contourArea = Imgproc.contourArea(matOfPoint2f)
        return contourArea.toFloat()
    }
}
