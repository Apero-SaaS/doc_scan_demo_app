package com.apero.app.poc_ml_docscan.repo.sourcer

import android.content.Context
import arrow.atomic.AtomicBoolean
import com.apero.app.poc_ml_docscan.home.model.SessionId
import com.apero.app.poc_ml_docscan.scan.common.arrow.atomic.guardUntilTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.File

@Single
class InternalCapturedImageSource(
    private val context: Context,
) {

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private val coroutineScope: CoroutineScope = CoroutineScope(dispatcher)
    private val capturedSessionsFolder = File(context.cacheDir, CAPTURED_SESSIONS)

    private val isActive = AtomicBoolean(false)

    init {
        coroutineScope.launch(dispatcher) {
            if (!capturedSessionsFolder.exists()) {
                capturedSessionsFolder.mkdir()
            }
            isActive.set(true)
        }
    }

    suspend fun getImageFolderFromSessionBy(sessionId: SessionId): File = withContext(dispatcher) {
        isActive.guardUntilTrue()

        val folder = File(capturedSessionsFolder, sessionId)
        if (!folder.exists()) {
            capturedSessionsFolder.mkdir()
        }

        folder
    }

    companion object {
        private const val CAPTURED_SESSIONS = "captured-sessions"
    }
}
