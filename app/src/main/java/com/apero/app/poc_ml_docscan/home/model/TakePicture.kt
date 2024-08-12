package com.apero.app.poc_ml_docscan.home.model

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.MainThread
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.view.CameraController
import androidx.core.net.toFile
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.apero.app.poc_ml_docscan.scan.api.model.SensorRotationDegrees
import com.apero.app.poc_ml_docscan.scan.common.model.InternalImage
import com.apero.app.poc_ml_docscan.utils.blur
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.measureTimedValue

suspend fun CameraController.takePictureAsync(
    scope: CoroutineScope,
    into: File,
    sensorRotationDegrees: SensorRotationDegrees,
    analysisImageProxy: ImageProxy? = null,
): Pair<StateFlow<TakePictureState>, Job> {
    val state: MutableStateFlow<TakePictureState> =
        MutableStateFlow(
            TakePictureState.Requesting(
                target = into,
                previewBitmap = analysisImageProxy
                    ?.getTakePictureBitmap(sensorRotationDegrees)
                    ?.blur(0.5f, 25)
            )
        )

    val job = scope.launch(Dispatchers.Main) {
        val (imageProxy, durationCapture) = measureTimedValue {
            capturePicture()
                .getOrElse {
                    state.value = TakePictureState.Error(it)
                    return@launch
                }
        }
        val (bitmap, durationBitmap) = measureTimedValue {
            imageProxy.getTakePictureBitmap(sensorRotationDegrees)
        }
        Timber.v("imageProxy.toBitmap takes $durationBitmap, capturePicture takes $durationCapture")
        state.value = TakePictureState.Saving(into, bitmap)

        withContext(Dispatchers.IO) {
            into.parentFile?.mkdirs()
            if (!into.exists()) {
                into.createNewFile()
            }

            imageProxy.use {
                val out = FileOutputStream(into)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
            }
        }
        state.value = TakePictureState.Saved(into, bitmap.takeUnless { it.isRecycled })
    }

    return state.asStateFlow() to job
}

private suspend fun ImageProxy.getTakePictureBitmap(
    sensorRotationDegrees: SensorRotationDegrees,
): Bitmap = withContext(Dispatchers.Default) {
    val fullSensorBitmap = toBitmap()

    val matrix = Matrix()
    matrix.postRotate(imageInfo.rotationDegrees + sensorRotationDegrees.toFloat())

    val cropRect = cropRect

    val cropFullBitmap = cropRect.top == 0 &&
            cropRect.left == 0 &&
            cropRect.bottom == fullSensorBitmap.height &&
            cropRect.right == fullSensorBitmap.width

    val croppedBitmap = if (cropFullBitmap) {
        fullSensorBitmap
    } else {
        Bitmap.createBitmap(
            fullSensorBitmap,
            cropRect.left, cropRect.top,
            cropRect.width(), cropRect.height(),
            matrix,
            false,
        )
    }

    Timber.v(buildString {
        appendLine("imageInfo.rotationDegrees: ${imageInfo.rotationDegrees}")
        appendLine("sensorRotationDegrees: $sensorRotationDegrees")
        appendLine("cropRect: $cropRect")
        appendLine("fullSensorBitmap: ${fullSensorBitmap.width}x${fullSensorBitmap.height}")
        appendLine("croppedBitmap: ${croppedBitmap.width}x${croppedBitmap.height}")
    })

    if (!cropFullBitmap) {
        fullSensorBitmap.recycle()
    }

    croppedBitmap
}

@MainThread
private suspend fun CameraController.capturePicture(): Either<ImageCaptureException, ImageProxy> =
    suspendCoroutine { continuation ->
        val imageSavedCallback = object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                continuation.resume(image.right())
            }

            override fun onError(exception: ImageCaptureException) {
                continuation.resume(exception.left())
            }
        }

        takePicture(
            CameraXExecutors.directExecutor(),
            imageSavedCallback,
        )
    }

@SuppressLint("RestrictedApi")
suspend fun CameraController.takePicture(
    into: File,
): Either<Exception, InternalImage> = suspendCoroutine { continuation ->
    val outputFileOptions = ImageCapture.OutputFileOptions
        .Builder(into)
        .build()
    val imageSavedCallback = object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            // never null because the ImageCapture.OutputFileOptions is constructed with #Builder(File).
            val savedUri = outputFileResults.savedUri!!
            if (savedUri.scheme?.equals("file", ignoreCase = true) == true) {
                val localImage = InternalImage(savedUri.toFile().absolutePath)
                continuation.resume(localImage.right())
            } else {
                throw Exception("$savedUri is illegal!")
            }
        }

        override fun onError(exception: ImageCaptureException) {
            continuation.resume(exception.left())
        }
    }

    try {
        takePicture(
            outputFileOptions,
            CameraXExecutors.directExecutor(),
            imageSavedCallback,
        )
    } catch (e: IllegalStateException) {
        continuation.resume(e.left())
    }
}
