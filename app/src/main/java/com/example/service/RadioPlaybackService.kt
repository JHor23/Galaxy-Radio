package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.example.GalaxyRadioApplication
import com.example.MainActivity
import com.example.R
import com.example.audio.IcyMetadataReader
import com.example.player.PlayerStatus
import com.example.player.RadioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RadioPlaybackService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var metadataJob: Job? = null
    private var statsJob: Job? = null

    private var player: ExoPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var currentUrl: String = ""
    private var currentStationName: String = "Galaxy Radio"
    private var currentGenre: String = "Live Space Radio"
    private var currentTrackTitle: String = ""
    private var currentArtist: String = ""
    private var sessionStartTimeMs: Long = 0L
    private var estimatedBytesTransferred: Long = 0L

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GalaxyRadio:WakeLock")
        initializePlayer()
    }

    private fun initializePlayer() {
        if (player != null) return

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2_500,  // minBufferMs
                10_000, // maxBufferMs
                1_000,  // bufferForPlaybackMs
                2_000   // bufferForPlaybackAfterRebufferMs
            )
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true) // handle audio focus
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(playerListener)
            }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    RadioPlayerManager.updateStatus(PlayerStatus.BUFFERING)
                    updateNotification()
                }
                Player.STATE_READY -> {
                    val isPlaying = player?.isPlaying == true
                    RadioPlayerManager.updateStatus(if (isPlaying) PlayerStatus.PLAYING else PlayerStatus.PAUSED)
                    player?.audioSessionId?.let { sessionId ->
                        if (sessionId > 0) RadioPlayerManager.updateAudioSessionId(sessionId)
                    }
                    updateNotification()
                }
                Player.STATE_ENDED -> {
                    RadioPlayerManager.updateStatus(PlayerStatus.IDLE)
                    releaseWakeLock()
                }
                Player.STATE_IDLE -> {
                    // Idle
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                RadioPlayerManager.updateStatus(PlayerStatus.PLAYING)
                acquireWakeLock()
                startStatsTracker()
            } else {
                if (player?.playbackState != Player.STATE_BUFFERING) {
                    RadioPlayerManager.updateStatus(PlayerStatus.PAUSED)
                }
                releaseWakeLock()
            }
            updateNotification()
        }

        override fun onPlayerError(error: PlaybackException) {
            val message = error.localizedMessage ?: "Stream connection error"
            RadioPlayerManager.updateStatus(PlayerStatus.ERROR, message)
            releaseWakeLock()
            updateNotification()
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            val title = mediaMetadata.title?.toString()
            val artist = mediaMetadata.artist?.toString()

            if (!title.isNullOrBlank()) {
                currentTrackTitle = title
                currentArtist = artist ?: currentStationName
                RadioPlayerManager.updateMetadata(currentTrackTitle, currentArtist)
                updateNotification()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val url = intent.getStringExtra(EXTRA_STREAM_URL).orEmpty()
                val name = intent.getStringExtra(EXTRA_STATION_NAME).orEmpty()
                val genre = intent.getStringExtra(EXTRA_STATION_GENRE).orEmpty()
                startStreaming(url, name, genre)
            }
            ACTION_TOGGLE -> {
                if (player?.isPlaying == true) {
                    player?.pause()
                } else {
                    player?.play()
                }
            }
            ACTION_PAUSE -> {
                player?.pause()
            }
            ACTION_RESUME -> {
                player?.play()
            }
            ACTION_STOP -> {
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startStreaming(url: String, name: String, genre: String) {
        if (url.isBlank()) return

        currentUrl = url
        currentStationName = name.ifBlank { "Galaxy Radio" }
        currentGenre = genre.ifBlank { "Live Stream" }
        currentTrackTitle = currentStationName
        currentArtist = currentGenre
        sessionStartTimeMs = System.currentTimeMillis()

        initializePlayer()

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(currentStationName)
                    .setArtist(currentGenre)
                    .build()
            )
            .build()

        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()

        startForegroundWithNotification()
        startIcyMetadataPolling()
    }

    private fun startIcyMetadataPolling() {
        metadataJob?.cancel()
        metadataJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (currentUrl.isNotBlank()) {
                    val (artist, title) = IcyMetadataReader.fetchLiveMetadata(currentUrl)
                    if (!title.isNullOrBlank()) {
                        launch(Dispatchers.Main) {
                            currentTrackTitle = title
                            currentArtist = if (!artist.isNullOrBlank()) artist else currentStationName
                            RadioPlayerManager.updateMetadata(currentTrackTitle, currentArtist)
                            updateNotification()
                        }
                    }
                }
                delay(12_000) // check metadata every 12 seconds
            }
        }
    }

    private fun startStatsTracker() {
        statsJob?.cancel()
        statsJob = serviceScope.launch {
            while (isActive) {
                if (player?.isPlaying == true) {
                    val elapsedSec = (System.currentTimeMillis() - sessionStartTimeMs) / 1000
                    // Estimate data rate based on 128kbps = 16 KB/sec
                    val quality = RadioPlayerManager.uiState.value.audioQuality
                    val bytesPerSec = (quality.targetBitrateKbps * 1024) / 8
                    estimatedBytesTransferred += bytesPerSec
                    RadioPlayerManager.updateStats(estimatedBytesTransferred, elapsedSec)
                }
                delay(1000)
            }
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as? android.app.NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val isPlaying = player?.isPlaying == true
        val toggleActionIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_TOGGLE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopActionIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = currentTrackTitle.ifBlank { currentStationName }
        val subtitle = if (currentArtist.isNotBlank() && currentArtist != currentStationName) {
            "$currentArtist • $currentStationName"
        } else {
            currentStationName
        }

        return NotificationCompat.Builder(this, GalaxyRadioApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.galaxy_radio_icon)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSubText("Live Radio")
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                toggleActionIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopActionIntent
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(subtitle)
                    .setBigContentTitle(title)
                    .setSummaryText("Galaxy Radio")
            )
            .build()
    }

    private fun stopPlayback() {
        metadataJob?.cancel()
        statsJob?.cancel()
        player?.stop()
        player?.clearMediaItems()
        releaseWakeLock()
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld != true) {
            wakeLock?.acquire(3 * 60 * 60 * 1000L) // 3 hours timeout safety
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onDestroy() {
        stopPlayback()
        player?.removeListener(playerListener)
        player?.release()
        player = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 4042

        const val ACTION_PLAY = "com.example.ACTION_PLAY"
        const val ACTION_TOGGLE = "com.example.ACTION_TOGGLE"
        const val ACTION_PAUSE = "com.example.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.ACTION_RESUME"
        const val ACTION_STOP = "com.example.ACTION_STOP"

        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_STATION_NAME = "extra_station_name"
        const val EXTRA_STATION_GENRE = "extra_station_genre"
    }
}
