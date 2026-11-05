package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.PlayerUiState
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyElectricBlue
import com.example.ui.theme.GalaxySurfaceVariant
import com.example.ui.theme.GalaxyTextMuted
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary

@Composable
fun MiniPlayerBar(
    playerState: PlayerUiState,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val station = playerState.currentStation ?: return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clickable(onClick = onExpand)
            .testTag("mini_player_bar"),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = GalaxySurfaceVariant,
        border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.45f)),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Visualizer or Pulsing Ring
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        MinimalistAudioWave(
                            isPlaying = playerState.isPlaying,
                            barCount = 4,
                            barWidth = 2.5.dp,
                            maxHeight = 14.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playerState.displayTrackTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = GalaxyTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GalaxyCyan,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    val sleepSec = playerState.sleepTimerRemainingSeconds
                    if (sleepSec != null && sleepSec > 0) {
                        val m = sleepSec / 60
                        val s = sleepSec % 60
                        val countdown = String.format("%02d:%02d", m, s)
                        Text(
                            text = " • ⏳ $countdown",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GalaxyCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Text(
                            text = " • ",
                            color = GalaxyTextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = playerState.formattedDataUsage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GalaxyElectricBlue,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play / Pause / Buffering Action
            Surface(
                shape = CircleShape,
                color = if (playerState.isPlaying) GalaxyCyan else Color.Transparent,
                border = BorderStroke(1.dp, GalaxyCyan),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTogglePlayPause)
                    .testTag("mini_player_toggle")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (playerState.isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = GalaxyCyan
                        )
                    } else {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = if (playerState.isPlaying) Color.Black else GalaxyCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("mini_player_stop")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Stop stream",
                    tint = GalaxyTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
