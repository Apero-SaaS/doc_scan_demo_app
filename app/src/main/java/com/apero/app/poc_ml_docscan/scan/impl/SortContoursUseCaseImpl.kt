package com.apero.app.poc_ml_docscan.scan.impl

import arrow.core.toNonEmptyListOrNull
import com.apero.app.poc_ml_docscan.scan.api.SortContoursUseCase
import com.apero.app.poc_ml_docscan.scan.common.model.Offset
import com.apero.app.poc_ml_docscan.scan.api.model.Corners
import com.apero.app.poc_ml_docscan.scan.impl.util.OpenCvHelper
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import kotlin.math.atan2

internal class SortContoursUseCaseImpl : SortContoursUseCase {
    init {
        OpenCvHelper.loadLibrary()
    }

    override suspend operator fun invoke(corners: Corners): Corners {
        val center = findCenter2(corners)

        val sortedPoints = corners.points
            .sortedWith(compareBy {
                atan2((it.y - center.y), (it.x - center.x))
            })
            .toNonEmptyListOrNull()!!
        return Corners(sortedPoints)
    }

    /**
     * See [issue](https://stackoverflow.com/questions/46333547/center-of-mass-computation-yields-wrong-results-in-opencv)
     */
    @Deprecated("OpenCV implementation is not correct")
    private fun findCenter(corners: Corners): Point {
        val points = corners.points
            .map {
                Point(it.x.toDouble(), it.y.toDouble())
            }

        val matOfPoint = MatOfPoint()
        matOfPoint.fromList(points)

        val moments = Imgproc.moments(matOfPoint)
        val centerX = moments.m10 / moments.m00
        val centerY = moments.m01 / moments.m00
        val center = Point(centerX, centerY)
        return center
    }

    private fun findCenter2(corners: Corners): Point {
        val (sumX, sumY) = corners.points
            .reduce { acc, offset ->
                offset + acc
            }

        val size = corners.points.size
        val center = Point(sumX.toDouble() / size, sumY.toDouble() / size)
        return center
    }
}

private operator fun Offset.plus(other: Offset): Offset = Offset(x + other.x, y + other.y)
