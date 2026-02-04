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
        val driveUrl = remoteConfig.getString(KEY_DRIVE_URL)
        return PromoConfig(
            slideTimeSeconds = slideTime,
            driveUrl = driveUrl.ifBlank { DEFAULT_DRIVE_URL }
        )
    }

    companion object {
        private const val KEY_SLIDE_TIME = "SlideTime"
        private const val KEY_DRIVE_URL = "DriveUrl"
        private const val DEFAULT_SLIDE_TIME = 30L
        private const val DEFAULT_DRIVE_URL = "https://drive.google.com/drive/folders/13CViE9s62xacgsyT1I40yt-78KEXv9Xm?usp=share_link"
    }
}
