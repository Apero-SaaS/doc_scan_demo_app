package com.apero.app.poc_ml_docscan.home

import androidx.camera.core.CameraUnavailableException
import androidx.camera.core.ImageProxy
import androidx.camera.integration.view.compose.FlashMode
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.ui.util.trace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.await
import arrow.atomic.AtomicInt
import arrow.atomic.update
import com.apero.app.poc_ml_docscan.MyApp
import com.apero.app.poc_ml_docscan.home.model.CaptureMode
import com.apero.app.poc_ml_docscan.home.model.InferResult
import com.apero.app.poc_ml_docscan.home.model.ScanMode
import com.apero.app.poc_ml_docscan.home.model.ScanModeSelector
import com.apero.app.poc_ml_docscan.home.model.TakePictureState
import com.apero.app.poc_ml_docscan.home.model.takePictureAsync
import com.apero.app.poc_ml_docscan.repo.ScanDocumentRepository
import com.apero.app.poc_ml_docscan.repo.sourcer.InternalCapturedImageSource
import com.apero.app.poc_ml_docscan.scan.common.model.InternalImage
import com.apero.app.poc_ml_docscan.scan.common.util.traceAsync
import com.documentscan.simplescan.scanpdf.ui.camera.model.ScanModeBadges
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import kotlin.time.measureTimedValue

class HomeViewModel : ViewModel() {
    private val scanDocumentRepository: ScanDocumentRepository =
        ScanDocumentRepository(InternalCapturedImageSource(MyApp.instance))
    private val activeWithLoading = AtomicInt(0)
    private val _cameraState = MutableStateFlow(CameraUiState())
    val cameraState = _cameraState.asStateFlow()

    private val _inferResult = MutableStateFlow<InferResult?>(null)
    val inferResult = _inferResult.asStateFlow()
    fun updateSensorRotation(cameraController: LifecycleCameraController) {
        viewModelScope.launch {
            while (isActive) {
                cameraController.cameraInfo?.sensorRotationDegrees
                    .also { Timber.d("updateSensorRotation:${it}") }
                    ?.let { rotation ->
                        _cameraState.update { it.copy(sensorRotationDegrees = rotation) }
                    }
                delay(1000L)
            }
        }

    }

    private val _uiState = MutableStateFlow(DocumentScanUiState())
    val uiState = _uiState.asStateFlow()

    private var scanSession = newScanSession()
    private fun newScanSession(lazy: Boolean = true) = viewModelScope.async(
        start = if (lazy) CoroutineStart.LAZY else CoroutineStart.DEFAULT
    ) {
        scanDocumentRepository.newScanSession()
    }

    fun updateInferResult(inferResult: InferResult) {
        _inferResult.update { inferResult }
    }

    suspend fun captureImage(cameraController: CameraController, imageProxy: ImageProxy?) =
        withLoading {
            try {
                cameraController.initializationFuture.await()
            } catch (e: CameraUnavailableException) {
                val exception = IllegalStateException("captureImage initialization failed", e)
                Timber.e(exception)
            }

            traceAsync("captureImage") {
                withContext(
                    context = viewModelScope.coroutineContext,
                ) {
                    val imageFile =
                        scanDocumentRepository.newCaptureImageFile(scanSession.await().id)
                    val (captureStateAndJob, duration) = measureTimedValue {
                        cameraController.takePictureAsync(
                            viewModelScope,
                            imageFile,
                            cameraState.value.sensorRotationDegrees,
                            imageProxy
                        )
                    }
                    val (captureState, job) = captureStateAndJob

                    Timber.v("captureImage takes $duration")

                    val newInternalImage = InternalImage(imageFile.path)
                    //TODO newInternalImage
                    job.join()
                    _uiState.update { it.copy(capturedImage = listOf(newInternalImage) + it.capturedImage) }
                    val finalState = captureState.value
                    if (finalState is TakePictureState.Error) {
                        val exception = Exception(
                            "captureImage() failed due to Lifecycle went to destroy",
                            finalState.exception,
                        )
                        Timber.e(exception)
                        //TODO error
                    }

                }
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun <R> withLoading(
        block: suspend () -> R,
    ) = trace("CameraScanViewModel\$withLoading") {
        try {
            activeWithLoading.update { it + 1 }
            _cameraState.update { it.copy(isLoading = true) }
            block()
        } finally {
            withContext(NonCancellable) {
                val newValue = activeWithLoading
                    .updateAndGet { it - 1 }
                    .coerceAtLeast(0)
                _cameraState.update { it.copy(isLoading = newValue > 0) }
            }
        }
    }
}

data class CameraUiState(
    val captureMode: CaptureMode = CaptureMode.SINGLE,
    val flashMode: FlashMode = FlashMode.OFF,
    val autoCaptureEnable: Boolean = false,
    val sensorRotationDegrees: Int = 0,
    val isLoading: Boolean = false
)

data class DocumentScanUiState(
    val scanMode: ScanMode = ScanMode.DOCUMENTS,
    val listScanMode: List<ScanModeSelector> = buildList {
        add(
            ScanModeSelector(
                ScanMode.TO_TEXT,
                isSelected = false,
                scanModeBadge = ScanModeBadges.PREMIUM
            )
        )
        add(ScanModeSelector(ScanMode.DOCUMENTS, isSelected = true))
        add(
            ScanModeSelector(
                ScanMode.ID_CARD,
                isSelected = false,
                scanModeBadge = ScanModeBadges.NEW
            )
        )
    },
    val capturedImage: List<InternalImage> = emptyList(),
)