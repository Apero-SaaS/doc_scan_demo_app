package com.apero.app.poc_ml_docscan.image_processing.filter

import arrow.core.Either
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR

public interface ImageFilter {
    public suspend operator fun invoke(bitmapIR: BitmapIR): Either<Exception, BitmapIR>
}
