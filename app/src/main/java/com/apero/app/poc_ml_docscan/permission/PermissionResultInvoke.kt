package com.apero.app.poc_ml_docscan.permission

interface PermissionResultInvoke {
    fun onPermissionGranted(requestCode: Int?, isGranted: Boolean)
    fun isReplayValue(): Boolean = false
}