package com.apero.app.poc_ml_docscan.permission.manager.impl

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.apero.app.poc_ml_docscan.permission.manager.PermissionManager

class StoragePermissionManager : PermissionManager {
    private var activity: AppCompatActivity? = null
    private var fragment: Fragment? = null

    constructor(activity: AppCompatActivity) : super(activity) {
        this.activity = activity
        requestSinglePermissions = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            notifyResultPermission()
        }
        requestSystemPermission = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            notifyResultPermission()
        }
        requestActivityResultContracts = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            notifyResultPermission()
        }
    }

    constructor(fragment: Fragment) : super(fragment) {
        this.fragment = fragment
        requestSinglePermissions = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            notifyResultPermission()
        }
        requestSystemPermission = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            notifyResultPermission()
        }
        requestActivityResultContracts = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            notifyResultPermission()
        }
    }

    private val permissionCache
        get() = (activity ?: fragment?.context
        ?: throw IllegalAccessException()).getSharedPreferences(
            "permission_cache",
            Context.MODE_PRIVATE
        )

    private var needRequestPermissionPopup: Boolean
        set(value) {
            permissionCache.edit { putBoolean(ARG_REQUEST_PERMISSION_FIRST, value) }
        }
        get() = permissionCache.getBoolean(ARG_REQUEST_PERMISSION_FIRST, true)

    private var hasRequestPermissionStorage: Boolean
        set(value) {
            permissionCache.edit { putBoolean(ARG_HAS_REQUEST_PERMISSION, value) }
        }
        get() = permissionCache.getBoolean(ARG_HAS_REQUEST_PERMISSION, false)

    private val requestSinglePermissions: ActivityResultLauncher<String>
    private val requestSystemPermission: ActivityResultLauncher<Intent>
    private val requestActivityResultContracts: ActivityResultLauncher<Intent>

    override fun hasRequestPermission(): Boolean {
        return hasRequestPermissionStorage
    }

    override fun requestPermission(requestCode: Int) {
        super.requestPermission(requestCode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestStoragePermissionApi30()
        } else {
            requestStoragePermissionApi19()
        }
    }

    override fun notifyResultPermission() {
        hasRequestPermissionStorage = true
        super.notifyResultPermission()
    }

    override fun isPermissionGranted(): Boolean {
        return (activity ?: fragment?.context)?.let { isPermissionGranted(it) } ?: false
    }

    private fun shouldShowRationale(): Boolean {
        return (activity
            ?: fragment?.activity)?.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ?: false
    }

    private fun requestStoragePermissionApi19() {
        if (needRequestPermissionPopup || shouldShowRationale()) {
            requestSinglePermissionPopup()
            if (needRequestPermissionPopup) {
                needRequestPermissionPopup = false
            }
        } else {
            requestSystemPermission()
        }
    }

    private fun requestSinglePermissionPopup() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        requestSinglePermissions.launch(permission)
        Log.d("StoragePermissionManage", "requestMultiplePermissions:launch")
    }

    private fun requestSystemPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", (activity ?: fragment?.activity)?.packageName, null)
        intent.data = uri
        requestSystemPermission.launch(intent)
        Log.d("StoragePermissionManage", "requestSystemPermission:launch")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestStoragePermissionApi30() {
        kotlin.runCatching {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            val uri = Uri.fromParts("package", (activity ?: fragment?.activity)?.packageName, null)
            intent.data = uri
            requestActivityResultContracts.launch(intent)
            Log.d("StoragePermissionManage", "requestActivityResultContracts:launch first")
        }.onFailure {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            requestActivityResultContracts.launch(intent)
            Log.e("StoragePermissionManage", "requestActivityResultContracts:launch second", it)
        }
    }

    companion object {
        private const val MANAGE_EXTERNAL_STORAGE_PERMISSION = "android:manage_external_storage"
        private const val ARG_REQUEST_PERMISSION_FIRST = "ARG_REQUEST_PERMISSION_FIRST"
        private const val ARG_HAS_REQUEST_PERMISSION = "ARG_HAS_REQUEST_PERMISSION"

        @JvmStatic
        fun isPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                checkStoragePermissionApi30(context)
            } else {
                checkStoragePermissionApi19(context)
            }
        }

        @RequiresApi(Build.VERSION_CODES.R)
        private fun checkStoragePermissionApi30(context: Context): Boolean {
            val appOps = context.getSystemService(AppOpsManager::class.java)
            val mode = appOps.unsafeCheckOpNoThrow(
                MANAGE_EXTERNAL_STORAGE_PERMISSION,
                context.applicationInfo.uid,
                context.packageName
            )

            return mode == AppOpsManager.MODE_ALLOWED
        }

        private fun checkStoragePermissionApi19(context: Context): Boolean {
            val status =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            return status == PackageManager.PERMISSION_GRANTED
        }
    }
}
