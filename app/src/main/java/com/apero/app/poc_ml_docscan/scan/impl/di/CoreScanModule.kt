package com.apero.core.scan.di

import android.content.Context
import com.apero.core.coroutine.di.CoreCoroutineModule
import com.apero.core.scan.ComputeContourAreaUseCase
import com.apero.core.scan.FindPaperSheetContoursRealtimeUseCaseImpl
import com.apero.core.scan.FindPaperSheetContoursRealtimeUseCase
import com.apero.core.scan.FindPaperSheetContoursUseCase
import com.apero.core.scan.SortContoursUseCase
import com.apero.core.scan.SortContoursUseCaseImpl
import com.apero.core.scan.StableFindPaperSheetContoursUseCaseImpl
import com.apero.app.poc_ml_docscan.scan.common.util.AnalyticsReporter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module(
    includes = [
        CoreCoroutineModule::class,
    ]
)
@ComponentScan("com.apero.core.scan")
public class CoreScanModule {
    /**
     * Cannot use [@Single][Single] in DocumentSegmentationUseCaseImpl,
     * see [Issue link](https://github.com/InsertKoinIO/koin/issues/1742)
     */
    @Single
    internal fun provideFindPaperSheetContoursRealtimeUseCase(
        context: Context,
        findPaperSheetContoursUseCase: FindPaperSheetContoursUseCase,
        @Named(CoreCoroutineModule.DEFAULT)
        defaultDispatcher: CoroutineDispatcher,
        computeContourAreaUseCase: ComputeContourAreaUseCase,
        sortContoursUseCase: SortContoursUseCase,
        scope: CoroutineScope,
        analyticsReporter: AnalyticsReporter,
    ): FindPaperSheetContoursRealtimeUseCase {
        val limitParallelism = defaultDispatcher.limitedParallelism(1)
        val delegate = FindPaperSheetContoursRealtimeUseCaseImpl(
            context, findPaperSheetContoursUseCase, limitParallelism, analyticsReporter, scope
        )
        return StableFindPaperSheetContoursUseCaseImpl(
            delegate = delegate,
            computeContourAreaUseCase = computeContourAreaUseCase,
            sortContoursUseCase = sortContoursUseCase,
            coveragePercentageThreshold = 0.1f,
            dispatcher = defaultDispatcher,
            analyticsReporter = analyticsReporter,
        )
    }

    @Factory
    internal fun provideSortContoursUseCase(
    ): SortContoursUseCase = SortContoursUseCaseImpl()
}
