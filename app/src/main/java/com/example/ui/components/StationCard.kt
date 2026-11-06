package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.RadioStation
import com.example.ui.theme.GalaxyCardBorder
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyElectricBlue
import com.example.ui.theme.GalaxyError
import com.example.ui.theme.GalaxySurface
import com.example.ui.theme.GalaxySurfaceVariant
import com.example.ui.theme.GalaxyTextMuted
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary
import com.example.ui.theme.GalaxyViolet

@Composable
fun StationCard(
    station: RadioStation,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onSelect: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteStation: (() -> Unit)? = null,
    onViewHistory: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isCurrent) GalaxyCyan.copy(alpha = 0.8f) else GalaxyCardBorder
    val surfaceColor = if (isCurrent) GalaxySurfaceVariant else GalaxySurface

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .testTag("station_card_${station.id}"),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (isCurrent) 6.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station Icon / Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(GalaxyViolet.copy(alpha = 0.35f), GalaxyDeepTone)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!station.faviconUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = station.faviconUrl,
                        contentDescription = station.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(50.dp)
                    )
                } else {
                    Text(
                        text = station.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GalaxyCyan
                    )
                }

                if (isCurrent && isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MinimalistAudioWave(
                            isPlaying = true,
                            barCount = 3,
                            barWidth = 2.dp,
                            maxHeight = 10.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Station Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = GalaxyTextPrimary
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )

                    if (station.isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorited",
                            tint = GalaxyCyan,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    if (station.isCustom) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GalaxyCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "CUSTOM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GalaxyCyan,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    Text(
                        text = station.displayGenre,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GalaxyTextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall.copy(color = GalaxyTextMuted)
                    )
                    Text(
                        text = station.displayBitrate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GalaxyElectricBlue,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Actions: Tiered Menu (3 dots) & Play/Pause (left in place)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                var menuExpanded by remember { mutableStateOf(false) }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("station_menu_button_${station.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Station options",
                            tint = GalaxyTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(GalaxySurface)
                            .border(1.dp, GalaxyCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (station.isFavorite) "Remove from Favorites" else "Add to Favorites",
                                    color = GalaxyTextPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (station.isFavorite) GalaxyCyan else GalaxyTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleFavorite()
                            },
                            modifier = Modifier.testTag("favorite_button_${station.id}")
                        )

                        if (onViewHistory != null) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Station History",
                                        color = GalaxyTextPrimary,
                                        fontSize = 14.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = null,
                                        tint = GalaxyCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onViewHistory()
                                },
                                modifier = Modifier.testTag("history_button_${station.id}")
                            )
                        }

                        if (onDeleteStation != null) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (station.isCustom) "Delete Custom Stream" else "Delete Station",
                                        color = GalaxyError,
                                        fontSize = 14.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = GalaxyError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteStation()
                                },
                                modifier = Modifier.testTag("delete_button_${station.id}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Play / Pause / Buffering button
                Surface(
                    shape = CircleShape,
                    color = if (isCurrent && isPlaying) GalaxyCyan else GalaxySurfaceVariant,
                    border = BorderStroke(1.dp, if (isCurrent) GalaxyCyan else GalaxyCardBorder),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(onClick = {
                            if (isCurrent) onTogglePlayPause() else onSelect()
                        })
                        .testTag("play_pause_button_${station.id}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrent && isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = if (isPlaying) Color.Black else GalaxyCyan
                            )
                        } else {
                            Icon(
                                imageVector = if (isCurrent && isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isCurrent && isPlaying) "Pause" else "Play",
                                tint = if (isCurrent && isPlaying) Color.Black else GalaxyCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private val GalaxyDeepTone = Color(0xFF0F1424)
