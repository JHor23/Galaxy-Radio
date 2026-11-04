package com.example.data.repository

import com.example.data.local.CustomStationEntity
import com.example.data.local.RadioStationDao
import com.example.data.remote.RadioBrowserApi
import com.example.model.RadioStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.example.data.local.HiddenCuratedStationEntity
import kotlinx.coroutines.flow.first

class RadioStationRepository(
    private val stationDao: RadioStationDao,
    private val api: RadioBrowserApi = RadioBrowserApi.create()
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    val customStations: Flow<List<RadioStation>> = stationDao.getCustomStations().map { list ->
        list.map { it.toRadioStation() }
    }

    val favoriteStations: Flow<List<RadioStation>> = stationDao.getFavoriteStations().map { list ->
        list.map { it.toRadioStation() }
    }

    val hiddenCuratedIds: Flow<List<String>> = stationDao.getHiddenCuratedStationIds()

    val curatedGalaxyStations: Flow<List<RadioStation>> = hiddenCuratedIds.map { hiddenIds ->
        val hiddenSet = hiddenIds.toSet()
        CURATED_GALAXY_STATIONS.filter { it.id !in hiddenSet }
    }

    suspend fun getActiveCuratedStations(): List<RadioStation> = withContext(Dispatchers.IO) {
        val hiddenSet = hiddenCuratedIds.first().toSet()
        CURATED_GALAXY_STATIONS.filter { it.id !in hiddenSet }
    }

    suspend fun deleteCuratedStation(stationId: String) = withContext(Dispatchers.IO) {
        stationDao.hideCuratedStation(HiddenCuratedStationEntity(stationId))
    }

    suspend fun resetAllCuratedStations() = withContext(Dispatchers.IO) {
        stationDao.clearAllHiddenCuratedStations()
    }

    suspend fun getHiddenCuratedStationsCount(): Int = withContext(Dispatchers.IO) {
        hiddenCuratedIds.first().size
    }

    companion object {
        val CURATED_GALAXY_STATIONS = listOf(
        RadioStation(
            id = "curated_soma_spacestation",
            name = "Space Station Soma",
            streamUrl = "https://ice1.somafm.com/spacestation-128-mp3",
            genre = "Ambient Space",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/spacestation120.png"
        ),
        RadioStation(
            id = "curated_nightwave_plaza",
            name = "Nightwave Plaza",
            streamUrl = "https://radio.plaza.one/mp3",
            genre = "Synthwave",
            bitrate = 128,
            codec = "MP3",
            country = "Global",
            faviconUrl = "https://plaza.one/apple-touch-icon.png"
        ),
        RadioStation(
            id = "curated_soma_groovesalad",
            name = "Groove Salad",
            streamUrl = "https://ice1.somafm.com/groovesalad-128-mp3",
            genre = "Downtempo",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/groovesalad120.png"
        ),
        RadioStation(
            id = "curated_soma_dronezone",
            name = "Drone Zone",
            streamUrl = "https://ice1.somafm.com/dronezone-128-mp3",
            genre = "Atmospheric Space",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/dronezone120.png"
        ),
        RadioStation(
            id = "curated_soma_deepspace",
            name = "Deep Space One",
            streamUrl = "https://ice1.somafm.com/deepspaceone-128-mp3",
            genre = "Deep Ambient",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/deepspaceone120.png"
        ),
        RadioStation(
            id = "curated_chillsky_lofi",
            name = "Cosmic Lo-Fi Sky",
            streamUrl = "https://stream.zeno.fm/f3wvbbqmdg8uv",
            genre = "Lo-Fi",
            bitrate = 128,
            codec = "MP3",
            country = "Global"
        ),
        RadioStation(
            id = "curated_soma_defcon",
            name = "DEF CON Radio",
            streamUrl = "https://ice1.somafm.com/defcon-128-mp3",
            genre = "Cyber Electronic",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/defcon120.png"
        ),
        RadioStation(
            id = "curated_soma_missioncontrol",
            name = "Mission Control",
            streamUrl = "https://ice1.somafm.com/missioncontrol-128-mp3",
            genre = "Ambient Space & NASA",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/missioncontrol120.png"
        ),
        RadioStation(
            id = "curated_soma_beatblender",
            name = "Beat Blender",
            streamUrl = "https://ice1.somafm.com/beatblender-128-mp3",
            genre = "Deep House",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/beatblender120.png"
        ),
        RadioStation(
            id = "curated_soma_secretagent",
            name = "Secret Agent",
            streamUrl = "https://ice1.somafm.com/secretagent-128-mp3",
            genre = "Spy & Lounge",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/secretagent120.png"
        ),
        RadioStation(
            id = "curated_soma_suburbsofgoa",
            name = "Suburbs of Goa",
            streamUrl = "https://ice4.somafm.com/suburbsofgoa-128-mp3",
            genre = "World Chill",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/suburbsofgoa120.png"
        ),
        RadioStation(
            id = "curated_soma_cliqhop",
            name = "Cliqhop IDM",
            streamUrl = "https://ice1.somafm.com/cliqhop-128-mp3",
            genre = "Cosmic Electronic",
            bitrate = 128,
            codec = "MP3",
            country = "United States",
            faviconUrl = "https://somafm.com/img3/cliqhop120.png"
        )
    )
    }

    suspend fun fetchPopularStations(tag: String? = null): List<RadioStation> = withContext(Dispatchers.IO) {
        try {
            val dtoList = if (tag.isNullOrBlank() || tag.equals("all", ignoreCase = true)) {
                api.getTopStations(limit = 40)
            } else {
                api.getStationsByTag(tag.lowercase().trim(), limit = 35)
            }

            val remoteStations = dtoList.mapNotNull { dto ->
                val stream = dto.urlResolved ?: dto.url
                if (!stream.isNullOrBlank()) {
                    RadioStation(
                        id = dto.stationUuid,
                        name = dto.name.trim(),
                        streamUrl = stream,
                        genre = dto.tags?.split(",")?.firstOrNull()?.trim()?.replaceFirstChar { it.uppercase() } ?: "Music",
                        bitrate = dto.bitrate ?: 128,
                        codec = dto.codec ?: "MP3",
                        country = dto.country ?: "Global",
                        faviconUrl = dto.favicon?.takeIf { it.isNotBlank() },
                        isCustom = false
                    )
                } else null
            }

            if (remoteStations.isNotEmpty()) remoteStations else getActiveCuratedStations()
        } catch (e: Exception) {
            // Fallback gracefully to curated celestial stations if offline or network timeout
            getActiveCuratedStations()
        }
    }

    suspend fun searchStations(query: String): List<RadioStation> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val activeCurated = getActiveCuratedStations()
        try {
            val dtoList = api.searchStations(name = query.trim(), limit = 30)
            val remote = dtoList.mapNotNull { dto ->
                val stream = dto.urlResolved ?: dto.url
                if (!stream.isNullOrBlank()) {
                    RadioStation(
                        id = dto.stationUuid,
                        name = dto.name.trim(),
                        streamUrl = stream,
                        genre = dto.tags?.split(",")?.firstOrNull()?.trim() ?: "Radio",
                        bitrate = dto.bitrate ?: 128,
                        codec = dto.codec ?: "MP3",
                        country = dto.country ?: "Global",
                        faviconUrl = dto.favicon?.takeIf { it.isNotBlank() },
                        isCustom = false
                    )
                } else null
            }
            if (remote.isNotEmpty()) remote else activeCurated.filter {
                it.name.contains(query, ignoreCase = true) || it.genre.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            activeCurated.filter {
                it.name.contains(query, ignoreCase = true) || it.genre.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun addCustomStation(
        name: String,
        streamUrl: String,
        genre: String,
        bitrate: Int = 128,
        country: String = "Custom",
        faviconUrl: String? = null
    ): RadioStation = withContext(Dispatchers.IO) {
        var finalUrl = streamUrl.trim()
        var finalName = name.trim()
        var finalGenre = genre.trim().ifBlank { "Custom" }
        var finalBitrate = if (bitrate > 0) bitrate else 128
        var finalFavicon = faviconUrl

        // Aggressively crawl and resolve real stream if given a webpage or playlist
        val crawled = com.example.data.crawler.AudioStreamCrawler.crawlForAudioFeed(finalUrl).getOrNull()
        if (crawled != null) {
            finalUrl = crawled.resolvedStreamUrl
            if (finalName.isBlank() || finalName == "Custom Station") {
                finalName = crawled.stationName
            }
            if (finalGenre == "Custom" && crawled.genre.isNotBlank() && crawled.genre != "Online Radio") {
                finalGenre = crawled.genre
            }
            if (finalBitrate == 128 && crawled.bitrate > 0) {
                finalBitrate = crawled.bitrate
            }
            if (finalFavicon == null && !crawled.faviconUrl.isNullOrBlank()) {
                finalFavicon = crawled.faviconUrl
            }
        }

        val newStation = RadioStation(
            id = "custom_" + UUID.randomUUID().toString(),
            name = finalName.ifBlank { "Custom Station" },
            streamUrl = finalUrl,
            genre = finalGenre,
            bitrate = finalBitrate,
            codec = if (finalUrl.contains(".aac", ignoreCase = true)) "AAC" else "MP3",
            country = country.trim().ifBlank { "Custom" },
            faviconUrl = finalFavicon,
            isCustom = true,
            isFavorite = false
        )
        stationDao.insertStation(CustomStationEntity.fromRadioStation(newStation))
        newStation
    }

    suspend fun deleteCustomStation(station: RadioStation) = withContext(Dispatchers.IO) {
        stationDao.deleteById(station.id)
    }

    suspend fun toggleFavorite(station: RadioStation, isFav: Boolean) = withContext(Dispatchers.IO) {
        val updated = station.copy(isFavorite = isFav)
        stationDao.insertStation(CustomStationEntity.fromRadioStation(updated))
    }

    /**
     * Aggressively crawl and test connection to any radio streaming URL or webpage.
     */
    suspend fun testStreamConnection(url: String): Result<StreamTestResult> = withContext(Dispatchers.IO) {
        val crawlResult = com.example.data.crawler.AudioStreamCrawler.crawlForAudioFeed(url)
        crawlResult.map { crawled ->
            StreamTestResult(
                isSuccess = true,
                resolvedStreamUrl = crawled.resolvedStreamUrl,
                stationName = crawled.stationName,
                genre = crawled.genre,
                bitrate = crawled.bitrate,
                contentType = crawled.contentType,
                faviconUrl = crawled.faviconUrl,
                isCrawledFromWebpage = crawled.isFromWebpage
            )
        }
    }
}

data class StreamTestResult(
    val isSuccess: Boolean,
    val resolvedStreamUrl: String,
    val stationName: String,
    val genre: String,
    val bitrate: Int,
    val contentType: String,
    val faviconUrl: String? = null,
    val isCrawledFromWebpage: Boolean = false
)
