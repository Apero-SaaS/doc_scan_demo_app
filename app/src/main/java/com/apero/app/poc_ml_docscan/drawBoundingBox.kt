package com.apero.app.poc_ml_docscan

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

fun DrawScope.drawBoundingBox(points: Array<Offset>) {
    val isAllSpecified = points.all { it.isSpecified }
    if (!isAllSpecified) return

    val topLeft = points[0]
    val bottomRight = points[1]
//    drawRect(
//        color = Color.Green,
//        topLeft = topLeft,
//        size = Size(
//            bottomRight.x - topLeft.x,
//            bottomRight.y - topLeft.y
//        ),
//        style = Stroke(1f)
//    )

}
