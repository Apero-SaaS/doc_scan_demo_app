package com.apero.app.poc_ml_docscan.image_processing.model

import com.apero.app.poc_ml_docscan.scan.common.model.Corners
import com.apero.app.poc_ml_docscan.scan.common.model.Offset
import com.apero.app.poc_ml_docscan.scan.common.model.equalsApproximately
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable

/* FIXME 16/01/2024 this class has the same signature with com.apero.core.data.model.Corners.
*   We should merge them later */
@Serializable
public data class CropTransformation(
    val packedOffsets: PersistentList<Offset>,
) : ImageTransformation {
    public constructor(
        topLeft: Offset, topRight: Offset,
        bottomLeft: Offset, bottomRight: Offset,
    ) : this(
        persistentListOf(
            topLeft, topRight,
            bottomRight, bottomLeft,
        )
    )

    val topLeft: Offset get() = packedOffsets[0]
    val topRight: Offset get() = packedOffsets[1]
    val bottomRight: Offset get() = packedOffsets[2]
    val bottomLeft: Offset get() = packedOffsets[3]

    override fun toString(): String {
        return buildString {
            append("CropTransformation(")
            append("topLeft=$topLeft, ")
            append("topRight=$topRight, ")
            append("bottomLeft=$bottomLeft, ")
            append("bottomRight=$bottomRight)")
        }
    }

    public companion object {
        public fun fromClockwiseOrderedList(offsets: Corners): CropTransformation =
            CropTransformation(offsets.points.toPersistentList())
    }
}

public fun CropTransformation.equalsApproximately(other: CropTransformation): Boolean {
    return packedOffsets.equalsApproximately(other.packedOffsets)
}
