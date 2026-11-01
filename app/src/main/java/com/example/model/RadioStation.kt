package com.example.model

data class RadioStation(
    val id: String,
    val name: String,
    val streamUrl: String,
    val genre: String = "Various",
    val bitrate: Int = 128,
    val codec: String = "MP3",
    val country: String = "Global",
    val faviconUrl: String? = null,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
) {
    val displayBitrate: String
        get() = if (bitrate > 0) "${bitrate} kbps" else "128 kbps"

    val displayGenre: String
        get() = genre.ifBlank { "Radio" }
}
