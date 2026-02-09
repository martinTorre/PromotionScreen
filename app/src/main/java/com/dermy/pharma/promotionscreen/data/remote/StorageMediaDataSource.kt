package com.dermy.pharma.promotionscreen.data.remote

import com.dermy.pharma.promotionscreen.data.model.MediaItem
import com.dermy.pharma.promotionscreen.data.model.MediaType
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface StorageMediaDataSource {
    suspend fun getMediaItems(storageBaseUrl: String): List<MediaItem>
}

data class StorageMediaItemDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("type") val type: String
)

class StorageMediaDataSourceImpl(
    private val httpClient: HttpClient
) : StorageMediaDataSource {

    private val gson = Gson()
    private val listType = object : TypeToken<List<StorageMediaItemDto>>() {}.type

    override suspend fun getMediaItems(storageBaseUrl: String): List<MediaItem> {
        if (storageBaseUrl.isBlank()) return emptyList()
        val base = storageBaseUrl.trim().let { if (it.endsWith("/")) it else "$it/" }
        val manifestUrl = if (base.contains("?")) {
            val (path, query) = base.split("?", limit = 2)
            path + LIST_JSON_FILENAME + "?" + query
        } else {
            base + LIST_JSON_FILENAME + "?alt=media"
        }
        val bodyStr = try {
            httpClient.get(manifestUrl).body<String>()
        } catch (_: Exception) {
            return emptyList()
        }
        val list = gson.fromJson<List<StorageMediaItemDto>>(bodyStr, listType) ?: return emptyList()
        return list.mapNotNull { dto ->
            val mediaType = when (dto.type.uppercase()) {
                "IMAGE" -> MediaType.IMAGE
                "VIDEO" -> MediaType.VIDEO
                else -> return@mapNotNull null
            }
            val fullUrl = when {
                !dto.url.isNullOrBlank() -> dto.url
                !dto.name.isNullOrBlank() -> buildFileUrl(base, dto.name)
                else -> return@mapNotNull null
            }
            MediaItem(url = fullUrl, type = mediaType, fileId = null)
        }
    }

    private fun buildFileUrl(base: String, fileName: String): String {
        val encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replace("+", "%20")
        return if (base.contains("?")) {
            val (path, query) = base.split("?", limit = 2)
            path + encoded + "?" + query
        } else {
            base + encoded + "?alt=media"
        }
    }

    companion object {
        private const val LIST_JSON_FILENAME = "list.json"
    }
}
