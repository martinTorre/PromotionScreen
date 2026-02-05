package com.dermy.pharma.promotionscreen.data.remote

import com.dermy.pharma.promotionscreen.data.model.MediaItem
import com.dermy.pharma.promotionscreen.data.model.MediaType
import com.dermy.pharma.promotionscreen.data.remote.drive.DriveFilesResponse
import com.dermy.pharma.promotionscreen.util.DriveUrlHelper
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse

interface DriveMediaDataSource {
    suspend fun getMediaItems(accessToken: String, folderId: String): List<MediaItem>
    suspend fun fileExists(accessToken: String, fileId: String): Boolean
}

class DriveMediaDataSourceImpl(
    private val httpClient: HttpClient
) : DriveMediaDataSource {

    private val gson = Gson()

    override suspend fun getMediaItems(accessToken: String, folderId: String): List<MediaItem> {
        val body: String = httpClient.get(DRIVE_API_BASE) {
            header("Cache-Control", "no-cache, no-store, must-revalidate")
            header("Pragma", "no-cache")
            parameter("q", "'$folderId' in parents and trashed = false")
            parameter("fields", "files(id,name,mimeType)")
            parameter("access_token", accessToken)
            parameter("_", System.currentTimeMillis())
        }.body()
        val response: DriveFilesResponse = gson.fromJson(body, DriveFilesResponse::class.java)
        return response.Files
            .filter { file -> isSupportedMimeType(file.MimeType) }
            .map { file ->
                val downloadUrl = "${DriveUrlHelper.DRIVE_UC_DOWNLOAD_PUBLIC}${file.Id}"
                MediaItem(
                    url = downloadUrl,
                    type = mediaTypeFromMimeType(file.MimeType),
                    fileId = file.Id
                )
            }
    }

    private fun isSupportedMimeType(mimeType: String): Boolean {
        val lower = mimeType.lowercase()
        return lower.startsWith("image/") || lower.startsWith("video/")
    }

    private fun mediaTypeFromMimeType(mimeType: String): MediaType {
        return if (mimeType.lowercase().startsWith("video/")) MediaType.VIDEO else MediaType.IMAGE
    }

    override suspend fun fileExists(accessToken: String, fileId: String): Boolean {
        return runCatching {
            val response: HttpResponse = httpClient.get("$DRIVE_API_BASE/$fileId") {
                header("Cache-Control", "no-cache, no-store, must-revalidate")
                parameter("fields", "id")
                parameter("access_token", accessToken)
            }
            response.status.value in 200..299
        }.getOrElse { false }
    }

    companion object {
        private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3/files"
    }
}
