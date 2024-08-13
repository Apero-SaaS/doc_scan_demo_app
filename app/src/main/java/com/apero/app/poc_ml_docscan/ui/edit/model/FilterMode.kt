package com.apero.app.poc_ml_docscan.ui.edit.model

import com.apero.app.poc_ml_docscan.image_processing.model.FilterTransformation


enum class FilterMode {
    ORIGINAL,
    MAGIC,
    NO_SHADOW,
    GREYSCALE,
    INVERT,
    BLACK_N_WHITE,
    ;

    fun mapToTransformation(): FilterTransformation? {
        return when (this) {
            ORIGINAL -> null
            INVERT -> FilterTransformation.INVERT
            GREYSCALE -> FilterTransformation.GREYSCALE
            BLACK_N_WHITE -> FilterTransformation.BLACK_N_WHITE
            NO_SHADOW -> FilterTransformation.NO_SHADOW
            MAGIC -> FilterTransformation.MAGIC
        }
    }

    companion object {
        fun getFromTransformation(filterTransformation: FilterTransformation?): FilterMode {
            return when (filterTransformation) {
                FilterTransformation.INVERT -> INVERT
                FilterTransformation.GREYSCALE -> GREYSCALE
                FilterTransformation.BLACK_N_WHITE -> BLACK_N_WHITE
                FilterTransformation.NO_SHADOW -> NO_SHADOW
                FilterTransformation.MAGIC -> MAGIC
                null -> ORIGINAL
            }
        }

        fun getDefault(): FilterMode = MAGIC
    }
}