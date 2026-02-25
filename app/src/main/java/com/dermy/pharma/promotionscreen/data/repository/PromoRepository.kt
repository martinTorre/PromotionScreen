package com.dermy.pharma.promotionscreen.data.repository

import android.content.Context
import com.dermy.pharma.promotionscreen.data.model.MediaItem
import com.dermy.pharma.promotionscreen.data.model.PromoConfig
import com.dermy.pharma.promotionscreen.data.remote.ConfigDataSource
import com.dermy.pharma.promotionscreen.data.remote.FirebaseStorageMediaDataSource
import com.dermy.pharma.promotionscreen.util.CacheHelper
import java.io.File

interface PromoRepository {
    suspend fun refreshConfig(): Boolean
    fun getConfig(): PromoConfig
    suspend fun getMediaItems(context: Context): List<MediaItem>
    suspend fun downloadAllMedia(
        context: Context,
        items: List<MediaItem>,
        onProgress: (Int) -> Unit
    ): List<MediaItem>
}

class PromoRepositoryImpl(
    private val configDataSource: ConfigDataSource,
    private val firebaseStorageMediaDataSource: FirebaseStorageMediaDataSource
) : PromoRepository {

    override suspend fun refreshConfig(): Boolean {
        return configDataSource.fetchAndActivate()
    }

    override fun getConfig(): PromoConfig {
        return configDataSource.getPromoConfig()
    }

    override suspend fun getMediaItems(context: Context): List<MediaItem> {
        val config = configDataSource.getPromoConfig()
        return runCatching {
            firebaseStorageMediaDataSource.getMediaItems(config.storageFolderPath)
        }.getOrElse { emptyList() }
    }

    override suspend fun downloadAllMedia(
        context: Context,
        items: List<MediaItem>,
        onProgress: (Int) -> Unit
    ): List<MediaItem> {
        if (items.isEmpty()) {
            onProgress(100)
            return items
        }
        val mediaDir = CacheHelper.getMediaDir(context)
        val result = mutableListOf<MediaItem>()
        items.forEachIndexed { index, item ->
            val fileName = item.fileId ?: "media_$index"
            val destFile = File(mediaDir, fileName)
            try {
                firebaseStorageMediaDataSource.downloadFile(item, destFile)
                result.add(item.copy(localPath = destFile.absolutePath))
            } catch (e: Exception) {
                result.add(item)
            }
            val percent = ((index + 1) * 100) / items.size
            onProgress(percent)
        }
        return result
    }
}