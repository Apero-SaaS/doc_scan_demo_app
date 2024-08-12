package com.apero.app.poc_ml_docscan.scan.impl.util

import com.apero.app.poc_ml_docscan.scan.common.model.Size
import com.apero.app.poc_ml_docscan.scan.api.model.Point2F

private typealias CvPoint = org.opencv.core.Point
private typealias CvSize = org.opencv.core.Size

internal fun CvPoint.toPoint2F(): Point2F =
    Point2F(x.toFloat(), y.toFloat())

internal fun CvSize.toSize(): Size = Size(width.toFloat(), height.toFloat())
