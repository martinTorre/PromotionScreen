package com.dermy.pharma.promotionscreen.data.local

import android.content.Context
import android.content.SharedPreferences

object LocalSettingsConstants {
    const val PREFS_NAME = "promo_screen_prefs"
    const val KEY_LAST_MEDIA_FETCH_TIME = "last_media_fetch_time"
    const val KEY_TV_ACCESS_TOKEN = "tv_access_token"
    const val KEY_TV_REFRESH_TOKEN = "tv_refresh_token"
    const val KEY_TV_TOKEN_EXPIRES_AT = "tv_token_expires_at"
}

class LocalSettings(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        LocalSettingsConstants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getLastMediaFetchTime(): Long? {
        val value = prefs.getLong(LocalSettingsConstants.KEY_LAST_MEDIA_FETCH_TIME, -1L)
        return if (value < 0) null else value
    }

    fun setLastMediaFetchTime(timeMillis: Long) {
        prefs.edit().putLong(LocalSettingsConstants.KEY_LAST_MEDIA_FETCH_TIME, timeMillis).apply()
    }

    fun getTvAccessToken(): String? = prefs.getString(LocalSettingsConstants.KEY_TV_ACCESS_TOKEN, null)?.takeIf { it.isNotEmpty() }

    fun setTvAccessToken(token: String?) {
        prefs.edit().putString(LocalSettingsConstants.KEY_TV_ACCESS_TOKEN, token ?: "").apply()
    }

    fun getTvRefreshToken(): String? = prefs.getString(LocalSettingsConstants.KEY_TV_REFRESH_TOKEN, null)?.takeIf { it.isNotEmpty() }

    fun setTvRefreshToken(token: String?) {
        prefs.edit().putString(LocalSettingsConstants.KEY_TV_REFRESH_TOKEN, token ?: "").apply()
    }

    fun getTvTokenExpiresAt(): Long = prefs.getLong(LocalSettingsConstants.KEY_TV_TOKEN_EXPIRES_AT, 0L)

    fun setTvTokenExpiresAt(expiresAtMillis: Long) {
        prefs.edit().putLong(LocalSettingsConstants.KEY_TV_TOKEN_EXPIRES_AT, expiresAtMillis).apply()
    }

    fun clearTvTokens() {
        prefs.edit()
            .remove(LocalSettingsConstants.KEY_TV_ACCESS_TOKEN)
            .remove(LocalSettingsConstants.KEY_TV_REFRESH_TOKEN)
            .remove(LocalSettingsConstants.KEY_TV_TOKEN_EXPIRES_AT)
            .apply()
    }
}
