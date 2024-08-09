package androidx.camera.integration.view.compose

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraEffect
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asFlow
import androidx.work.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.yield

@SuppressLint("RestrictedApi")
@Composable
fun rememberCameraControllerState(
    context: Context = LocalContext.current,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    flashMode: FlashMode = FlashMode.OFF,
): CameraControllerState {
    val inspectionMode = LocalInspectionMode.current
    val flashModeState = rememberSaveable {
        mutableStateOf(flashMode)
    }
    val state = remember(context) {
        val cameraController = if (inspectionMode) {
            null
        } else {
            LifecycleCameraController(context)
                .apply {
                    this.cameraSelector = cameraSelector
                }
        }
        CameraControllerState(cameraController, flashModeState)
    }

    if (inspectionMode) return state

    val cameraState by state.cameraState

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(state.flashMode, cameraState) {
        if (cameraState.type != CameraState.Type.OPEN) return@LaunchedEffect

        val hasFlashUnit = state.cameraController.cameraInfo?.hasFlashUnit() == true
        val imageCaptureFlashMode = state.cameraController.imageCaptureFlashMode

        if (hasFlashUnit) {
            state.cameraController.imageCaptureFlashMode = state.flashMode.mode
            state.cameraController.cameraControl
                ?.enableTorch(state.flashMode == FlashMode.ON)
                ?.await()
        }
    }
    LaunchedEffect(state.imageEffect, state.surfaceEffect) {
        val effects = mutableSetOf<CameraEffect>()
        var surfaceEffectTarget = 0
        if (state.surfaceEffect != null) {
            surfaceEffectTarget = CameraEffect.PREVIEW or CameraEffect.VIDEO_CAPTURE
        }
        if (state.imageEffect != null) {
            surfaceEffectTarget = surfaceEffectTarget or CameraEffect.IMAGE_CAPTURE
        }
        if (surfaceEffectTarget != 0) {
            effects.add(state.surfaceEffect!!)
        }
        if (state.imageEffect != null) {
            effects.add(state.imageEffect!!)
        }
        state.cameraController.setEffects(effects)
    }
    LaunchedEffect(lifecycleOwner, state.cameraController) {
        state.cameraController.bindToLifecycle(lifecycleOwner)
    }
    return state
}

@SuppressLint("RestrictedApi")
@Stable
class CameraControllerState(
    /**
     * null when in InspectionMode
     */
    cameraController: LifecycleCameraController?,
    flashMode: MutableState<FlashMode>,
) {
    val cameraController: LifecycleCameraController by lazy { cameraController!! }

    var flashMode by flashMode

    val cameraState: State<CameraState>
        @Composable
        get() = produceState(
            initialValue = CameraState.create(CameraState.Type.PENDING_OPEN),
            producer = {
                while (cameraController.cameraInfo == null) {
                    delay(10L)
                    yield()
                }

                cameraController.cameraInfo!!
                    .cameraState
                    .asFlow()
                    .collectLatest {
                        value = it
                    }
            }
        )

    var imageEffect by mutableStateOf<CameraEffect?>(null)
    var surfaceEffect by mutableStateOf<CameraEffect?>(null)

    val sensorRotationDegrees
        @Composable
        get() = produceState(initialValue = 0) {
            while (true) {
                delay(50L)
                value = cameraController.cameraInfo?.sensorRotationDegrees ?: 0
            }
        }
}
