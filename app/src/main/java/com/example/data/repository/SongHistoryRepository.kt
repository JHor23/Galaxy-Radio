package com.example.data.repository

import android.content.Context
import com.example.data.local.RadioDatabase
import com.example.data.local.SongHistoryDao
import com.example.data.local.SongHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongHistoryRepository(private val songHistoryDao: SongHistoryDao) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            seedInitialHistoryIfEmpty()
        }
    }

    fun getHistoryForStation(stationId: String, streamUrl: String): Flow<List<SongHistoryEntity>> {
        return songHistoryDao.getHistoryForStation(stationId, streamUrl)
    }

    fun getAllHistory(): Flow<List<SongHistoryEntity>> {
        return songHistoryDao.getAllHistory()
    }

    suspend fun recordSong(
        stationId: String,
        stationName: String,
        streamUrl: String,
        trackTitle: String,
        artist: String
    ) = withContext(Dispatchers.IO) {
        val cleanTitle = trackTitle.trim()
        val cleanArtist = artist.trim()
        if (cleanTitle.isBlank() || cleanTitle.equals("No Stream Active", ignoreCase = true)) {
            return@withContext
        }

        // Avoid recording duplicate sequential tracks for the same station
        val latest = songHistoryDao.getLatestSongForStation(stationId, streamUrl)
        if (latest != null &&
            latest.trackTitle.equals(cleanTitle, ignoreCase = true) &&
            latest.artist.equals(cleanArtist, ignoreCase = true)
        ) {
            return@withContext
        }

        songHistoryDao.insert(
            SongHistoryEntity(
                stationId = stationId,
                stationName = stationName,
                streamUrl = streamUrl,
                trackTitle = cleanTitle,
                artist = if (cleanArtist.isBlank() || cleanArtist.equals(cleanTitle, ignoreCase = true)) stationName else cleanArtist,
                playedAtTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistoryForStation(stationId: String, streamUrl: String) = withContext(Dispatchers.IO) {
        songHistoryDao.deleteHistoryForStation(stationId, streamUrl)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        songHistoryDao.clearAllHistory()
    }

    private suspend fun seedInitialHistoryIfEmpty() {
        try {
            if (songHistoryDao.getCount() > 0) return

            val now = System.currentTimeMillis()
            val sampleTracks = listOf(
                // Space Station Soma
                Triple("curated_space_station", "Space Station Soma", listOf(
                    Pair("Solar Fields", "Sol (Album Version)"),
                    Pair("Carbon Based Lifeforms", "World of Sleepers"),
                    Pair("Aes Dana", "Inks (Morning Extended)"),
                    Pair("Asura", "Life² (Cosmic Voyage)"),
                    Pair("Stellardrone", "Eternity")
                )),
                // Drone Zone
                Triple("curated_drone_zone", "Drone Zone", listOf(
                    Pair("Steve Roach", "Structures from Silence"),
                    Pair("Robert Rich", "Echo of Real Landscape"),
                    Pair("Brian Eno", "An Ending (Ascent)"),
                    Pair("Stars of the Lid", "The Ballasted Orchestra")
                )),
                // Deep Space One
                Triple("curated_deep_space_one", "Deep Space One", listOf(
                    Pair("Lustmord", "Subspace Transmission"),
                    Pair("Biosphere", "Substrata"),
                    Pair("Alio Die", "Under an Amber Sky"),
                    Pair("Jonn Serrie", "And the Stars Go With You")
                )),
                // Groove Salad
                Triple("curated_groove_salad", "Groove Salad", listOf(
                    Pair("Bonobo", "Cirrus (Ambient Redux)"),
                    Pair("Tycho", "A Walk"),
                    Pair("Boards of Canada", "Dayvan Cowboy"),
                    Pair("Thievery Corporation", "Lebanese Blonde")
                ))
            )

            var timeOffset = 3600000L // 1 hour ago
            for ((id, name, tracks) in sampleTracks) {
                var stationOffset = timeOffset
                for ((artist, title) in tracks) {
                    songHistoryDao.insert(
                        SongHistoryEntity(
                            stationId = id,
                            stationName = name,
                            streamUrl = "",
                            trackTitle = title,
                            artist = artist,
                            playedAtTimestamp = now - stationOffset
                        )
                    )
                    stationOffset += 420000L // 7 minutes earlier
                }
                timeOffset += 1200000L
            }
        } catch (_: Exception) {
            // Seeding is optional best-effort
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SongHistoryRepository? = null

        fun getInstance(context: Context): SongHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                val db = RadioDatabase.getInstance(context)
                val instance = SongHistoryRepository(db.songHistoryDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
