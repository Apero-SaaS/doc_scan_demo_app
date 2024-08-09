package com.apero.app.poc_ml_docscan.scan.impl

import android.annotation.SuppressLint
import android.util.Log
import arrow.core.Either
import arrow.core.toNonEmptyListOrNull
import com.apero.app.poc_ml_docscan.scan.api.model.Corners
import com.apero.app.poc_ml_docscan.scan.impl.util.toPoint2F
import org.koin.core.annotation.Single
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

@Single
internal class FindPaperSheetContoursUseCaseImpl:
    FindPaperSheetContoursUseCase {

    internal companion object {

        const val TAG = "FindPaperSheetContoursUseCase"
    }

    init {
        System.loadLibrary("opencv_java4")
    }

    /* TODO try to run on Dispatchers.Default */
    @SuppressLint("LogNotTimber")
    override suspend fun invoke(
        tensorBuffer: TensorBuffer,
    ): Either<Exception, Corners?> = Either.catchOrThrow {
        val original = Mat(tensorBuffer.shape[1], tensorBuffer.shape[2], CvType.CV_32FC1)

        val byteBuffer = tensorBuffer.buffer
        byteBuffer.rewind()
        original.put(0, 0, tensorBuffer.floatArray)

        val normalizeMat = Mat()
        original.convertTo(normalizeMat, CvType.CV_8UC1, 255.0)

        val binaryMat = Mat()
        Imgproc.threshold(normalizeMat, binaryMat, 128.0, 255.0, Imgproc.THRESH_BINARY)

        val contours: MutableList<MatOfPoint> = ArrayList()
        val hierarchy = Mat()
        Imgproc.findContours(
            binaryMat,
            contours,
            hierarchy,
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        hierarchy.release()

        val documentCorners: ArrayList<Point> = ArrayList()
        for (contour in contours) {
            val contour2f = MatOfPoint2f()
                .apply {
                    fromList(contour.toList())
                }
            val peri = Imgproc.arcLength(contour2f, true)
            val approx2f = MatOfPoint2f()
            Imgproc.approxPolyDP(contour2f, approx2f, 0.02 * peri, true)

            if (approx2f.total() == 4L) { // Check for quadrilateral
                // Convert approx2f to MatOfPoint for boundingRect
                val approx = MatOfPoint()
                approx2f.convertTo(approx, CvType.CV_32S)

                // Check for the largest quadrilateral (assumed to be the document)
                val rect = Imgproc.boundingRect(approx)
                val matOfPoint = MatOfPoint().apply {
                    fromList(documentCorners)
                }
                val boundingRect = Imgproc.boundingRect(matOfPoint)
                if (documentCorners.isEmpty() || rect.area() > boundingRect.area()) {
                    documentCorners.clear()
                    for (i in 0 until approx.total()) {
                        documentCorners.add(Point(approx[i.toInt(), 0]))
                    }
                }
            }
        }

        Log.d(TAG, "invoke: ${documentCorners.joinToString()}")

        documentCorners
            .map(Point::toPoint2F)
            .toNonEmptyListOrNull()
            ?.let(::Corners)
    }
}
