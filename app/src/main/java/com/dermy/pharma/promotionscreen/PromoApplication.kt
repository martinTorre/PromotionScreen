package com.dermy.pharma.promotionscreen

import android.app.Application
import com.dermy.pharma.promotionscreen.data.remote.AuthDataSource
import com.dermy.pharma.promotionscreen.data.remote.AuthDataSourceImpl
import com.dermy.pharma.promotionscreen.data.remote.ConfigDataSource
import com.dermy.pharma.promotionscreen.data.remote.ConfigDataSourceImpl
import com.dermy.pharma.promotionscreen.data.remote.DriveMediaDataSource
import com.dermy.pharma.promotionscreen.data.remote.DriveMediaDataSourceImpl
import com.dermy.pharma.promotionscreen.data.repository.PromoRepository
import com.dermy.pharma.promotionscreen.data.repository.PromoRepositoryImpl
import com.dermy.pharma.promotionscreen.di.KtorClient
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class PromoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initRemoteConfigDefaults()
    }

    private fun initRemoteConfigDefaults() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
        )
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    companion object {
        private val authDataSourceInstance: AuthDataSource by lazy { AuthDataSourceImpl() }

        fun getAuthDataSource(): AuthDataSource = authDataSourceInstance

        fun getPromoRepository(): PromoRepository {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configDataSource: ConfigDataSource = ConfigDataSourceImpl(remoteConfig)
            val driveMediaDataSource: DriveMediaDataSource =
                DriveMediaDataSourceImpl(KtorClient.create())
            return PromoRepositoryImpl(configDataSource, driveMediaDataSource, authDataSourceInstance)
        }
    }
}
