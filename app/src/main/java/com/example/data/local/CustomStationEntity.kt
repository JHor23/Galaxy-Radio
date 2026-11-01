package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.RadioStation

@Entity(tableName = "radio_stations")
data class CustomStationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val streamUrl: String,
    val genre: String,
    val bitrate: Int,
    val codec: String,
    val country: String,
    val faviconUrl: String?,
    val isCustom: Boolean,
    val isFavorite: Boolean,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toRadioStation(): RadioStation = RadioStation(
        id = id,
        name = name,
        streamUrl = streamUrl,
        genre = genre,
        bitrate = bitrate,
        codec = codec,
        country = country,
        faviconUrl = faviconUrl,
        isCustom = isCustom,
        isFavorite = isFavorite
    )

    companion object {
        fun fromRadioStation(station: RadioStation): CustomStationEntity = CustomStationEntity(
            id = station.id,
            name = station.name,
            streamUrl = station.streamUrl,
            genre = station.genre,
            bitrate = station.bitrate,
            codec = station.codec,
            country = station.country,
            faviconUrl = station.faviconUrl,
            isCustom = station.isCustom,
            isFavorite = station.isFavorite
        )
    }
}
