package com.apero.app.poc_ml_docscan.home

import android.Manifest
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.apero.app.poc_ml_docscan.databinding.ActivityHomeBinding
import com.apero.app.poc_ml_docscan.permission.manager.impl.SinglePermissionWithSystemManager
import com.apero.app.poc_ml_docscan.permission.queue.PermissionNextAction
import com.apero.app.poc_ml_docscan.permission.queue.PermissionQueue

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateUI(savedInstanceState)
    }

    private var analysisImageProxy: ImageProxy? = null
//    private val documentSegmentationUseCase by inject<FindPaperSheetContoursRealtimeUseCase>()
    private var sensorRotation = 90
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

    private fun updateUI(savedInstanceState: Bundle?) {
        requestCameraPermission()
        viewModel.updateSensorRotation(cameraController)
        bindCameraWithLifecycle()
        setupWithPreview()
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
//        cameraController.setupImageAnalyzer { imageProxy, index ->
//            val bitmap = imageProxy.toBitmap()
//            val originalSize = Size(
//                imageProxy.width.toFloat(),
//                imageProxy.height.toFloat()
//            )
//            documentSegmentationUseCase(bitmap, sensorRotation, false)
//                .map {
//                    val (corners, inferTime, findContoursTime, debugMask) = it
//                    Timber.d("inferTime: $inferTime, findContoursTime: $findContoursTime, corners: $corners, debugMask: $debugMask")
//                    InferResult(
//                        corners = it.corners,
//                        bitmap = it.debugMask,
//                        originalSize = originalSize,
//                        rotationDegree = sensorRotation,
//                        outputSize = it.outputSize.toComposeSize(),
//                        index = index,
//                    )
//                }
//                .onLeft {
//                    Timber.e(it, "ImageAnalysisAnalyzerEffect")
//                }
//                .getOrElse {
//                    InferResult(
//                        corners = null,
//                        bitmap = null,
//                        originalSize = originalSize,
//                        rotationDegree = sensorRotation,
//                        outputSize = Size.Zero,
//                        index = index,
//                    )
//                }.let {
//                    Timber.d("InferResult: $it")
//                    scanViewModel.updateInferResult(it)
//                }
//        }
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
}