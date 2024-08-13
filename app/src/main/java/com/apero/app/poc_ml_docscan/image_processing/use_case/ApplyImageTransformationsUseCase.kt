package com.apero.app.poc_ml_docscan.image_processing.use_case

import arrow.core.Either
import arrow.core.Nel
import com.apero.app.poc_ml_docscan.image_processing.model.BitmapIR
import com.apero.app.poc_ml_docscan.image_processing.model.ImageTransformation
import com.apero.app.poc_ml_docscan.scan.common.model.InternalImage

public interface ApplyImageTransformationsUseCase {

    @Deprecated("prefer use invoke(InternalImage) instead")
    public suspend operator fun invoke(
        bitmap: BitmapIR,
        transforms: Nel<ImageTransformation>,
    ): Either<Throwable, BitmapIR>

    public suspend operator fun invoke(
        source: InternalImage,
        useCacheIfExists: Boolean,
        transforms: Nel<ImageTransformation>,
    ): Either<Throwable, InternalImage>
}
