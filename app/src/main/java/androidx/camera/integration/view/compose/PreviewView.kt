package androidx.camera.integration.view.compose

import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.ImplementationMode.PERFORMANCE
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalAnimationApi::class)
@Composable
inline fun AnimatedVisibilityScope.PreviewView(
    modifier: Modifier = Modifier,
    state: CameraControllerState = rememberCameraControllerState(),
    implementationMode: PreviewView.ImplementationMode = PERFORMANCE,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FIT_CENTER,
) {
    if (transition.targetState == EnterExitState.PostExit) {
        Spacer(modifier = modifier)
    } else {
        InternalPreviewView(
            state = state,
            implementationMode = implementationMode,
            scaleType = scaleType,
            modifier = modifier
        )
    }
}

@Suppress("NOTHING_TO_INLINE", "unused")
@Composable
inline fun PreviewView(
    modifier: Modifier = Modifier,
    state: CameraControllerState = rememberCameraControllerState(),
    implementationMode: PreviewView.ImplementationMode = PERFORMANCE,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FIT_CENTER,
) {
    InternalPreviewView(
        state = state,
        implementationMode = implementationMode,
        scaleType = scaleType,
        modifier = modifier
    )
}

@PublishedApi
@Composable
internal fun InternalPreviewView(
    state: CameraControllerState,
    implementationMode: PreviewView.ImplementationMode,
    scaleType: PreviewView.ScaleType,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .background(Color.Gray)
                .drawBehind {
                    drawLine(Color.Black, Offset.Zero, Offset(size.width, size.height))
                    drawLine(Color.Black, Offset(size.width, 0f), Offset(0f, size.height))
                }
        ) {
            BasicText(text = "PreviewView")
        }
        return
    }
    AndroidView(
        factory = {
            PreviewView(it)
        },
        update = {
            it.scaleType = scaleType
            it.implementationMode = implementationMode
            it.controller = state.cameraController
        },
        onRelease = {
            state.cameraController.unbind()
        },
        modifier = modifier
    )
}
