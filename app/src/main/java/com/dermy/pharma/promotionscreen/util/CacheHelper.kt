package com.dermy.pharma.promotionscreen.util

import android.content.Context
import coil.Coil
import coil.ImageLoader
import java.io.File

object CacheHelper {

    fun clearMediaCache(context: Context) {
        clearCoilCache(context)
        clearAppCacheDir(context)
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
