package com.dermy.pharma.promotionscreen.util

import android.content.Context
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.os.Build

fun Context.isTvDevice(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as? android.app.UiModeManager
        uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    } else {
        @Suppress("DEPRECATION")
        (packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
            || (resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION)
    }
}
