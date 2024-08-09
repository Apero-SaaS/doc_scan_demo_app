package com.apero.app.poc_ml_docscan.scan.impl

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.MainThread
import androidx.collection.LruCache
import arrow.core.Either
import arrow.core.raise.either
import com.apero.app.poc_ml_docscan.ml.DocSeg
import com.apero.app.poc_ml_docscan.scan.api.FindPaperSheetContoursRealtimeUseCase
import com.apero.app.poc_ml_docscan.scan.common.model.Size
import com.apero.core.scan.model.SensorRotationDegrees
import com.apero.app.poc_ml_docscan.scan.common.util.AnalyticsReporter
import com.apero.app.poc_ml_docscan.scan.common.util.traceAsync
import com.google.android.gms.dynamite.DynamiteModule
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.android.gms.tflite.java.TfLite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.DequantizeOp
import org.tensorflow.lite.support.image.ColorSpaceType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import timber.log.Timber
import kotlin.time.TimeSource
import kotlin.time.measureTimedValue

private typealias DocSegModel = DocSeg

internal class FindPaperSheetContoursRealtimeUseCaseImpl(
    private val context: Context,
    private val findPaperSheetContoursUseCase: FindPaperSheetContoursUseCase,
    private val dispatcher: CoroutineDispatcher,
    private val analyticsReporter: AnalyticsReporter,
    scope: CoroutineScope,
) : FindPaperSheetContoursRealtimeUseCase {

    private val initialedInstant = TimeSource.Monotonic.markNow()

    private val gpuDelegateAvailable: Deferred<Boolean> by lazy {
        TfLiteGpu.isGpuDelegateAvailable(context)
            .asDeferred()
    }

    @get:MainThread
    val initializeTfLitePlayServicesRuntime = scope.async(
        context = Dispatchers.Main,
        start = CoroutineStart.LAZY,
    ) {
        val optionsBuilder = TfLiteInitializationOptions.builder()
        optionsBuilder.setEnableGpuDelegateSupport(gpuDelegateAvailable.await())

        Either.catchOrThrow<DynamiteModule.LoadingException, Unit> {
            TfLite.initialize(context, optionsBuilder.build())
                .await()
        }
    }

    private val _modelReady = MutableStateFlow(false)
    override val modelReady: StateFlow<Boolean> = _modelReady.asStateFlow()

    private val model = scope.async(
        context = Dispatchers.Default,
        start = CoroutineStart.LAZY,
    ) {
        val device = if (gpuDelegateAvailable.await()) Model.Device.CPU else Model.Device.CPU
        /* TODO 24/12/2023 collect analytics information about model download & startup time */
        either {
            initializeTfLitePlayServicesRuntime.await()
                .bind()
            Timber.d("Creating DocSegModel, $device")
            DocSeg.newInstance(
                context,
                Model.Options.Builder()
                    .setDevice(device)
                    .setTfLiteRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY)
                    .setNumThreads(4)
                    .build()
            )
                .also {
                    Timber.d("Created DocSegModel")
                    analyticsReporter.reportEvent(
                        "time_start_model",
                        "duration_ms" to initialedInstant.elapsedNow().inWholeMilliseconds.toString(),
                    )
                    _modelReady.value = true
                }
        }
    }

    private val inputImageProcessorCache = LruCache<SensorRotationDegrees, ImageProcessor>(15)

    private val outputImageProcessor by lazy {
        TensorProcessor.Builder()
            .add(DequantizeOp(0f, 225f))
            .build()
    }

    override suspend fun invoke(
        bitmap: Bitmap,
        degrees: SensorRotationDegrees,
        debug: Boolean,
    ): Either<Exception, FindPaperSheetContoursRealtimeUseCase.Contours> = withContext(dispatcher) {
        compute(bitmap, degrees, debug)
    }

    private suspend fun compute(
        bitmap: Bitmap,
        degrees: SensorRotationDegrees,
        debug: Boolean,
    ): Either<Exception, FindPaperSheetContoursRealtimeUseCase.Contours> = traceAsync("invoke") {
        either {
            val (inferResult, inferTime) = measureTimedValue {
                val model = model.await()
                    .bind()
                infer(model, bitmap, degrees)
            }

            val normalizedBuffer = normalizeOutput(inferResult)

            val debugBitmap = if (debug) getDebugMask(normalizedBuffer) else null
            Timber.d("debugBitmap $debugBitmap")
            val (contourResult, findContoursTime) = measureTimedValue {
                /* TODO 23/12/2023 try switch to inferResult */
                findPaperSheetContoursUseCase(normalizedBuffer)
                    .bind()
            }

            val result = FindPaperSheetContoursRealtimeUseCase.Contours(
                corners = contourResult,
                inferTime = inferTime,
                findContoursTime = findContoursTime,
                debugMask = debugBitmap,
                outputSize = Size(
                    normalizedBuffer.shape[1].toFloat(),
                    normalizedBuffer.shape[2].toFloat(),
                )
            )

            result
        }
    }

    /**
     * @return raw infer output from tflite model
     */
    private suspend fun infer(
        model: DocSegModel,
        bitmap: Bitmap,
        sensorRotationDegrees: SensorRotationDegrees,
    ): TensorBuffer = traceAsync("infer") {
        check(initializeTfLitePlayServicesRuntime.isCompleted)

        val inputImageProcessor = getInputImageProcessor(sensorRotationDegrees)
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val input = inputImageProcessor.process(tensorImage)

        model.process(input)
            .maskAsTensorBuffer
    }

    private suspend fun normalizeOutput(
        output: TensorBuffer,
    ): TensorBuffer = traceAsync("normalizeOutput") {
        outputImageProcessor.process(output)
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun getDebugMask(
        normalizedBuffer: TensorBuffer,
    ): Bitmap {
        val image = TensorImage()
        image.load(normalizedBuffer, ColorSpaceType.GRAYSCALE)
        return image.bitmap
    }

    private fun getInputImageProcessor(rotationDegrees: SensorRotationDegrees): ImageProcessor =
        inputImageProcessorCache[rotationDegrees]
            ?: ImageProcessor.Builder()
                .add(Rot90Op(-rotationDegrees / 90))
                .build()
                .also {
                    inputImageProcessorCache.put(rotationDegrees, it)
                }
}
