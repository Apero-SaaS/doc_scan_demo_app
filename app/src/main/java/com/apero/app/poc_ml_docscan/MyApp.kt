package com.apero.app.poc_ml_docscan

import android.app.Application
import com.apero.app.poc_ml_docscan.repo.ScanDocumentRepository
import com.apero.app.poc_ml_docscan.repo.sourcer.InternalCapturedImageSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.opencv.BuildConfig
import timber.log.Timber

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidContext(this@MyApp)
        }
    }

    companion object {
        lateinit var instance: MyApp
            private set
    }
}