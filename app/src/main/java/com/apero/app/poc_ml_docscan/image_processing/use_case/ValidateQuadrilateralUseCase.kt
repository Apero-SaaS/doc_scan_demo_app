package com.apero.app.poc_ml_docscan.image_processing.use_case

import com.apero.app.poc_ml_docscan.scan.common.model.Offset
import kotlin.math.abs

public class ValidateQuadrilateralUseCase {

    public operator fun invoke(vertices: List<Offset>): Boolean {
        // The quadrilateral must have four vertices
        if (vertices.size != 4) return false

        for (i in vertices.indices) {
            val p0 = vertices[i]
            val p1 = vertices[(i + 1) % vertices.size]
            val p2 = vertices[(i + 2) % vertices.size]
            val p3 = vertices[(i + 3) % vertices.size]

            val isInside = isPointInsideTriangle(p0, p1, p2, p3)

            if (isInside) return false
        }

        if (quadrilateralArea(vertices) < MIN_QUADRILATERAL_AREA) return false

        return true
    }

    private fun isPointInsideTriangle(
        point: Offset,
        vertex1: Offset,
        vertex2: Offset,
        vertex3: Offset,
    ): Boolean {
        // Calculate barycentric coordinates
        val denominator =
            ((vertex2.y - vertex3.y) * (vertex1.x - vertex3.x) +
                    (vertex3.x - vertex2.x) * (vertex1.y - vertex3.y))

        val lambda1 = ((vertex2.y - vertex3.y) * (point.x - vertex3.x) +
                (vertex3.x - vertex2.x) * (point.y - vertex3.y)) / denominator

        val lambda2 = ((vertex3.y - vertex1.y) * (point.x - vertex3.x) +
                (vertex1.x - vertex3.x) * (point.y - vertex3.y)) / denominator

        val lambda3 = 1.0 - lambda1 - lambda2

        // Check if point is inside the triangle
        return lambda1 >= 0
                && lambda2 >= 0
                && lambda3 >= 0
                && lambda1 <= 1
                && lambda2 <= 1
                && lambda3 <= 1
    }

    private fun quadrilateralArea(vertices: List<Offset>): Float {
        return triangleArea(vertices[0], vertices[1], vertices[2]) +
                triangleArea(vertices[0], vertices[2], vertices[3])
    }

    private fun triangleArea(p1: Offset, p2: Offset, p3: Offset): Float {
        return abs(
            p1.x * p2.y + p2.x * p3.y + p3.x * p1.y
                    - p2.x * p1.y - p3.x * p2.y - p1.x * p3.y
        ) / 2
    }

    public companion object {
        public const val MIN_QUADRILATERAL_AREA: Float = 10f
    }
}
