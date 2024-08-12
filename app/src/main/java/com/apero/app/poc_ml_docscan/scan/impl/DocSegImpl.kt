package com.apero.app.poc_ml_docscan.scan.impl

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apero.app.poc_ml_docscan.scan.api.FindPaperSheetContoursRealtimeUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.annotation.Single

object DocSegImpl {

    @Single
    fun providerStableFindPaperSheetContoursUseCase(
        context: Context,
        coveragePercentageThreshold: Float = 0.1f,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): FindPaperSheetContoursRealtimeUseCase {
        val delegate = providerFindPaperSheetContoursRealtimeUseCase(context, dispatcher)
        return StableFindPaperSheetContoursUseCaseImpl(
            delegate,
            ComputeContourAreaUseCaseImpl(),
            SortContoursUseCaseImpl(),
            coveragePercentageThreshold,
            dispatcher
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun providerFindPaperSheetContoursRealtimeUseCase(
        context: Context,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): FindPaperSheetContoursRealtimeUseCase {
        val limitParallelism = dispatcher.limitedParallelism(1)
        val scope = CoroutineScope(dispatcher)
        val delegate = FindPaperSheetContoursRealtimeUseCaseImpl(
            context, FindPaperSheetContoursUseCaseImpl(), limitParallelism, scope
        )
        return delegate
    }
}