package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.RadioStationRepository
import com.example.model.RadioStation
import com.example.player.PlayerUiState
import com.example.ui.components.StationCard
import com.example.ui.theme.GalaxyCardBorder
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxySurface
import com.example.ui.theme.GalaxyTextMuted
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary
import com.example.ui.theme.GalaxyVoid

@Composable
fun SavedStationsScreen(
    repository: RadioStationRepository,
    playerState: PlayerUiState,
    onSelectStation: (RadioStation) -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onDeleteCustom: (RadioStation) -> Unit,
    onResetCurated: (() -> Unit)? = null,
    onOpenAddStation: () -> Unit,
    onNavigateToExplore: (() -> Unit)? = null,
    onViewHistory: ((RadioStation) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Custom, 1 = Favorites

    val customStations by repository.customStations.collectAsState(initial = emptyList())
    val favoriteStations by repository.favoriteStations.collectAsState(initial = emptyList())

    val displayedList = if (selectedTab == 0) customStations else favoriteStations

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Tab Selector Row
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = GalaxySurface,
            border = BorderStroke(1.dp, GalaxyCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                // Custom Tab
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedTab == 0) GalaxyCyan else GalaxySurface,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 0 }
                        .testTag("tab_custom_stations")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Radio,
                            contentDescription = null,
                            tint = if (selectedTab == 0) GalaxyVoid else GalaxyTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "My Custom (${customStations.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) GalaxyVoid else GalaxyTextSecondary
                        )
                    }
                }

                // Favorites Tab
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedTab == 1) GalaxyCyan else GalaxySurface,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 1 }
                        .testTag("tab_favorite_stations")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = if (selectedTab == 1) GalaxyVoid else GalaxyTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Favorites (${favoriteStations.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) GalaxyVoid else GalaxyTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val hiddenCuratedIds by repository.hiddenCuratedIds.collectAsState(initial = emptyList())
        val hasDeletedCurated = hiddenCuratedIds.isNotEmpty()

        // Header Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedTab == 0) "Custom Streams" else "Favorites",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GalaxyTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasDeletedCurated && onResetCurated != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GalaxyCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onResetCurated() }
                            .testTag("saved_screen_restore_curated_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = "Restore Curated Stations",
                                tint = GalaxyCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Restore (${hiddenCuratedIds.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GalaxyCyan,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (onNavigateToExplore != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GalaxyCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNavigateToExplore)
                            .testTag("saved_to_explore_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Radio,
                                contentDescription = "Explore Stations",
                                tint = GalaxyCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Explore",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GalaxyCyan,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (selectedTab == 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GalaxyCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenAddStation)
                            .testTag("saved_screen_add_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = GalaxyCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Stream",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GalaxyCyan,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // List or Empty View
        if (displayedList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Filled.Radio else Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = GalaxyCyan.copy(alpha = 0.6f),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (selectedTab == 0) "No Custom Streams Saved" else "No Favorites Yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GalaxyTextPrimary
                )
                Text(
                    text = if (selectedTab == 0) {
                        "Enter any live MP3/AAC internet radio URL to stream your own frequencies."
                    } else {
                        "Tap the star or heart icon on any station in the Explore tab to save it here."
                    },
                    fontSize = 13.sp,
                    color = GalaxyTextMuted,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )

                if (selectedTab == 0) {
                    Button(
                        onClick = onOpenAddStation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GalaxyCyan,
                            contentColor = GalaxyVoid
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_state_add_station_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Custom Station", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedList, key = { it.id }) { station ->
                    val isCurrent = playerState.currentStation?.id == station.id
                    StationCard(
                        station = station,
                        isCurrent = isCurrent,
                        isPlaying = isCurrent && playerState.isPlaying,
                        isBuffering = isCurrent && playerState.isBuffering,
                        onSelect = { onSelectStation(station) },
                        onTogglePlayPause = onTogglePlayPause,
                        onToggleFavorite = { onToggleFavorite(station) },
                        onDeleteStation = if (station.isCustom) {
                            { onDeleteCustom(station) }
                        } else null,
                        onViewHistory = if (onViewHistory != null) { { onViewHistory(station) } } else null
                    )
                }
            }
        }
    }
}
