package com.dermy.pharma.promotionscreen.data.repository

import android.content.Context
import com.dermy.pharma.promotionscreen.data.model.MediaItem
import com.dermy.pharma.promotionscreen.data.model.PromoConfig
import com.dermy.pharma.promotionscreen.data.remote.ConfigDataSource
import com.dermy.pharma.promotionscreen.data.remote.FirebaseStorageMediaDataSource

interface PromoRepository {
    suspend fun refreshConfig(): Boolean
    fun getConfig(): PromoConfig
    suspend fun getMediaItems(context: Context): List<MediaItem>
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
}
