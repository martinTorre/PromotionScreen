package com.dermy.pharma.promotionscreen.data.local

import android.content.Context
import android.content.SharedPreferences

object LocalSettingsConstants {
    const val PREFS_NAME = "promo_screen_prefs"
    const val KEY_LAST_MEDIA_FETCH_TIME = "last_media_fetch_time"
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
}
