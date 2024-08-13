package com.apero.app.poc_ml_docscan.image_processing.use_case

import android.graphics.Matrix
import arrow.core.Nel
import com.apero.app.poc_ml_docscan.scan.common.model.Offset

@Deprecated("draft animate document from original image to transformed. but yet succeed")
public interface FindPerspectiveTransformUseCase {
    public suspend operator fun invoke(src: Nel<Offset>, dst: Nel<Offset>): Matrix
}
