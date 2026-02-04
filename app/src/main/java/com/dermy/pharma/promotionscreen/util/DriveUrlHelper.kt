package com.dermy.pharma.promotionscreen.util

object DriveUrlHelper {

    private const val DRIVE_FILE_PREFIX = "https://drive.google.com/file/d/"
    const val DRIVE_UC_DOWNLOAD_PUBLIC = "https://drive.google.com/uc?export=download&id="
    private const val DRIVE_UC_DOWNLOAD = DRIVE_UC_DOWNLOAD_PUBLIC

    fun toDirectDownloadUrl(url: String): String {
        val id = extractFileId(url) ?: return url
        return "$DRIVE_UC_DOWNLOAD$id"
    }

    fun extractFolderId(driveUrl: String): String? {
        val folderMatch = Regex("drive/folders/([a-zA-Z0-9_-]+)").find(driveUrl)
        return folderMatch?.groupValues?.get(1)
    }

    private fun extractFileId(url: String): String? {
        if (url.startsWith(DRIVE_FILE_PREFIX)) {
            val afterPrefix = url.removePrefix(DRIVE_FILE_PREFIX)
            return afterPrefix.substringBefore("/")
        }
        val match = Regex("[/?]d/([a-zA-Z0-9_-]+)").find(url)
        return match?.groupValues?.get(1)
    }
}
