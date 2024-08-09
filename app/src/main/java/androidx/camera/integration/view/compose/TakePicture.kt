package androidx.camera.integration.view.compose

import android.annotation.SuppressLint
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.view.CameraController
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@JvmInline
value class LocalImage(val uri: Uri)

suspend inline fun CameraControllerState.takePicture(into: File) =
    cameraController.takePicture(into)

@SuppressLint("RestrictedApi")
suspend fun CameraController.takePicture(
    into: File,
): Either<ImageCaptureException, LocalImage> = suspendCoroutine { continuation ->
    val outputFileOptions = ImageCapture.OutputFileOptions
        .Builder(into)
        .build()
    val imageSavedCallback = object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            // never null because the ImageCapture.OutputFileOptions is constructed with #Builder(File).
            continuation.resume(LocalImage(outputFileResults.savedUri!!).right())
        }

        override fun onError(exception: ImageCaptureException) {
            continuation.resume(exception.left())
        }
    }
    takePicture(
        outputFileOptions,
        CameraXExecutors.directExecutor(),
        imageSavedCallback,
    )
}
