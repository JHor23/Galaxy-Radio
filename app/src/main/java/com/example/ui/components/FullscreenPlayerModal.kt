package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.PlayerUiState
import com.example.ui.theme.GalaxyCardBorder
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyDeep
import com.example.ui.theme.GalaxyElectricBlue
import com.example.ui.theme.GalaxySurface
import com.example.ui.theme.GalaxySurfaceVariant
import com.example.ui.theme.GalaxyTextMuted
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary
import com.example.ui.theme.GalaxyViolet
import com.example.ui.theme.GalaxyVoid

@Composable
fun FullscreenPlayerModal(
    playerState: PlayerUiState,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToQuality: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val station = playerState.currentStation ?: return
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        GalaxyStarfield(modifier = Modifier.fillMaxSize(), starCount = 45)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Nav Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .testTag("dismiss_fullscreen_player")
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = GalaxyTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GalaxySurfaceVariant,
                    border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (playerState.isPlaying) GalaxyCyan else GalaxyTextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (playerState.isPlaying) "LIVE ORBIT" else "PAUSED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playerState.isPlaying) GalaxyCyan else GalaxyTextSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sleepSec = playerState.sleepTimerRemainingSeconds
                    if (sleepSec != null && sleepSec > 0) {
                        val m = sleepSec / 60
                        val s = sleepSec % 60
                        val countdownStr = String.format("%02d:%02d", m, s)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GalaxyCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, GalaxyCyan),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenSleepTimer)
                                .testTag("fullscreen_sleep_timer_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bedtime,
                                    contentDescription = "Sleep Timer Active",
                                    tint = GalaxyCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = countdownStr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GalaxyCyan
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onOpenSleepTimer,
                            modifier = Modifier.testTag("fullscreen_sleep_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = "Sleep Timer",
                                tint = GalaxyTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("fullscreen_favorite_toggle")
                    ) {
                        Icon(
                            imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (station.isFavorite) GalaxyCyan else GalaxyTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Center Planetary Visualizer
            Box(
                modifier = Modifier
                    .size(230.dp),
                contentAlignment = Alignment.Center
            ) {
                OrbitalPlanetaryVisualizer(
                    isPlaying = playerState.isPlaying,
                    isBuffering = playerState.isBuffering,
                    sizeDp = 230.dp
                )

                // Center Celestial Station Emblem
                Surface(
                    shape = CircleShape,
                    color = GalaxyDeep,
                    border = BorderStroke(2.dp, GalaxyCyan),
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(GalaxyViolet.copy(alpha = 0.4f), GalaxyDeep)
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Radio,
                            contentDescription = null,
                            tint = GalaxyCyan,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Live Station Metadata
            Text(
                text = station.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GalaxyTextPrimary
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Live Track Title
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GalaxySurfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, GalaxyCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NOW PLAYING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GalaxyCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = playerState.displayTrackTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = GalaxyTextPrimary
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        if (playerState.liveArtist.isNotBlank() && playerState.liveArtist != station.name) {
                            Text(
                                text = playerState.liveArtist,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GalaxyTextSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenHistory,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("now_playing_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "Station Track History",
                                tint = GalaxyCyan,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("Track Info", "${playerState.displayTrackTitle} - ${station.name}")
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "Track copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Track",
                                tint = GalaxyTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Audio telemetry pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TelemetryBadge(label = "BITRATE", value = station.displayBitrate)
                TelemetryBadge(label = "TIME", value = playerState.formattedDuration)
                TelemetryBadge(label = "STREAMED", value = playerState.formattedDataUsage)
                TelemetryBadge(label = "QUALITY", value = playerState.audioQuality.title)
            }

            val sleepRemainingSec = playerState.sleepTimerRemainingSeconds
            if (sleepRemainingSec != null && sleepRemainingSec > 0) {
                val m = sleepRemainingSec / 60
                val s = sleepRemainingSec % 60
                val countdownFormatted = String.format("%02d:%02d", m, s)
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GalaxyCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenSleepTimer)
                        .testTag("fullscreen_sleep_timer_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = GalaxyCyan.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Bedtime,
                                        contentDescription = "Sleep Timer Active",
                                        tint = GalaxyCyan,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SLEEP TIMER RUNNING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GalaxyCyan,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Pauses radio stream in $countdownFormatted",
                                    fontSize = 12.sp,
                                    color = GalaxyTextPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GalaxyVoid.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = countdownFormatted,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GalaxyCyan,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop Action
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("fullscreen_stop_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GalaxySurfaceVariant,
                        border = BorderStroke(1.dp, GalaxyCardBorder),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = "Stop",
                                tint = GalaxyTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Previous Station
                IconButton(
                    onClick = { com.example.player.RadioPlayerManager.playPrevious(context) },
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("fullscreen_prev_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GalaxySurfaceVariant,
                        border = BorderStroke(1.dp, GalaxyCardBorder),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous Station",
                                tint = GalaxyTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Play / Pause Pulsing Center Button
                Surface(
                    shape = CircleShape,
                    color = GalaxyCyan,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("fullscreen_play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (playerState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp,
                                color = GalaxyVoid
                            )
                        } else {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                tint = GalaxyVoid,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                // Next Station
                IconButton(
                    onClick = { com.example.player.RadioPlayerManager.playNext(context) },
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("fullscreen_next_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GalaxySurfaceVariant,
                        border = BorderStroke(1.dp, GalaxyCardBorder),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next Station",
                                tint = GalaxyTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Mute / Unmute Volume Action
                IconButton(
                    onClick = { com.example.player.RadioPlayerManager.toggleMute(context) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("fullscreen_mute_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GalaxySurfaceVariant,
                        border = BorderStroke(1.dp, if (playerState.isMuted) GalaxyCyan else GalaxyCardBorder),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (playerState.isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                contentDescription = if (playerState.isMuted) "Unmute" else "Mute",
                                tint = if (playerState.isMuted) GalaxyCyan else GalaxyTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Equalizer Shortcut Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GalaxySurfaceVariant,
                    border = BorderStroke(1.dp, GalaxyCardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onNavigateToEqualizer)
                        .testTag("fullscreen_eq_shortcut")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Equalizer,
                            contentDescription = "Equalizer",
                            tint = GalaxyCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Acoustic Equalizer & DSP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GalaxyTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Jump Action Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Equalizer
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GalaxySurface,
                    border = BorderStroke(1.dp, GalaxyCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onNavigateToEqualizer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Equalizer,
                            contentDescription = null,
                            tint = GalaxyCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Equalizer",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GalaxyTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 2. History
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GalaxySurface,
                    border = BorderStroke(1.dp, GalaxyCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenHistory)
                        .testTag("fullscreen_history_tile")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = GalaxyElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "History",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GalaxyTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 3. Sleep Timer
                val sleepSec = playerState.sleepTimerRemainingSeconds
                val sleepLabel = if (sleepSec != null && sleepSec > 0) {
                    val m = sleepSec / 60
                    val s = sleepSec % 60
                    String.format("%02d:%02d", m, s)
                } else {
                    "Sleep"
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (sleepSec != null && sleepSec > 0) GalaxyCyan.copy(alpha = 0.15f) else GalaxySurface,
                    border = BorderStroke(1.dp, if (sleepSec != null && sleepSec > 0) GalaxyCyan else GalaxyCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenSleepTimer)
                        .testTag("fullscreen_bottom_sleep_timer")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bedtime,
                            contentDescription = null,
                            tint = if (sleepSec != null && sleepSec > 0) GalaxyCyan else GalaxyViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sleepLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (sleepSec != null && sleepSec > 0) GalaxyCyan else GalaxyTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 4. Quality
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GalaxySurface,
                    border = BorderStroke(1.dp, GalaxyCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onNavigateToQuality)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NetworkCheck,
                            contentDescription = null,
                            tint = GalaxyElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quality",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GalaxyTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TelemetryBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = GalaxySurface,
        border = BorderStroke(1.dp, GalaxyCardBorder.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = GalaxyTextMuted,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GalaxyCyan,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
