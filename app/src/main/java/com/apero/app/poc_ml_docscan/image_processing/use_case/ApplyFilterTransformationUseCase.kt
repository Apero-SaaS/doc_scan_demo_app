package com.apero.app.poc_ml_docscan.image_processing.use_case

import arrow.core.Either
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.FilterTransformation

public interface ApplyFilterTransformationUseCase {
    public suspend operator fun invoke(
        bitmap: BitmapIR,
        filterTransformation: FilterTransformation,
    ): Either<Exception, BitmapIR>
}
