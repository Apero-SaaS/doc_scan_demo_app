package com.apero.app.poc_ml_docscan.repo

import com.apero.app.poc_ml_docscan.repo.sourcer.InternalCapturedImageSource
import com.apero.app.poc_ml_docscan.home.model.ScanSession
import com.apero.app.poc_ml_docscan.home.model.SessionId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.File
import java.util.UUID

@Single
class ScanDocumentRepository(
    private val internalCapturedImageSource: InternalCapturedImageSource,
) {
    suspend fun newScanSession(): ScanSession {
        val uuid = UUID.randomUUID().toString()
        return ScanSession(
            id = uuid,
            capturedImageFolder = internalCapturedImageSource.getImageFolderFromSessionBy(uuid)
        )
    }

    suspend fun newCaptureImageFile(id: SessionId): File {
        val cachedFolder = internalCapturedImageSource.getImageFolderFromSessionBy(id)
        val cacheName = UUID.randomUUID().toString()
        val emptyImageFile = File(
            cachedFolder,
            "${cacheName}.jpg",
        )
        emptyImageFile.parentFile!!.mkdirs()

        return emptyImageFile
    }

    suspend fun deleteScanSession(sessionId: SessionId) = withContext(Dispatchers.IO) {
        internalCapturedImageSource.getImageFolderFromSessionBy(sessionId)
            .deleteRecursively()
    }
}
