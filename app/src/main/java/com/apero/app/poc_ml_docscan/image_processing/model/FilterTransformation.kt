package com.apero.app.poc_ml_docscan.image_processing.model

import kotlinx.serialization.Serializable

@Serializable
public enum class FilterTransformation : ImageTransformation {
    INVERT,
    GREYSCALE,
    BLACK_N_WHITE,
    NO_SHADOW,
    MAGIC,
    ;

    public fun getTag(): String {
        return when (this) {
            INVERT -> "Invert"
            GREYSCALE -> "Greyscale"
            BLACK_N_WHITE -> "B&W"
            NO_SHADOW -> "No Shadow"
            MAGIC -> "Magic"
        }
    }
}
