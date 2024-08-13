package com.apero.app.poc_ml_docscan.ui.edit.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.apero.app.poc_ml_docscan.R
import com.apero.app.poc_ml_docscan.ui.home.model.ScanMode

import kotlinx.parcelize.Parcelize

@Parcelize
enum class EditToolType(@DrawableRes val icon: Int, @StringRes val title: Int) : Parcelable {
    RETAKE(icon = R.drawable.ic_retake_tool, title = R.string.text_retake),
    ROTATE(icon = R.drawable.ic_rotate_tool, title = R.string.text_rotate),
    DELETE(icon = R.drawable.ic_delete_tool, title = R.string.text_delete),
    ARRANGE(icon = R.drawable.ic_arrange_tool, title = R.string.text_arrange),
    SIGN(icon = R.drawable.ic_sign_tool, title = R.string.text_sign),
    TO_TEXT(icon = R.drawable.ic_to_text_tool, title = R.string.text_to_text),
    WATERMARK(icon = R.drawable.ic_watermark_tool, title = R.string.text_watermark),
    FILTER(icon = R.drawable.ic_filter_tool, title = R.string.text_filter),
    ADJUST(icon = R.drawable.ic_adjust_tool, title = R.string.text_adjust),
    CROP(icon = R.drawable.ic_crop_tool, title = R.string.text_crop),
    COPY(icon = R.drawable.ic_copy_tool, title = R.string.text_copy),
    ;

    companion object {
        fun getDefaultFocus(scanMode: ScanMode): EditToolType {
            return when (scanMode) {
                ScanMode.DOCUMENTS -> FILTER
                ScanMode.TO_TEXT -> CROP
                ScanMode.ID_CARD -> TODO()
            }
        }
    }
}
