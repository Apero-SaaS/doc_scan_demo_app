package com.apero.app.poc_ml_docscan.image_processing.use_case

import arrow.core.Either
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.ResizeTransformation

/**
 * Created by KO Huyn on 29/03/2024.
 */
public interface ResizeTransformationUseCase {
    public suspend operator fun invoke(
        bitmap: BitmapIR,
        resizeTransformation: ResizeTransformation,
    ): Either<Throwable, BitmapIR>
}