package com.apero.app.poc_ml_docscan.edit.adapter

import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.apero.app.poc_ml_docscan.base.BaseViewHolder
import com.apero.app.poc_ml_docscan.databinding.ItemPagePreviewBinding
import com.apero.app.poc_ml_docscan.edit.model.PagePreviewUiModel
import com.apero.app.poc_ml_docscan.scan.common.model.androidUri
import timber.log.Timber
import java.io.FileNotFoundException

/**
 * Created by KO Huyn on 25/07/2024.
 */
class EditPagePreviewAdapter :
    ListAdapter<PagePreviewUiModel, BaseViewHolder<ItemPagePreviewBinding>>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PagePreviewUiModel>() {
            override fun areItemsTheSame(
                oldItem: PagePreviewUiModel,
                newItem: PagePreviewUiModel
            ): Boolean {
                return oldItem.pageId == newItem.pageId
            }

            override fun areContentsTheSame(
                oldItem: PagePreviewUiModel,
                newItem: PagePreviewUiModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun getChangePayload(
                oldItem: PagePreviewUiModel,
                newItem: PagePreviewUiModel
            ): Any? {
                return if (oldItem.rotateDegrees != newItem.rotateDegrees) {
                    PAYLOAD_ROTATE
                } else if (oldItem.cropTransformation != newItem.cropTransformation) {
                    PAYLOAD_CROP_MODE
                } else super.getChangePayload(oldItem, newItem)
            }
        }

        private const val PAYLOAD_ROTATE = "PAYLOAD_ROTATE"
        private const val PAYLOAD_CROP_ENABLE = "PAYLOAD_CROP_ENABLE"
        private const val PAYLOAD_CROP_MODE = "PAYLOAD_CROP_MODE"
    }

    var enableCrop: Boolean = false
        set(value) {
            field = value
            if (pageSelected != -1) {
                notifyItemChanged(pageSelected, PAYLOAD_CROP_ENABLE)
            } else {
                notifyItemRangeChanged(0, itemCount, PAYLOAD_CROP_ENABLE)
            }
        }

    var pageSelected: Int = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ItemPagePreviewBinding> {
        return BaseViewHolder(
            ItemPagePreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ItemPagePreviewBinding>, position: Int) {
        val item = getItem(position)
        with(holder) {
            val imagePreview = item.imagePreview ?: item.internalImage
            val cropPoints: Array<Point?>? = item.cropTransformation
                ?.packedOffsets
                ?.map { Point(it.x.toInt(), it.y.toInt()) }
                ?.toTypedArray()
            kotlin.runCatching {
                try {
                    val cr = context.contentResolver
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(
                        cr.openInputStream(imagePreview.androidUri),
                        Rect(),
                        options
                    )
                    options.inJustDecodeBounds = false
                    options.inSampleSize = calculateSampleSize(options)
                    val bitmap =
                        BitmapFactory.decodeStream(
                            cr.openInputStream(imagePreview.androidUri),
                            Rect(),
                            options
                        )
                    if (bitmap != null) {
                        binding.ivPreviewPage.setImageToCrop(bitmap, cropPoints)
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
            binding.ivPreviewPage.rotate(item.rotateDegrees)
            binding.ivPreviewPage.setShowCropPoint(enableCrop)
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ItemPagePreviewBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.any {
                listOf(PAYLOAD_ROTATE, PAYLOAD_CROP_ENABLE, PAYLOAD_CROP_MODE).contains(
                    it
                )
            }) {
            with(holder) {
                val item = getItem(position)
                if (payloads.contains(PAYLOAD_ROTATE)) {
                    binding.ivPreviewPage.rotate(item.rotateDegrees)
                }
                if (payloads.contains(PAYLOAD_CROP_ENABLE)) {
                    Timber.d("PAYLOAD_CROP_ENABLE with param enableCrop:$enableCrop")
                    binding.ivPreviewPage.setShowCropPoint(enableCrop)
                }
                if (payloads.contains(PAYLOAD_CROP_MODE)) {
                    val bitmap = binding.ivPreviewPage.bitmap
                    if (bitmap != null) {
                        val cropPoints: Array<Point?>? = item.cropTransformation
                            ?.packedOffsets
                            ?.map { Point(it.x.toInt(), it.y.toInt()) }
                            ?.toTypedArray()
                        binding.ivPreviewPage.setImageToCrop(bitmap, cropPoints)
                    }
                }
            }
        } else super.onBindViewHolder(holder, position, payloads)
    }

    private fun calculateSampleSize(options: BitmapFactory.Options): Int {
        val outHeight = options.outHeight
        val outWidth = options.outWidth
        var sampleSize = 1
        val destHeight = 1000
        val destWidth = 1000
        if (outHeight > destHeight || outWidth > destHeight) {
            sampleSize = if (outHeight > outWidth) {
                outHeight / destHeight
            } else {
                outWidth / destWidth
            }
        }
        if (sampleSize < 1) {
            sampleSize = 1
        }
        return sampleSize
    }

}