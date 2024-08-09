package com.apero.app.poc_ml_docscan.scan.impl.util

internal object OpenCvHelper {
    fun loadLibrary() {
        System.loadLibrary("opencv_java4")
    }
}
