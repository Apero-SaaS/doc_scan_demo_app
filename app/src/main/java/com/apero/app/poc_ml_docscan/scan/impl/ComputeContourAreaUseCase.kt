package com.apero.core.scan

import com.apero.core.scan.model.Corners

public interface ComputeContourAreaUseCase {

    public suspend operator fun invoke(corners: Corners): Float
}
