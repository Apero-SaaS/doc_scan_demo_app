package com.apero.app.poc_ml_docscan.scan.api

import com.apero.app.poc_ml_docscan.scan.api.model.Corners

public interface SortContoursUseCase {
    public suspend operator fun invoke(corners: Corners): Corners
}
