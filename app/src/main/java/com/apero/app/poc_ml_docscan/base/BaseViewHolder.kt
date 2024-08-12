package com.apero.app.poc_ml_docscan.base

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class BaseViewHolder<out T : ViewBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root) {
    val context get() = itemView.context
}