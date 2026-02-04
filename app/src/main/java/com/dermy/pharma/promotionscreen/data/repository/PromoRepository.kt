package com.dermy.pharma.promotionscreen.data.repository

import android.content.Context
import com.dermy.pharma.promotionscreen.data.model.MediaItem
import com.dermy.pharma.promotionscreen.data.model.PromoConfig
import com.dermy.pharma.promotionscreen.data.remote.AuthDataSource
import com.dermy.pharma.promotionscreen.data.remote.ConfigDataSource
import com.dermy.pharma.promotionscreen.data.remote.DriveMediaDataSource
import com.dermy.pharma.promotionscreen.util.DriveUrlHelper

interface PromoRepository {
    suspend fun refreshConfig(): Boolean
    fun getConfig(): PromoConfig
    suspend fun getMediaItems(context: Context): List<MediaItem>
}

class PromoRepositoryImpl(
    private val configDataSource: ConfigDataSource,
    private val driveMediaDataSource: DriveMediaDataSource,
    private val authDataSource: AuthDataSource
) : PromoRepository {

    override suspend fun refreshConfig(): Boolean {
        return configDataSource.fetchAndActivate()
    }

    override fun getConfig(): PromoConfig {
        return configDataSource.getPromoConfig()
    }

    override suspend fun getMediaItems(context: Context): List<MediaItem> {
        val account = authDataSource.getLastSignedInAccount(context) ?: return emptyList()
        val token = authDataSource.getAccessToken(context, account)
        val config = configDataSource.getPromoConfig()
        val folderId = DriveUrlHelper.extractFolderId(config.driveUrl) ?: return emptyList()
        return driveMediaDataSource.getMediaItems(token, folderId)
    }
}
