package androidx.camera.integration.view.compose

import androidx.camera.core.ImageCapture
import kotlinx.serialization.Serializable

/**
 * Flash modes for [ImageCapture]
 */
@Serializable
enum class FlashMode(@ImageCapture.FlashMode internal val mode: Int) {
    AUTO(ImageCapture.FLASH_MODE_AUTO),
    OFF(ImageCapture.FLASH_MODE_OFF),
    ON(ImageCapture.FLASH_MODE_ON),
    ;
}
