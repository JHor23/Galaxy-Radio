package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.RadioStationRepository
import com.example.model.RadioStation
import com.example.player.PlayerUiState
import com.example.ui.components.StationCard
import com.example.ui.theme.GalaxyCardBorder
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxySurface
import com.example.ui.theme.GalaxySurfaceVariant
import com.example.ui.theme.GalaxyTextMuted
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary
import com.example.ui.theme.GalaxyVoid
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    repository: RadioStationRepository,
    playerState: PlayerUiState,
    onSelectStation: (RadioStation) -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onDeleteStation: ((RadioStation) -> Unit)? = null,
    onResetCurated: (() -> Unit)? = null,
    onOpenAddStation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("All") }

    var stations by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val filterTags = listOf("All", "Ambient", "Space", "Synthwave", "Lofi", "Electronic", "Jazz", "Rock")

    fun loadStations(tag: String) {
        isLoading = true
        scope.launch {
            stations = if (tag == "All") {
                repository.fetchPopularStations(null)
            } else {
                repository.fetchPopularStations(tag)
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedTag) {
        if (searchQuery.isBlank()) {
            loadStations(selectedTag)
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isLoading = true
            scope.launch {
                stations = repository.searchStations(searchQuery)
                isLoading = false
            }
        } else {
            loadStations(selectedTag)
        }
    }

    LaunchedEffect(stations) {
        if (stations.isNotEmpty()) {
            com.example.player.RadioPlayerManager.setStationQueue(stations)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search cosmic streams, genres, stations...", color = GalaxyTextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = GalaxyCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear search",
                                tint = GalaxyTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
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
                    .weight(1f)
                    .testTag("search_station_input")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GalaxySurfaceVariant,
                border = BorderStroke(1.dp, GalaxyCardBorder),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { loadStations(selectedTag) }
                    .testTag("refresh_stations_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = GalaxyCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Tag Filters
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterTags) { tag ->
                val isSelected = selectedTag == tag && searchQuery.isBlank()
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) GalaxyCyan else GalaxySurface,
                    border = BorderStroke(1.dp, if (isSelected) GalaxyCyan else GalaxyCardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            searchQuery = ""
                            selectedTag = tag
                        }
                        .testTag("tag_filter_$tag")
                ) {
                    Text(
                        text = tag,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GalaxyVoid else GalaxyTextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        val hiddenCuratedIds by repository.hiddenCuratedIds.collectAsState(initial = emptyList())
        val hasDeletedCurated = hiddenCuratedIds.isNotEmpty()

        // Stations Count & Quick Add Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (searchQuery.isNotBlank()) "Search Results (${stations.size})" else "Cosmic Stations (${stations.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GalaxyTextSecondary
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
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onResetCurated()
                                loadStations(selectedTag)
                            }
                            .testTag("reset_curated_stations_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = "Restore Curated Stations",
                                tint = GalaxyCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Restore (${hiddenCuratedIds.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GalaxyCyan
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpenAddStation)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("quick_add_station_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = GalaxyCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Custom",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GalaxyCyan
                    )
                }
            }
        }

        // Station List
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = GalaxyCyan, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Tuning into cosmic signals...",
                        fontSize = 13.sp,
                        color = GalaxyTextSecondary
                    )
                }
            } else if (stations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No Cosmic Stations Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GalaxyTextPrimary
                    )
                    Text(
                        text = "Try adjusting your search terms or add a custom station URL directly.",
                        fontSize = 13.sp,
                        color = GalaxyTextMuted,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(stations, key = { it.id }) { station ->
                        val isCurrent = playerState.currentStation?.id == station.id
                        StationCard(
                            station = station,
                            isCurrent = isCurrent,
                            isPlaying = isCurrent && playerState.isPlaying,
                            isBuffering = isCurrent && playerState.isBuffering,
                            onSelect = { onSelectStation(station) },
                            onTogglePlayPause = onTogglePlayPause,
                            onToggleFavorite = { onToggleFavorite(station) },
                            onDeleteStation = if (onDeleteStation != null) {
                                {
                                    onDeleteStation(station)
                                    // Remove immediately from current display list
                                    stations = stations.filter { it.id != station.id }
                                }
                            } else null
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
