package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RadioBrowserStationDto(
    @Json(name = "stationuuid") val stationUuid: String,
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String?,
    @Json(name = "url_resolved") val urlResolved: String?,
    @Json(name = "favicon") val favicon: String?,
    @Json(name = "tags") val tags: String?,
    @Json(name = "country") val country: String?,
    @Json(name = "bitrate") val bitrate: Int?,
    @Json(name = "codec") val codec: String?,
    @Json(name = "votes") val votes: Int?
)
