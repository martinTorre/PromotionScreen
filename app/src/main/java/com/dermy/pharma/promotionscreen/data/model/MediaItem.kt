package com.dermy.pharma.promotionscreen.data.model

enum class MediaType {
    IMAGE,
    VIDEO
}

data class MediaItem(
    val url: String,
    val type: MediaType,
    val fileId: String? = null
)
