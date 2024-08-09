package com.apero.core.scan.util

internal object OpenCvHelper {
    fun loadLibrary() {
        System.loadLibrary("opencv_java4")
    }
}
