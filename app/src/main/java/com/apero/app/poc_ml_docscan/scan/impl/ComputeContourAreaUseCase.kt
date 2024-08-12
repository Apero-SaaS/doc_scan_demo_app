package com.apero.app.poc_ml_docscan.scan.impl

import com.apero.app.poc_ml_docscan.scan.api.model.Corners

interface ComputeContourAreaUseCase {

    suspend operator fun invoke(corners: Corners): Float
}
