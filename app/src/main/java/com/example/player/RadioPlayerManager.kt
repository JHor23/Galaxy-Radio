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

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun playStation(context: Context, station: RadioStation) {
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
        val station = current.currentStation ?: return

        if (current.status == PlayerStatus.ERROR || current.status == PlayerStatus.IDLE) {
            playStation(context, station)
        } else {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = RadioPlaybackService.ACTION_TOGGLE
            }
            startService(context, intent)
        }
    }

    fun pause(context: Context) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_PAUSE
        }
        startService(context, intent)
    }

    fun resume(context: Context) {
        val current = _uiState.value
        val station = current.currentStation ?: return

        if (current.status == PlayerStatus.ERROR || current.status == PlayerStatus.IDLE) {
            playStation(context, station)
        } else {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = RadioPlaybackService.ACTION_RESUME
            }
            startService(context, intent)
        }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {
            // Background service start restrictions handling
        }
    }
}
