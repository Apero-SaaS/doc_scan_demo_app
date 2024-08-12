package com.apero.app.poc_ml_docscan.home

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.util.trace
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import arrow.atomic.AtomicInt
import arrow.core.getOrElse
import coil.load
import com.apero.app.poc_ml_docscan.databinding.ActivityHomeBinding
import com.apero.app.poc_ml_docscan.edit.EditActivity
import com.apero.app.poc_ml_docscan.home.model.InferResult
import com.apero.app.poc_ml_docscan.home.model.toComposeSize
import com.apero.app.poc_ml_docscan.permission.manager.impl.SinglePermissionWithSystemManager
import com.apero.app.poc_ml_docscan.permission.queue.PermissionNextAction
import com.apero.app.poc_ml_docscan.permission.queue.PermissionQueue
import com.apero.app.poc_ml_docscan.scan.api.FindPaperSheetContoursRealtimeUseCase
import com.apero.app.poc_ml_docscan.scan.common.model.file
import com.apero.app.poc_ml_docscan.scan.impl.DocSegImpl
import com.apero.app.poc_ml_docscan.utils.RotateTransformation
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.Executors


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateUI()
    }

    private var analysisImageProxy: ImageProxy? = null
    private val documentSegmentationUseCase: FindPaperSheetContoursRealtimeUseCase by lazy {
        DocSegImpl.providerFindPaperSheetContoursRealtimeUseCase(this)
    }
    private var sensorRotation = 0
    val cameraController: LifecycleCameraController by lazy {
        LifecycleCameraController(this).apply {
            this.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            this.imageCaptureTargetSize = CameraController.OutputSize(AspectRatio.RATIO_4_3)
        }
    }

    private val cameraPermissionManager =
        SinglePermissionWithSystemManager(
            this,
            Manifest.permission.CAMERA
        )

    private fun updateUI() {
        requestCameraPermission()
        viewModel.updateSensorRotation(cameraController)
        bindCameraWithLifecycle()
        setupWithPreview()
        handleObserver()
        handleClick()
    }

    private fun setupWithPreview() {
        binding.previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        binding.previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        binding.previewView.controller = cameraController
    }

    private fun bindCameraWithLifecycle() {
        cameraController.bindToLifecycle(this)
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    cameraController.unbind()
                }
            }
        })
        cameraController.setupImageAnalyzer { imageProxy, index ->
            val bitmap = imageProxy.toBitmap()
            val originalSize = Size(
                imageProxy.width.toFloat(),
                imageProxy.height.toFloat()
            )
            documentSegmentationUseCase.invoke(bitmap, sensorRotation, false)
                .map {
                    val (corners, inferTime, findContoursTime, debugMask) = it
                    Timber.d("inferTime: $inferTime, findContoursTime: $findContoursTime, corners: $corners, debugMask: $debugMask")
                    InferResult(
                        corners = it.corners,
                        bitmap = it.debugMask,
                        originalSize = originalSize,
                        rotationDegree = sensorRotation,
                        outputSize = it.outputSize.toComposeSize(),
                        index = index,
                    )
                }
                .onLeft {
                    Timber.e(it, "ImageAnalysisAnalyzerEffect")
                }
                .getOrElse {
                    InferResult(
                        corners = null,
                        bitmap = null,
                        originalSize = originalSize,
                        rotationDegree = sensorRotation,
                        outputSize = Size.Zero,
                        index = index,
                    )
                }.let {
                    Timber.d("InferResult: $it")
                    viewModel.updateInferResult(it)
                }
        }
    }

    private fun handleObserver() {
        viewModel.cameraState
            .map { it.sensorRotationDegrees }
            .onEach {
                sensorRotation = it
            }.launchIn(lifecycleScope)

        viewModel.inferResult
            .map { it }
            .distinctUntilChanged()
            .onEach {
                binding.viewOverlay.setInferResult(it)
            }.launchIn(lifecycleScope)

        viewModel.uiState.map {
            it.capturedImage.firstOrNull { internalImage ->
                internalImage.file.exists() && internalImage.file.length() > 0
            }
        }.onEach {
            binding.ivCollectionCaptured.load(it?.file) {
                size(48, 48)
                crossfade(true)
                transformations(RotateTransformation(sensorRotation.toFloat()))
            }
        }.launchIn(lifecycleScope)
    }

    private fun requestCameraPermission() {
        if (!cameraPermissionManager.isPermissionGranted()) {
            PermissionQueue()
                .enqueue(
                    cameraPermissionManager,
                    PermissionNextAction.NextWhenGranted
                )
                .executePermissions {
                    if (cameraPermissionManager.isPermissionGranted()) {

                    } else {
                        //TODO: back to home
                    }
                }
        }
    }

    private fun handleClick() {
        binding.ivCapture.setOnSuspendClickListener {
            captureImage()
        }
        binding.ivCollectionCaptured.setOnClickListener {
            EditActivity.start(this, viewModel.uiState.value.capturedImage)
        }
    }

    private suspend fun captureImage() {
        viewModel.captureImage(cameraController, analysisImageProxy)
    }

    private fun View.setOnSuspendClickListener(block: suspend () -> Unit) {
        setOnClickListener {
            isEnabled = false
            lifecycleScope.launch {
                block()
                isEnabled = true
            }
        }
    }

    private fun CameraController.setupImageAnalyzer(
        poolSize: Int = 4,
        analyze: suspend (ImageProxy, index: Int) -> Unit,
    ) {
        lifecycleScope.launch {
            val executor = Executors.newScheduledThreadPool(poolSize)
            val index = AtomicInt(0)
            setImageAnalysisAnalyzer(executor) { imageProxy ->
                val cachedToBitmapImageProxy = object : ImageProxy by imageProxy {
                    private val bitmap by lazy { imageProxy.toBitmap() }
                    override fun toBitmap() = bitmap
                }
                runBlocking {
                    trace("CameraControllerState\$ImageAnalysisAnalyzerEffect") {
                        analyze(cachedToBitmapImageProxy, index.getAndIncrement())
                    }
                    // very important!! If missing ImageAnalysisUseCase will not provide new ImageProxy
                    analysisImageProxy = cachedToBitmapImageProxy
                    imageProxy.close()
                }
            }
        }
    }
}