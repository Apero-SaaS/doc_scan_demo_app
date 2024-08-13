package com.apero.app.poc_ml_docscan.ui.home.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.SizeF
import android.view.View
import androidx.core.content.ContextCompat
import com.apero.app.poc_ml_docscan.R
import com.apero.app.poc_ml_docscan.ui.home.model.InferResult
import com.apero.app.poc_ml_docscan.scan.common.model.Offset
import timber.log.Timber

class OverlayView @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private val points = mutableListOf<PointF>()
    private val animatedPoints = mutableListOf<PointF>()
    private var contoursWidth: Float = 4f
    private var inferResult: InferResult? = null
        set(value) {
            field = value
            calculateMatrixAndPoints()
            invalidate()
        }
    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.primary)
        style = Paint.Style.STROKE
        strokeWidth = contoursWidth
    }

    init {
        startAnimation()
    }

    fun setInferResult(inferResult: InferResult?) = apply {
        this.inferResult = inferResult
    }

    private fun calculateMatrixAndPoints() {
        val width = width.toFloat()
        val height = height.toFloat()
        points.clear()
        val result = inferResult
        if (result?.corners != null) {
            val originalSize =  // size raw image
                if (result.rotationDegree % 180 == 0) {
                    SizeF(result.outputSize.width, result.outputSize.height)
                } else {
                    SizeF(result.outputSize.height, result.outputSize.width)
                }
            val viewSize = SizeF(width, height) // size overlay view
            val scaleX = viewSize.width / originalSize.width
            val scaleY = viewSize.height / originalSize.height
            Timber.d("viewSize:${viewSize}")

            result.corners.points.forEach { point ->
                val mappedPoint = floatArrayOf(point.x, point.y)
                points.add(
                    PointF(
                        mappedPoint[0] * scaleX,
                        mappedPoint[1] * scaleY
                    )
                )
            }
        } else {
            // Default points if no result
            points.addAll(
                listOf(
                    PointF(0f, 0f),
                    PointF(width, 0f),
                    PointF(width, height),
                    PointF(0f, height)
                )
            )
        }

        // For simplicity, we're directly assigning points to animatedPoints
        // In a real implementation, you'd want to animate between the old and new points
        animatedPoints.clear()
        animatedPoints.addAll(points)
    }

    private fun startAnimation() {
        points.forEachIndexed { index, point ->
            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1000
                interpolator = android.view.animation.LinearInterpolator()
                addUpdateListener {
                    val animatedFraction = it.animatedFraction
                    animatedPoints[index].x = point.x + (50 * animatedFraction)
                    animatedPoints[index].y = point.y + (50 * animatedFraction)
                    invalidate()
                }
            }
            animator.start()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        inferResult?.let {
            if (animatedPoints.size >= 4) {
                val path = Path()
                path.moveTo(animatedPoints[0].x, animatedPoints[0].y)
                for (i in 1 until animatedPoints.size) {
                    path.lineTo(animatedPoints[i].x, animatedPoints[i].y)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun scale(originalPoint: Offset): Offset {
        return Offset(
            originalPoint.x * scaleX,
            originalPoint.y * scaleY
        )
    }
}