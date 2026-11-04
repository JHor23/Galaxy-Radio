package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.GalaxyRadioApplication
import com.example.MainActivity
import com.example.R
import com.example.audio.IcyMetadataReader
import com.example.data.crawler.AudioStreamCrawler
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
    private var streamPrepareJob: Job? = null
    private var metadataJob: Job? = null
    private var statsJob: Job? = null

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSessionCompat? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var currentUrl: String = ""
    private var currentStationName: String = "Galaxy Radio"
    private var currentGenre: String = "Live Space Radio"
    private var currentTrackTitle: String = ""
    private var currentArtist: String = ""
    private var sessionStartTimeMs: Long = 0L
    private var estimatedBytesTransferred: Long = 0L
    private var isMuted: Boolean = false

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GalaxyRadio:WakeLock")

        initializeMediaSession()
        initializePlayer()
    }

    private fun initializeMediaSession() {
        try {
            mediaSession = MediaSessionCompat(this, "GalaxyRadioMediaSession").apply {
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                )

                val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    setClass(this@RadioPlaybackService, RadioPlaybackService::class.java)
                }
                val pendingMediaButton = PendingIntent.getService(
                    this@RadioPlaybackService,
                    0,
                    mediaButtonIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                setMediaButtonReceiver(pendingMediaButton)

                isActive = true
                setCallback(object : MediaSessionCompat.Callback() {
                    override fun onPlay() {
                        player?.play()
                    }

                    override fun onPause() {
                        player?.pause()
                    }

                    override fun onStop() {
                        stopPlayback()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }

                    override fun onSkipToNext() {
                        RadioPlayerManager.playNext(this@RadioPlaybackService)
                    }

                    override fun onSkipToPrevious() {
                        RadioPlayerManager.playPrevious(this@RadioPlaybackService)
                    }

                    override fun onCustomAction(action: String?, extras: Bundle?) {
                        if (action == ACTION_MUTE_TOGGLE) {
                            toggleMute()
                        }
                    }
                })
            }
        } catch (_: Exception) {}
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

        val defaultRequestHeaders = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Accept" to "*/*",
            "Icy-MetaData" to "1"
        )

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .setDefaultRequestProperties(defaultRequestHeaders)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(httpDataSourceFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true) // handle audio focus
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(playerListener)
                volume = if (isMuted) 0f else 1f
            }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    RadioPlayerManager.updateStatus(PlayerStatus.BUFFERING)
                    updateMediaSessionState(PlaybackStateCompat.STATE_BUFFERING)
                    updateMediaSessionMetadata()
                    updateNotification()
                }
                Player.STATE_READY -> {
                    val isPlaying = player?.isPlaying == true
                    RadioPlayerManager.updateStatus(if (isPlaying) PlayerStatus.PLAYING else PlayerStatus.PAUSED)
                    updateMediaSessionState(if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED)
                    updateMediaSessionMetadata()
                    player?.audioSessionId?.let { sessionId ->
                        if (sessionId > 0) RadioPlayerManager.updateAudioSessionId(sessionId)
                    }
                    updateNotification()
                }
                Player.STATE_ENDED -> {
                    RadioPlayerManager.updateStatus(PlayerStatus.IDLE)
                    updateMediaSessionState(PlaybackStateCompat.STATE_STOPPED)
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
                updateMediaSessionState(PlaybackStateCompat.STATE_PLAYING)
                acquireWakeLock()
                startStatsTracker()
            } else {
                if (player?.playbackState != Player.STATE_BUFFERING) {
                    RadioPlayerManager.updateStatus(PlayerStatus.PAUSED)
                    updateMediaSessionState(PlaybackStateCompat.STATE_PAUSED)
                }
                releaseWakeLock()
            }
            updateMediaSessionMetadata()
            updateNotification()
        }

        override fun onPlayerError(error: PlaybackException) {
            val rootCause = error.cause
            val isUnrecognizedFormat = rootCause is androidx.media3.exoplayer.source.UnrecognizedInputFormatException ||
                    (error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED) ||
                    (error.errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED)

            val httpException = rootCause as? androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException
            val isForbiddenOrAuth = httpException?.responseCode == 403 || httpException?.responseCode == 401
            val isNotFound = httpException?.responseCode == 404

            val friendlyMessage = when {
                isForbiddenOrAuth -> "Access to this station stream was blocked (403 Forbidden)"
                isNotFound -> "Station stream not found (404)"
                isUnrecognizedFormat -> "Audio stream format unrecognized or offline"
                else -> error.localizedMessage ?: "Stream connection error"
            }
            val failedUrl = currentUrl

            player?.stop()
            player?.clearMediaItems()

            // If the stream failed with 403, unrecognized format, or was a webpage, attempt an automatic crawler recovery
            val shouldAttemptCrawl = failedUrl.isNotBlank() &&
                    (isForbiddenOrAuth || isUnrecognizedFormat || failedUrl.contains("/player") ||
                            failedUrl.contains(".htm") || failedUrl.contains(".php") ||
                            failedUrl.contains("happyradio.fm") || failedUrl.contains(".pls") || failedUrl.contains(".m3u"))

            if (shouldAttemptCrawl) {
                serviceScope.launch(Dispatchers.IO) {
                    val crawlResult = AudioStreamCrawler.crawlForAudioFeed(failedUrl)
                    val crawled = crawlResult.getOrNull()
                    if (crawled != null && crawled.resolvedStreamUrl != failedUrl) {
                        launch(Dispatchers.Main) {
                            startStreaming(crawled.resolvedStreamUrl, crawled.stationName, crawled.genre)
                        }
                        return@launch
                    }
                    launch(Dispatchers.Main) {
                        RadioPlayerManager.updateStatus(PlayerStatus.ERROR, friendlyMessage)
                        updateMediaSessionState(PlaybackStateCompat.STATE_ERROR)
                        releaseWakeLock()
                        updateNotification()
                    }
                }
            } else {
                RadioPlayerManager.updateStatus(PlayerStatus.ERROR, friendlyMessage)
                updateMediaSessionState(PlaybackStateCompat.STATE_ERROR)
                releaseWakeLock()
                updateNotification()
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            val title = mediaMetadata.title?.toString()
            val artist = mediaMetadata.artist?.toString()

            if (!title.isNullOrBlank()) {
                currentTrackTitle = title
                currentArtist = artist ?: currentStationName
                RadioPlayerManager.updateMetadata(currentTrackTitle, currentArtist)
                updateMediaSessionMetadata()
                updateNotification()
            }
        }
    }

    private fun updateMediaSessionState(state: Int) {
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED

        try {
            val customMuteAction = PlaybackStateCompat.CustomAction.Builder(
                ACTION_MUTE_TOGGLE,
                if (isMuted) "Unmute" else "Mute",
                if (isMuted) android.R.drawable.ic_lock_silent_mode else android.R.drawable.ic_lock_silent_mode_off
            ).build()

            mediaSession?.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(actions)
                    .addCustomAction(customMuteAction)
                    .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                    .build()
            )
        } catch (_: Exception) {}
    }

    private fun updateMediaSessionMetadata() {
        try {
            val title = currentTrackTitle.ifBlank { currentStationName }
            val artist = if (currentArtist.isNotBlank() && currentArtist != currentStationName) {
                "$currentArtist • $currentStationName"
            } else {
                currentStationName
            }

            val largeIconBitmap = try {
                BitmapFactory.decodeResource(resources, R.drawable.galaxy_radio_icon)
            } catch (_: Exception) {
                null
            }

            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentStationName)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, largeIconBitmap)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, largeIconBitmap)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, largeIconBitmap)
                .build()

            mediaSession?.setMetadata(metadata)
        } catch (_: Exception) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ALWAYS satisfy the Android foreground service requirement immediately
        startForegroundWithNotification()

        // Handle hardware media buttons (headset, Bluetooth, etc.)
        MediaButtonReceiver.handleIntent(mediaSession, intent)

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
            ACTION_NEXT -> {
                RadioPlayerManager.playNext(this)
            }
            ACTION_PREVIOUS -> {
                RadioPlayerManager.playPrevious(this)
            }
            ACTION_MUTE_TOGGLE -> {
                toggleMute()
            }
            ACTION_STOP -> {
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun toggleMute() {
        isMuted = !isMuted
        player?.volume = if (isMuted) 0f else 1f
        RadioPlayerManager.updateMuted(isMuted)
        updateMediaSessionState(
            if (player?.isPlaying == true) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        )
        updateNotification()
    }

    private fun startStreaming(url: String, name: String, genre: String) {
        if (url.isBlank()) return

        // 1. Immediately cancel any pending prepare or metadata jobs
        streamPrepareJob?.cancel()
        metadataJob?.cancel()
        statsJob?.cancel()

        // 2. Immediately stop and clear any currently playing stream from the previous station
        player?.stop()
        player?.clearMediaItems()

        // 3. Reset station and session metadata
        currentUrl = url
        currentStationName = name.ifBlank { "Galaxy Radio" }
        currentGenre = genre.ifBlank { "Live Stream" }
        currentTrackTitle = currentStationName
        currentArtist = currentGenre
        sessionStartTimeMs = System.currentTimeMillis()

        initializePlayer()
        RadioPlayerManager.updateMetadata(currentTrackTitle, currentArtist)
        RadioPlayerManager.updateStatus(PlayerStatus.BUFFERING)
        updateMediaSessionState(PlaybackStateCompat.STATE_BUFFERING)
        updateMediaSessionMetadata()
        updateNotification()

        streamPrepareJob = serviceScope.launch {
            var targetStreamUrl = url
            var targetName = currentStationName
            var targetGenre = currentGenre

            // Only crawl if it is an explicit web player page or playlist
            val lower = url.lowercase().trim()
            val isWebpageOrPlaylist = lower.contains("/player") ||
                    lower.contains(".html") || lower.contains(".htm") || lower.contains(".php") ||
                    lower.contains("happyradio.fm") || lower.contains("wixsite.com") ||
                    lower.endsWith(".pls") || lower.endsWith(".m3u")

            if (isWebpageOrPlaylist) {
                val crawlResult = AudioStreamCrawler.crawlForAudioFeed(url)
                val crawled = crawlResult.getOrNull()
                if (crawled != null) {
                    targetStreamUrl = crawled.resolvedStreamUrl
                    if (targetName == "Galaxy Radio" || targetName == "Custom Station" || targetName.isBlank()) {
                        targetName = crawled.stationName
                    }
                    if (crawled.genre.isNotBlank() && crawled.genre != "Online Radio") {
                        targetGenre = crawled.genre
                    }
                    currentStationName = targetName
                    currentGenre = targetGenre
                    currentUrl = targetStreamUrl
                    currentTrackTitle = targetName
                    currentArtist = targetGenre
                    RadioPlayerManager.updateMetadata(currentTrackTitle, currentArtist)
                    updateMediaSessionMetadata()
                    updateNotification()
                } else {
                    // Crawl failed: do not feed raw HTML to ExoPlayer as it causes UnrecognizedInputFormatException
                    RadioPlayerManager.updateStatus(PlayerStatus.ERROR, "No active audio stream found at this webpage")
                    updateMediaSessionState(PlaybackStateCompat.STATE_ERROR)
                    releaseWakeLock()
                    updateNotification()
                    return@launch
                }
            }

            try {
                val mediaItemBuilder = MediaItem.Builder()
                    .setUri(targetStreamUrl)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(currentStationName)
                            .setArtist(currentGenre)
                            .build()
                    )

                val targetLower = targetStreamUrl.lowercase()
                when {
                    targetLower.contains(".m3u8") -> mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
                    targetLower.endsWith(".mp3") -> mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.AUDIO_MPEG)
                    targetLower.endsWith(".aac") -> mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.AUDIO_AAC)
                    targetLower.endsWith(".ogg") || targetLower.endsWith(".opus") -> mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.AUDIO_OGG)
                }

                val mediaItem = mediaItemBuilder.build()

                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.play()

                startIcyMetadataPolling()
            } catch (e: Exception) {
                RadioPlayerManager.updateStatus(PlayerStatus.ERROR, e.localizedMessage ?: "Failed to start stream")
                updateMediaSessionState(PlaybackStateCompat.STATE_ERROR)
            }
        }
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
                            updateMediaSessionMetadata()
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
        try {
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
        } catch (_: Exception) {}
    }

    private fun updateNotification() {
        try {
            val manager = getSystemService(NOTIFICATION_SERVICE) as? android.app.NotificationManager
            manager?.notify(NOTIFICATION_ID, buildNotification())
        } catch (_: Exception) {}
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

        val prevActionIntent = PendingIntent.getService(
            this,
            10,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_PREVIOUS },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleActionIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_TOGGLE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextActionIntent = PendingIntent.getService(
            this,
            11,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val muteActionIntent = PendingIntent.getService(
            this,
            12,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_MUTE_TOGGLE },
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

        val sleepSec = RadioPlayerManager.uiState.value.sleepTimerRemainingSeconds
        val sleepNotice = if (sleepSec != null && sleepSec > 0) {
            val min = (sleepSec + 59) / 60
            " • ⏳ Sleep in ${min}m"
        } else ""

        val muteNotice = if (isMuted) " • 🔇 Muted" else ""

        val subText = if (sleepSec != null && sleepSec > 0) {
            val min = (sleepSec + 59) / 60
            "Mini Player • Sleep ${min}m"
        } else {
            "Mini Player • Live Stream"
        }

        val largeIconBitmap = try {
            BitmapFactory.decodeResource(resources, R.drawable.galaxy_radio_icon)
        } catch (_: Exception) {
            null
        }

        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession?.sessionToken)
            .setShowActionsInCompactView(0, 1, 2) // Action 0: Previous, Action 1: Play/Pause, Action 2: Next
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopActionIntent)

        return NotificationCompat.Builder(this, GalaxyRadioApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_radio_notification)
            .apply {
                if (largeIconBitmap != null) {
                    setLargeIcon(largeIconBitmap)
                }
            }
            .setContentTitle(title)
            .setContentText("$subtitle$sleepNotice$muteNotice")
            .setSubText(subText)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setColor(0xFF00F2FE.toInt()) // Cyan accent color
            .setColorized(true)
            .setOngoing(isPlaying)
            // Action 0: Previous station
            .addAction(
                android.R.drawable.ic_media_previous,
                "Previous",
                prevActionIntent
            )
            // Action 1: Play / Pause toggle
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                toggleActionIntent
            )
            // Action 2: Next station
            .addAction(
                android.R.drawable.ic_media_next,
                "Next",
                nextActionIntent
            )
            // Action 3: Volume Mute / Unmute
            .addAction(
                if (isMuted) android.R.drawable.ic_lock_silent_mode else android.R.drawable.ic_lock_silent_mode_off,
                if (isMuted) "Unmute" else "Mute",
                muteActionIntent
            )
            // Action 4: Stop
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopActionIntent
            )
            .setStyle(mediaStyle)
            .build()
    }

    private fun stopPlayback() {
        streamPrepareJob?.cancel()
        metadataJob?.cancel()
        statsJob?.cancel()
        player?.stop()
        player?.clearMediaItems()
        updateMediaSessionState(PlaybackStateCompat.STATE_STOPPED)
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
        mediaSession?.release()
        mediaSession = null
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
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.ACTION_PREVIOUS"
        const val ACTION_MUTE_TOGGLE = "com.example.ACTION_MUTE_TOGGLE"

        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_STATION_NAME = "extra_station_name"
        const val EXTRA_STATION_GENRE = "extra_station_genre"
    }
}
