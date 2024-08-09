package com.apero.app.poc_ml_docscan

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.integration.view.compose.PreviewView
import androidx.camera.integration.view.compose.rememberCameraControllerState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.rememberTextMeasurer
import com.apero.app.poc_ml_docscan.ml.DocSeg
import com.apero.app.poc_ml_docscan.ui.theme.POCMLDocscanTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.DequantizeOp
import org.tensorflow.lite.support.image.ColorSpaceType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import java.util.concurrent.Executors
import kotlin.time.measureTimedValue

typealias DocSegModel = DocSeg

class MainActivity : ComponentActivity() {


    private val model by lazy {
        DocSegModel.newInstance(
            this,
            Model.Options.Builder()
                .setDevice(Model.Device.CPU)
                .setNumThreads(4)
                .build()
        )
    }

    private val tensorImage by lazy {
        TensorImage(DataType.FLOAT32)
    }

    val inputImageProcessor by lazy {
        ImageProcessor.Builder()
            // .add(ResizeOp(MODEL_HEIGHT, MODEL_WIDTH, ResizeOp.ResizeMethod.BILINEAR))
            // .add(NormalizeOp(0f, 255f))
            // .add(QuantizeOp(0f, 0.0f))
            // .add(CastOp(DataType.FLOAT32))
            .build()
    }

    val outputImageProcessor by lazy {
        TensorProcessor.Builder()
            .add(DequantizeOp(0f, 225f))
            .build()
    }

    //    private var overlayBitmap by mutableStateOf<Bitmap?>(null)
    private var overlayBitmap: Bitmap? = null
    private var points by mutableStateOf(arrayOf(Offset.Unspecified, Offset.Unspecified))
    private var extendedPoints by mutableStateOf(arrayOf(Offset.Unspecified, Offset.Unspecified))
    private var isValid: Boolean by mutableStateOf(false)
    private var fps: Double = 0.0
    private val input by lazy {
        readBitmapFromResource(this@MainActivity, R.drawable.img_sample)
    }

    var frame_counter = 0
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            POCMLDocscanTheme {
                val permissionLauncher = rememberPermissionState(
                    Manifest.permission.CAMERA
                ) {
                    Log.d(TAG, "onCreate: ")
                    /* no-op */
                }
                LaunchedEffect(key1 = permissionLauncher.status.isGranted) {
                    if (!permissionLauncher.status.isGranted) {
                        permissionLauncher.launchPermissionRequest()
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (permissionLauncher.status.isGranted) {
                        Box {
                            val state = rememberCameraControllerState()
                            var analyzeWidthHeight by remember {
                                mutableStateOf(Size.Unspecified)
                            }
                            val sensorRotation by state.sensorRotationDegrees
                            DisposableEffect(key1 = state) {
                                state.cameraController.setImageAnalysisAnalyzer(
                                    Executors.newSingleThreadExecutor()
                                ) { imageProxy ->

//                                    frame_counter += 1
//                                    if (frame_counter % 3) {}
                                    analyzeWidthHeight = Size(
                                        imageProxy.width.toFloat(),
                                        imageProxy.height.toFloat()
                                    )
                                    inferenceV1(imageProxy.use { it.toBitmap() })
                                }
                                onDispose {
                                    state.cameraController.clearImageAnalysisAnalyzer()
                                }
                            }
                            PreviewView(
                                state = state,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                            )

                            val textMeasurer = rememberTextMeasurer()
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {

                                if (!isValid) {
                                    Log.d(TAG, "Invalid")
                                    return@Canvas
                                }
                                val overlayBitmap = overlayBitmap
                                if (!analyzeWidthHeight.isSpecified) return@Canvas
                                // drawMask(sensorRotation, analyzeWidthHeight, overlayBitmap, textMeasurer)
                                // drawMask2(sensorRotation, analyzeWidthHeight, overlayBitmap)
                                if (overlayBitmap == null || overlayBitmap.isRecycled) return@Canvas

                                val outputSize = Size(
                                    overlayBitmap.width.toFloat(),
                                    overlayBitmap.height.toFloat()
                                )
                                drawForcePerspective(
                                    sensorRotation = sensorRotation,
                                    inputSize = analyzeWidthHeight,
                                    outputSize = outputSize
                                ) {
                                    drawIntoCanvas {
                                        it.nativeCanvas.drawBitmap(
                                            overlayBitmap,
                                            0f,
                                            0f,
                                            android.graphics.Paint().apply {
                                                color = android.graphics.Color.RED
                                            }
                                        )
                                        overlayBitmap.recycle()
                                    }
                                }

                                drawForcePerspective(
                                    sensorRotation = sensorRotation,
                                    inputSize = analyzeWidthHeight,
                                    outputSize = outputSize
                                ) {
                                    drawBoundingBox(points)

                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Camera permission has not granted"
                        )
                    }
                }
            }
        }
    }

    private fun inferenceV1(
        bitmap: Bitmap,
    ): Unit {
        tensorImage.load(bitmap)
        val newBitmap = tensorImage
        val (outputs, duration) = measureTimedValue {
            model.process(newBitmap)
        }
        Log.d(TAG, "inferenceBitmap: process: $duration")
        val maskImage = outputs.maskAsTensorBuffer
            .let {
                outputImageProcessor.process(it)
            }
        val outputTensorImage = TensorImage()
        outputTensorImage.load(maskImage, ColorSpaceType.GRAYSCALE)
        overlayBitmap = outputTensorImage.bitmap
        val width = overlayBitmap!!.width
        val height = overlayBitmap!!.height

        Log.d(TAG, "Bitmap wight and height - width: $width, - height: $height ")
        val intArray = maskImage.floatArray
        val boxes = findBoundingBox(intArray)

        if (boxes != null) {
            isValid = boxes.isValid
            points = arrayOf(
                Offset(boxes.boundingBox.minX.toFloat(), boxes.boundingBox.minY.toFloat()),
                Offset(boxes.boundingBox.maxX.toFloat(), boxes.boundingBox.maxY.toFloat()),
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Releases model resources if no longer used.
        model.close()
    }
}

const val TAG = "MainActivity.kt"

data class BoundingBox(val minX: Int, val minY: Int, val maxX: Int, val maxY: Int)
data class MaskEdgePoint(val x_points: List<Int>, val y_points: List<Int>)
data class MaskPoints(val boundingBox: BoundingBox, val isValid: Boolean)
