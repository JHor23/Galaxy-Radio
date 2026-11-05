package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SongHistoryEntity
import com.example.data.repository.SongHistoryRepository
import com.example.model.RadioStation
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StationHistoryModal(
    station: RadioStation?,
    playerState: PlayerUiState,
    songHistoryRepository: SongHistoryRepository,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedScopeTab by remember { mutableIntStateOf(0) } // 0 = This Station, 1 = All Stations
    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val targetStationId = station?.id ?: playerState.currentStation?.id ?: ""
    val targetStreamUrl = station?.streamUrl ?: playerState.currentStation?.streamUrl ?: ""
    val targetStationName = station?.name ?: playerState.currentStation?.name ?: "Galaxy Stream"

    val stationHistory by songHistoryRepository
        .getHistoryForStation(targetStationId, targetStreamUrl)
        .collectAsState(initial = emptyList())

    val allHistory by songHistoryRepository
        .getAllHistory()
        .collectAsState(initial = emptyList())

    val currentList = if (selectedScopeTab == 0 && targetStationId.isNotBlank()) stationHistory else allHistory

    val filteredList = remember(currentList, searchQuery) {
        if (searchQuery.isBlank()) {
            currentList
        } else {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            currentList.filter {
                it.trackTitle.lowercase(Locale.ROOT).contains(q) ||
                it.artist.lowercase(Locale.ROOT).contains(q) ||
                it.stationName.lowercase(Locale.ROOT).contains(q)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("station_history_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = GalaxyCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GalaxyCyan),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = GalaxyCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Station History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GalaxyTextPrimary
                        )
                        Text(
                            text = if (selectedScopeTab == 0 && targetStationName.isNotBlank()) targetStationName else "All Played Cosmic Tracks",
                            fontSize = 12.sp,
                            color = GalaxyTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentList.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = GalaxyTextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_history_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = GalaxyTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scope Tabs (This Station vs All Streams)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GalaxySurface,
                border = BorderStroke(1.dp, GalaxyCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (selectedScopeTab == 0) GalaxyCyan else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { selectedScopeTab = 0 }
                            .testTag("history_tab_current_station")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Radio,
                                contentDescription = null,
                                tint = if (selectedScopeTab == 0) GalaxyVoid else GalaxyTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "This Station (${stationHistory.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedScopeTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedScopeTab == 0) GalaxyVoid else GalaxyTextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (selectedScopeTab == 1) GalaxyCyan else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { selectedScopeTab = 1 }
                            .testTag("history_tab_all_stations")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = if (selectedScopeTab == 1) GalaxyVoid else GalaxyTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All History (${allHistory.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedScopeTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedScopeTab == 1) GalaxyVoid else GalaxyTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search filter bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter track title or artist...", color = GalaxyTextMuted, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = GalaxyCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = GalaxyTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GalaxyCyan,
                    unfocusedBorderColor = GalaxyCardBorder,
                    focusedTextColor = GalaxyTextPrimary,
                    unfocusedTextColor = GalaxyTextPrimary,
                    cursorColor = GalaxyCyan,
                    focusedContainerColor = GalaxySurface,
                    unfocusedContainerColor = GalaxySurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_history_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Currently Playing Card (if listening to this station right now)
            val currentStation = playerState.currentStation
            val isListeningToThis = currentStation != null && (currentStation.id == targetStationId || currentStation.streamUrl == targetStreamUrl)
            if (isListeningToThis && playerState.displayTrackTitle.isNotBlank() && selectedScopeTab == 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GalaxyCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            MinimalistAudioWave(
                                isPlaying = playerState.isPlaying,
                                barCount = 3,
                                barWidth = 2.5.dp,
                                maxHeight = 12.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NOW PLAYING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GalaxyCyan,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = playerState.displayTrackTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GalaxyTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = playerState.displayArtist,
                                    fontSize = 11.sp,
                                    color = GalaxyTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                copyToClipboard(context, "${playerState.displayTrackTitle} - ${playerState.displayArtist}")
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Track",
                                tint = GalaxyCyan,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            // History List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GalaxySurfaceVariant,
                            border = BorderStroke(1.dp, GalaxyCardBorder),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    tint = GalaxyTextMuted,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No tracks matched \"$searchQuery\"" else "No Song History Recorded",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GalaxyTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Try searching for a different track title or artist"
                            } else {
                                "As you listen to cosmic streams and radio stations, previously played tracks are automatically logged here in real-time."
                            },
                            fontSize = 12.sp,
                            color = GalaxyTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        SongHistoryRowItem(
                            song = item,
                            showStationName = selectedScopeTab == 1,
                            onCopy = {
                                copyToClipboard(context, "${item.trackTitle} - ${item.artist}")
                            },
                            onSearchWeb = {
                                searchWeb(context, "${item.artist} ${item.trackTitle}")
                            }
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = if (selectedScopeTab == 0) "Clear Station History?" else "Clear All History?",
                    color = GalaxyTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (selectedScopeTab == 0) {
                        "Are you sure you want to remove recorded song history for $targetStationName?"
                    } else {
                        "Are you sure you want to clear song history for all stations?"
                    },
                    color = GalaxyTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (selectedScopeTab == 0) {
                                songHistoryRepository.clearHistoryForStation(targetStationId, targetStreamUrl)
                            } else {
                                songHistoryRepository.clearAllHistory()
                            }
                        }
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GalaxyViolet)
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = GalaxyTextSecondary)
                }
            },
            containerColor = GalaxyDeep
        )
    }
}

@Composable
fun SongHistoryRowItem(
    song: SongHistoryEntity,
    showStationName: Boolean,
    onCopy: () -> Unit,
    onSearchWeb: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GalaxySurface,
        border = BorderStroke(1.dp, GalaxyCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_item_${song.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.trackTitle,
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
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GalaxyCyan,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " • ",
                        color = GalaxyTextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = formatRelativeTime(song.playedAtTimestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GalaxyTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
                if (showStationName && song.stationName.isNotBlank()) {
                    Text(
                        text = song.stationName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GalaxyElectricBlue,
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("copy_history_item_${song.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy Track",
                        tint = GalaxyTextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onSearchWeb,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("search_history_item_${song.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInNew,
                        contentDescription = "Search Track Online",
                        tint = GalaxyCyan,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Cosmic Track", text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Copied \"$text\" to clipboard", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {}
}

private fun searchWeb(context: Context, query: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query)))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)))
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {}
    }
}

private fun formatRelativeTime(timestampMs: Long): String {
    val diffSec = ((System.currentTimeMillis() - timestampMs) / 1000).coerceAtLeast(0)
    return when {
        diffSec < 60 -> "Just now"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        diffSec < 172800 -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            sdf.format(Date(timestampMs))
        }
    }
}
