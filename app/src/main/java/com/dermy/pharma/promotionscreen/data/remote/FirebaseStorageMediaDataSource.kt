package com.dermy.pharma.promotionscreen.data.remote

import com.dermy.pharma.promotionscreen.data.model.MediaItem
import com.dermy.pharma.promotionscreen.data.model.MediaType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FirebaseStorageMediaDataSource {
    suspend fun getMediaItems(storageFolderPath: String): List<MediaItem>
}

class FirebaseStorageMediaDataSourceImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : FirebaseStorageMediaDataSource {

    override suspend fun getMediaItems(storageFolderPath: String): List<MediaItem> {
        if (storageFolderPath.isBlank()) return emptyList()
        return withContext(Dispatchers.IO) {
            ensureSignedIn()
            val folderRef = storage.reference.child(storageFolderPath.trim().trimEnd('/'))
            val listResult = folderRef.listAll().await()
            val items = listResult.items
            items.mapNotNull { ref ->
                val name = ref.name
                val type = mediaTypeFromFileName(name) ?: return@mapNotNull null
                val url = try {
                    ref.downloadUrl.await().toString()
                } catch (_: Exception) {
                    return@mapNotNull null
                }
                MediaItem(url = url, type = type, fileId = ref.name)
            }
        }
    }

    private suspend fun ensureSignedIn() {
        if (auth.currentUser != null) return
        val emailResult = runCatching {
            auth.signInWithEmailAndPassword(HARDCODED_EMAIL, HARDCODED_PASSWORD).await()
        }
        if (emailResult.isFailure && auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    private fun mediaTypeFromFileName(fileName: String): MediaType? {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            in IMAGE_EXTENSIONS -> MediaType.IMAGE
            in VIDEO_EXTENSIONS -> MediaType.VIDEO
            else -> null
        }
    }

    companion object {
        private const val HARDCODED_EMAIL = "martinmcfly1990@gmail.com"
        private const val HARDCODED_PASSWORD = "dermy@1599"
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
        private val VIDEO_EXTENSIONS = setOf("mp4", "webm", "3gp", "mkv", "mov")
    }
}
