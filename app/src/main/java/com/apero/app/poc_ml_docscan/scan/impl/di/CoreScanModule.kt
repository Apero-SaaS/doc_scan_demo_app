package com.apero.app.poc_ml_docscan.scan.impl.di

import android.content.Context
import com.apero.app.poc_ml_docscan.scan.api.FindPaperSheetContoursRealtimeUseCase
import com.apero.app.poc_ml_docscan.scan.api.SortContoursUseCase

import com.apero.app.poc_ml_docscan.scan.impl.ComputeContourAreaUseCase
import com.apero.app.poc_ml_docscan.scan.impl.FindPaperSheetContoursRealtimeUseCaseImpl
import com.apero.app.poc_ml_docscan.scan.impl.FindPaperSheetContoursUseCase
import com.apero.app.poc_ml_docscan.scan.impl.StableFindPaperSheetContoursUseCaseImpl
import com.apero.app.poc_ml_docscan.scan.common.util.AnalyticsReporter
import com.apero.app.poc_ml_docscan.scan.impl.SortContoursUseCaseImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@ComponentScan("com.apero.app.poc_ml_docscan")
public class CoreScanModule {
    /**
     * Cannot use [@Single][Single] in DocumentSegmentationUseCaseImpl,
     * see [Issue link](https://github.com/InsertKoinIO/koin/issues/1742)
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Single
    internal fun provideFindPaperSheetContoursRealtimeUseCase(
        context: Context,
        findPaperSheetContoursUseCase: FindPaperSheetContoursUseCase,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
        computeContourAreaUseCase: ComputeContourAreaUseCase,
        sortContoursUseCase: SortContoursUseCase,
        scope: CoroutineScope,
        analyticsReporter: AnalyticsReporter,
    ): FindPaperSheetContoursRealtimeUseCase {
        val limitParallelism = defaultDispatcher.limitedParallelism(1)
        val delegate = FindPaperSheetContoursRealtimeUseCaseImpl(
            context, findPaperSheetContoursUseCase, limitParallelism, scope
        )
        return StableFindPaperSheetContoursUseCaseImpl(
            delegate = delegate,
            computeContourAreaUseCase = computeContourAreaUseCase,
            sortContoursUseCase = sortContoursUseCase,
            coveragePercentageThreshold = 0.1f,
            dispatcher = defaultDispatcher,
        )
    }

    @Factory
    internal fun provideSortContoursUseCase(
    ): SortContoursUseCase = SortContoursUseCaseImpl()
}
