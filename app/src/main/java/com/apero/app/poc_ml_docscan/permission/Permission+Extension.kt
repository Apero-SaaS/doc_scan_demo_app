package com.apero.app.poc_ml_docscan.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.apero.app.poc_ml_docscan.permission.manager.impl.StoragePermissionManager

/**
 * Created by KO Huyn on 21/11/2023.
 */

fun Context.isGrantedPostNotification(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true
}

fun Context.isGrantedFileManager(): Boolean {
    return StoragePermissionManager.isPermissionGranted(this)
}