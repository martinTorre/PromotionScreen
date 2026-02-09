package com.dermy.pharma.promotionscreen.data.remote

import com.dermy.pharma.promotionscreen.data.model.PromoConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await

interface ConfigDataSource {
    suspend fun fetchAndActivate(): Boolean
    fun getPromoConfig(): PromoConfig
}

class ConfigDataSourceImpl(
    private val remoteConfig: FirebaseRemoteConfig
) : ConfigDataSource {

    override suspend fun fetchAndActivate(): Boolean {
        return remoteConfig.fetchAndActivate().await()
    }

    override fun getPromoConfig(): PromoConfig {
        val slideTimeStr = remoteConfig.getString(KEY_SLIDE_TIME)
        val slideTime = slideTimeStr.toLongOrNull()?.takeIf { it > 0 } ?: DEFAULT_SLIDE_TIME
        val storageBaseUrl = remoteConfig.getString(KEY_STORAGE_BASE_URL).takeIf { it.isNotBlank() } ?: DEFAULT_STORAGE_BASE_URL
        val storageFolderPath = remoteConfig.getString(KEY_STORAGE_FOLDER_PATH).takeIf { it.isNotBlank() } ?: DEFAULT_STORAGE_FOLDER_PATH
        return PromoConfig(
            slideTimeSeconds = slideTime,
            storageBaseUrl = storageBaseUrl,
            storageFolderPath = storageFolderPath
        )
    }

    companion object {
        private const val KEY_SLIDE_TIME = "SlideTime"
        private const val KEY_STORAGE_BASE_URL = "StorageBaseUrl"
        private const val KEY_STORAGE_FOLDER_PATH = "StorageFolderPath"
        private const val DEFAULT_SLIDE_TIME = 30L
        private const val DEFAULT_STORAGE_BASE_URL = "https://firebasestorage.googleapis.com/v0/b/dermy-pharma.firebasestorage.app/o/dermy%2F"
        private const val DEFAULT_STORAGE_FOLDER_PATH = "dermy"
    }
}
