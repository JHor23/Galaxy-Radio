package com.example.player

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.model.AudioQualityPreset
import com.example.model.RadioStation
import com.example.service.RadioPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PlayerStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

data class PlayerUiState(
    val currentStation: RadioStation? = null,
    val status: PlayerStatus = PlayerStatus.IDLE,
    val liveTrackTitle: String = "",
    val liveArtist: String = "",
    val audioQuality: AudioQualityPreset = AudioQualityPreset.BALANCED,
    val audioSessionId: Int = 0,
    val bytesStreamed: Long = 0L,
    val streamDurationSeconds: Long = 0L,
    val sleepTimerRemainingSeconds: Long? = null,
    val isMuted: Boolean = false,
    val errorMessage: String? = null
) {
    val isPlaying: Boolean
        get() = status == PlayerStatus.PLAYING

    val isBuffering: Boolean
        get() = status == PlayerStatus.BUFFERING

    val displayTrackTitle: String
        get() = liveTrackTitle.ifBlank { currentStation?.name ?: "No Stream Active" }

    val displayArtist: String
        get() = liveArtist.ifBlank { currentStation?.genre ?: "Live Galaxy Radio" }

    val formattedDataUsage: String
        get() {
            val mb = bytesStreamed / (1024.0 * 1024.0)
            return if (mb >= 1.0) String.format("%.2f MB", mb) else String.format("%.0f KB", bytesStreamed / 1024.0)
        }

    val formattedDuration: String
        get() {
            val minutes = streamDurationSeconds / 60
            val seconds = streamDurationSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

object RadioPlayerManager {

    private const val PREFS_NAME = "galaxy_radio_player_state"
    private const val KEY_STATION_ID = "last_station_id"
    private const val KEY_STATION_NAME = "last_station_name"
    private const val KEY_STATION_STREAM_URL = "last_station_stream_url"
    private const val KEY_STATION_GENRE = "last_station_genre"
    private const val KEY_STATION_FAVICON = "last_station_favicon"
    private const val KEY_STATION_BITRATE = "last_station_bitrate"
    private const val KEY_STATION_IS_CUSTOM = "last_station_is_custom"
    private const val KEY_STATION_IS_FAVORITE = "last_station_is_favorite"

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun init(context: Context) {
        if (_uiState.value.currentStation == null) {
            val restored = getPersistedStation(context)
            if (restored != null) {
                _uiState.update {
                    it.copy(
                        currentStation = restored,
                        status = PlayerStatus.PAUSED,
                        liveTrackTitle = restored.name,
                        liveArtist = restored.genre
                    )
                }
            }
        }
    }

    fun playStation(context: Context, station: RadioStation) {
        persistStation(context, station)
        _uiState.update {
            it.copy(
                currentStation = station,
                status = PlayerStatus.BUFFERING,
                liveTrackTitle = station.name,
                liveArtist = station.genre,
                errorMessage = null
            )
        }
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_PLAY
            putExtra(RadioPlaybackService.EXTRA_STREAM_URL, station.streamUrl)
            putExtra(RadioPlaybackService.EXTRA_STATION_NAME, station.name)
            putExtra(RadioPlaybackService.EXTRA_STATION_GENRE, station.genre)
        }
        startService(context, intent)
    }

    fun togglePlayPause(context: Context) {
        val current = _uiState.value
        val station = current.currentStation ?: getPersistedStation(context) ?: return

        if (current.status == PlayerStatus.PLAYING) {
            pause(context)
        } else {
            // For live radio streams, resuming after a pause, phone call, or app switch
            // must reconnect cleanly to the live broadcast with full station details.
            playStation(context, station)
        }
    }

    fun pause(context: Context) {
        _uiState.update { it.copy(status = PlayerStatus.PAUSED) }
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_PAUSE
        }
        startService(context, intent)
    }

    fun resume(context: Context) {
        val current = _uiState.value
        val station = current.currentStation ?: getPersistedStation(context) ?: return
        playStation(context, station)
    }

    fun stop(context: Context) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_STOP
        }
        startService(context, intent)
        _uiState.update {
            it.copy(
                status = PlayerStatus.IDLE,
                errorMessage = null
            )
        }
    }

    private fun persistStation(context: Context, station: RadioStation) {
        try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_STATION_ID, station.id)
                .putString(KEY_STATION_NAME, station.name)
                .putString(KEY_STATION_STREAM_URL, station.streamUrl)
                .putString(KEY_STATION_GENRE, station.genre)
                .putString(KEY_STATION_FAVICON, station.faviconUrl.orEmpty())
                .putInt(KEY_STATION_BITRATE, station.bitrate)
                .putBoolean(KEY_STATION_IS_CUSTOM, station.isCustom)
                .putBoolean(KEY_STATION_IS_FAVORITE, station.isFavorite)
                .apply()
        } catch (_: Exception) {}
    }

    fun getPersistedStation(context: Context): RadioStation? {
        return try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val streamUrl = prefs.getString(KEY_STATION_STREAM_URL, null) ?: return null
            val id = prefs.getString(KEY_STATION_ID, streamUrl) ?: streamUrl
            val name = prefs.getString(KEY_STATION_NAME, "Galaxy Radio") ?: "Galaxy Radio"
            val genre = prefs.getString(KEY_STATION_GENRE, "Live Radio") ?: "Live Radio"
            val favicon = prefs.getString(KEY_STATION_FAVICON, null)?.ifBlank { null }
            val bitrate = prefs.getInt(KEY_STATION_BITRATE, 128)
            val isCustom = prefs.getBoolean(KEY_STATION_IS_CUSTOM, false)
            val isFavorite = prefs.getBoolean(KEY_STATION_IS_FAVORITE, false)

            RadioStation(
                id = id,
                name = name,
                streamUrl = streamUrl,
                genre = genre,
                faviconUrl = favicon,
                bitrate = bitrate,
                isCustom = isCustom,
                isFavorite = isFavorite
            )
        } catch (_: Exception) {
            null
        }
    }

    private var stationQueue: List<RadioStation> = emptyList()

    fun setStationQueue(stations: List<RadioStation>) {
        if (stations.isNotEmpty()) {
            stationQueue = stations
        }
    }

    fun playNext(context: Context) {
        val current = _uiState.value.currentStation
        val queue = if (stationQueue.isNotEmpty()) stationQueue else com.example.data.repository.RadioStationRepository.CURATED_GALAXY_STATIONS
        if (queue.isEmpty()) return

        val currentIndex = queue.indexOfFirst { it.id == current?.id || it.streamUrl == current?.streamUrl }
        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % queue.size else 0
        playStation(context, queue[nextIndex])
    }

    fun playPrevious(context: Context) {
        val current = _uiState.value.currentStation
        val queue = if (stationQueue.isNotEmpty()) stationQueue else com.example.data.repository.RadioStationRepository.CURATED_GALAXY_STATIONS
        if (queue.isEmpty()) return

        val currentIndex = queue.indexOfFirst { it.id == current?.id || it.streamUrl == current?.streamUrl }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else queue.size - 1
        playStation(context, queue[prevIndex])
    }

    fun toggleMute(context: Context) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_MUTE_TOGGLE
        }
        startService(context, intent)
    }

    fun setQualityPreset(preset: AudioQualityPreset) {
        _uiState.update { it.copy(audioQuality = preset) }
    }

    internal fun updateMuted(isMuted: Boolean) {
        _uiState.update { it.copy(isMuted = isMuted) }
    }

    // Internal updates from service
    internal fun updateStatus(status: PlayerStatus, error: String? = null) {
        _uiState.update {
            it.copy(
                status = status,
                errorMessage = error
            )
        }
    }

    internal fun updateMetadata(trackTitle: String, artist: String) {
        _uiState.update {
            it.copy(
                liveTrackTitle = trackTitle,
                liveArtist = artist
            )
        }
    }

    internal fun updateAudioSessionId(sessionId: Int) {
        _uiState.update { it.copy(audioSessionId = sessionId) }
    }

    internal fun updateStats(bytes: Long, durationSec: Long) {
        _uiState.update {
            it.copy(
                bytesStreamed = bytes,
                streamDurationSeconds = durationSec
            )
        }
    }

    internal fun updateSleepTimer(seconds: Long?) {
        _uiState.update {
            it.copy(sleepTimerRemainingSeconds = seconds)
        }
    }

    private fun startService(context: Context, intent: Intent) {
        try {
            val appContext = context.applicationContext ?: context
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }
        } catch (_: Exception) {
            // Background service start restrictions handling
        }
    }
}
