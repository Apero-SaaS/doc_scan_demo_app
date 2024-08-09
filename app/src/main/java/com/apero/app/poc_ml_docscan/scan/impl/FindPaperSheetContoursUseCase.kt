package com.apero.app.poc_ml_docscan.scan.impl

import arrow.core.Either
import com.apero.app.poc_ml_docscan.scan.api.model.Corners
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

internal interface FindPaperSheetContoursUseCase {
    suspend operator fun invoke(tensorBuffer: TensorBuffer): Either<Exception, Corners?>
}
