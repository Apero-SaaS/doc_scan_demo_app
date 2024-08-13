package com.apero.app.poc_ml_docscan.image_processing.use_case

import arrow.core.Either
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.CropTransformation

public interface PerspectiveTransformUseCase {
    public suspend operator fun invoke(
        bitmap: BitmapIR,
        cropTransformation: CropTransformation,
    ): Either<Throwable, BitmapIR>
}
