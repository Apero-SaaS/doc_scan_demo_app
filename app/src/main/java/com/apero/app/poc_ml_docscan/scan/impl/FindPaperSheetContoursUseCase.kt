package com.apero.core.scan

import arrow.core.Either
import com.apero.core.scan.model.Corners
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

internal interface FindPaperSheetContoursUseCase {
    suspend operator fun invoke(tensorBuffer: TensorBuffer): Either<Exception, Corners?>
}
