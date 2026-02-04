package com.dermy.pharma.promotionscreen.data.remote.drive

import com.google.gson.annotations.SerializedName

data class DriveFilesResponse(
    @SerializedName("files") val Files: List<DriveFile> = emptyList()
)

data class DriveFile(
    @SerializedName("id") val Id: String,
    @SerializedName("name") val Name: String,
    @SerializedName("mimeType") val MimeType: String
)
