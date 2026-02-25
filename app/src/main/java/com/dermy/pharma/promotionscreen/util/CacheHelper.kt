package com.dermy.pharma.promotionscreen.util

import android.content.Context
import coil.Coil
import coil.ImageLoader
import java.io.File

object CacheHelper {

    private const val MEDIA_DIR_NAME = "promo_media"

    fun getMediaDir(context: Context): File {
        val dir = File(context.filesDir, MEDIA_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun clearMediaCache(context: Context) {
        clearCoilCache(context)
        clearAppCacheDir(context)
    }

    suspend fun deleteAllMedia(context: Context, onProgress: (Int) -> Unit) {
        clearCoilCache(context)
        clearAppCacheDir(context)
        val mediaDir = getMediaDir(context)
        val files = mediaDir.listFiles() ?: run {
            onProgress(100)
            return
        }
        if (files.isEmpty()) {
            onProgress(100)
            return
        }
        files.forEachIndexed { index, file ->
            file.deleteRecursively()
            val percent = ((index + 1) * 100) / files.size
            onProgress(percent)
        }
    }

    private fun clearCoilCache(context: Context) {
        runCatching {
            val imageLoader: ImageLoader = Coil.imageLoader(context)
            imageLoader.memoryCache?.clear()
            imageLoader.diskCache?.clear()
        }
    }

    private fun clearAppCacheDir(context: Context) {
        runCatching {
            context.cacheDir.listFiles()?.forEach { file ->
                file.deleteRecursively()
            }
        }
    }
}