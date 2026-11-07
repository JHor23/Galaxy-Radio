package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.EqualizerManager
import com.example.data.repository.RadioStationRepository
import com.example.data.repository.SongHistoryRepository
import com.example.model.RadioStation
import com.example.player.PlayerUiState
import com.example.player.RadioPlayerManager
import com.example.ui.components.AddStationBottomSheet
import com.example.ui.components.FullscreenPlayerModal
import com.example.ui.components.GalaxyStarfield
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.StationHistoryModal
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.QualityDataScreen
import com.example.ui.screens.SavedStationsScreen
import com.example.ui.theme.GalaxyCardBorder
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyDeep
import com.example.ui.theme.GalaxyElectricBlue
import com.example.ui.theme.GalaxySurface
import com.example.ui.theme.GalaxyTextMuted
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary
import com.example.ui.theme.GalaxyVoid
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var equalizerManager: EqualizerManager
    private lateinit var repository: RadioStationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RadioPlayerManager.init(this)

        val app = application as GalaxyRadioApplication
        equalizerManager = EqualizerManager(this)
        repository = RadioStationRepository(app.database.stationDao())

        setContent {
            MyApplicationTheme {
                RadioAppMain(
                    repository = repository,
                    equalizerManager = equalizerManager
                )
            }
        }
    }

    override fun onDestroy() {
        equalizerManager.releaseAudioFx()
        super.onDestroy()
    }
}

private data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun RadioAppMain(
    repository: RadioStationRepository,
    equalizerManager: EqualizerManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playerState by RadioPlayerManager.uiState.collectAsState()
    val songHistoryRepository = remember { SongHistoryRepository.getInstance(context) }

    var currentTab by remember { mutableIntStateOf(0) }
    var isPlayerModalOpen by remember { mutableStateOf(false) }
    var isAddStationSheetOpen by remember { mutableStateOf(false) }
    var isSleepTimerDialogOpen by remember { mutableStateOf(false) }
    var isHistoryModalOpen by remember { mutableStateOf(false) }
    var historyStation by remember { mutableStateOf<RadioStation?>(null) }

    // Request notification permission for Android 13+ background playback controls
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Attach equalizer whenever audio session ID changes
    LaunchedEffect(playerState.audioSessionId) {
        if (playerState.audioSessionId > 0) {
            equalizerManager.attachAudioSession(playerState.audioSessionId)
        }
    }

    val navItems = listOf(
        NavItem("Explore", Icons.Filled.Radio, Icons.Outlined.Radio, "nav_explore"),
        NavItem("Equalizer", Icons.Filled.Equalizer, Icons.Outlined.Equalizer, "nav_equalizer"),
        NavItem("Saved", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "nav_saved"),
        NavItem("Quality", Icons.Filled.Speed, Icons.Outlined.Speed, "nav_quality")
    )

    // BackHandler to close player modal, sheet, or dialog before exiting
    BackHandler(enabled = isHistoryModalOpen || isSleepTimerDialogOpen || isPlayerModalOpen || isAddStationSheetOpen || currentTab != 0) {
        when {
            isHistoryModalOpen -> isHistoryModalOpen = false
            isSleepTimerDialogOpen -> isSleepTimerDialogOpen = false
            isPlayerModalOpen -> isPlayerModalOpen = false
            isAddStationSheetOpen -> isAddStationSheetOpen = false
            currentTab != 0 -> currentTab = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GalaxyVoid)
    ) {
        GalaxyStarfield(modifier = Modifier.fillMaxSize(), starCount = 50)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GalaxyTopBar(
                    playerState = playerState,
                    onOpenAddStation = { isAddStationSheetOpen = true },
                    onOpenPlayer = { if (playerState.currentStation != null) isPlayerModalOpen = true },
                    onOpenSleepTimer = { isSleepTimerDialogOpen = true },
                    onOpenHistory = {
                        historyStation = playerState.currentStation
                        isHistoryModalOpen = true
                    }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    // Persistent Mini Player above Nav Bar if a station has been selected
                    if (playerState.currentStation != null && !isPlayerModalOpen) {
                        MiniPlayerBar(
                            playerState = playerState,
                            onExpand = { isPlayerModalOpen = true },
                            onTogglePlayPause = { RadioPlayerManager.togglePlayPause(context) },
                            onStop = { RadioPlayerManager.stop(context) }
                        )
                    }

                    // Navigation Bar
                    NavigationBar(
                        containerColor = GalaxyDeep,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_navigation_bar")
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = currentTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = index },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GalaxyVoid,
                                    selectedTextColor = GalaxyCyan,
                                    indicatorColor = GalaxyCyan,
                                    unselectedIconColor = GalaxyTextSecondary,
                                    unselectedTextColor = GalaxyTextSecondary
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> ExploreScreen(
                        repository = repository,
                        playerState = playerState,
                        onSelectStation = { station ->
                            RadioPlayerManager.playStation(context, station)
                        },
                        onTogglePlayPause = { RadioPlayerManager.togglePlayPause(context) },
                        onToggleFavorite = { station ->
                            scope.launch {
                                repository.toggleFavorite(station, !station.isFavorite)
                            }
                        },
                        onDeleteStation = { station ->
                            scope.launch {
                                if (station.isCustom) {
                                    repository.deleteCustomStation(station)
                                } else {
                                    repository.deleteCuratedStation(station.id)
                                }
                            }
                        },
                        onResetCurated = {
                            scope.launch {
                                repository.resetAllCuratedStations()
                            }
                        },
                        onOpenAddStation = { isAddStationSheetOpen = true },
                        onNavigateToSaved = { currentTab = 2 },
                        onViewHistory = { station ->
                            historyStation = station
                            isHistoryModalOpen = true
                        }
                    )
                    1 -> EqualizerScreen(
                        equalizerManager = equalizerManager
                    )
                    2 -> SavedStationsScreen(
                        repository = repository,
                        playerState = playerState,
                        onSelectStation = { station ->
                            RadioPlayerManager.playStation(context, station)
                        },
                        onTogglePlayPause = { RadioPlayerManager.togglePlayPause(context) },
                        onToggleFavorite = { station ->
                            scope.launch {
                                repository.toggleFavorite(station, !station.isFavorite)
                            }
                        },
                        onDeleteCustom = { station ->
                            scope.launch {
                                repository.deleteCustomStation(station)
                            }
                        },
                        onResetCurated = {
                            scope.launch {
                                repository.resetAllCuratedStations()
                            }
                        },
                        onOpenAddStation = { isAddStationSheetOpen = true },
                        onNavigateToExplore = { currentTab = 0 },
                        onViewHistory = { station ->
                            historyStation = station
                            isHistoryModalOpen = true
                        }
                    )
                    3 -> QualityDataScreen(
                        playerState = playerState,
                        onSelectQuality = { preset ->
                            RadioPlayerManager.setQualityPreset(preset)
                        }
                    )
                }
            }
        }

        // Add Custom Station Bottom Sheet
        if (isAddStationSheetOpen) {
            AddStationBottomSheet(
                repository = repository,
                onDismiss = { isAddStationSheetOpen = false },
                onStationAdded = { name, url, genre, bitrate ->
                    scope.launch {
                        val newStation = repository.addCustomStation(name, url, genre, bitrate)
                        RadioPlayerManager.playStation(context, newStation)
                    }
                }
            )
        }

        // Fullscreen Galactic Player Modal
        AnimatedVisibility(
            visible = isPlayerModalOpen && playerState.currentStation != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullscreenPlayerModal(
                playerState = playerState,
                onDismiss = { isPlayerModalOpen = false },
                onTogglePlayPause = { RadioPlayerManager.togglePlayPause(context) },
                onStop = {
                    RadioPlayerManager.stop(context)
                    isPlayerModalOpen = false
                },
                onToggleFavorite = {
                    playerState.currentStation?.let { station ->
                        scope.launch {
                            repository.toggleFavorite(station, !station.isFavorite)
                        }
                    }
                },
                onNavigateToEqualizer = {
                    isPlayerModalOpen = false
                    currentTab = 1
                },
                onNavigateToQuality = {
                    isPlayerModalOpen = false
                    currentTab = 3
                },
                onOpenSleepTimer = {
                    isSleepTimerDialogOpen = true
                },
                onOpenHistory = {
                    historyStation = playerState.currentStation
                    isHistoryModalOpen = true
                }
            )
        }

        // Sleep Timer Dialog
        if (isSleepTimerDialogOpen) {
            SleepTimerDialog(
                onDismiss = { isSleepTimerDialogOpen = false }
            )
        }

        // Station Track History Modal
        if (isHistoryModalOpen) {
            StationHistoryModal(
                station = historyStation ?: playerState.currentStation,
                playerState = playerState,
                songHistoryRepository = songHistoryRepository,
                onDismiss = { isHistoryModalOpen = false }
            )
        }
    }
}

@Composable
fun GalaxyTopBar(
    playerState: PlayerUiState,
    onOpenAddStation: () -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Surface(
        color = GalaxyDeep.copy(alpha = 0.95f),
        border = BorderStroke(0.dp, Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = GalaxyCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GalaxyCyan),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Radio,
                            contentDescription = null,
                            tint = GalaxyCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GALAXY",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = GalaxyTextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RADIO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = GalaxyCyan
                        )
                    }
                    Text(
                        text = "Cosmic Audio Streaming",
                        fontSize = 10.sp,
                        color = GalaxyTextMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Top Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                var menuExpanded by remember { mutableStateOf(false) }
                val sleepSec = playerState.sleepTimerRemainingSeconds

                // Active sleep timer countdown chip
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
                            .padding(end = 8.dp)
                            .testTag("top_bar_sleep_timer_active")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = null,
                                tint = GalaxyCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = countdownStr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GalaxyCyan
                            )
                        }
                    }
                }

                // "ON AIR" button stays in its location
                if (playerState.isPlaying) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GalaxyCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenPlayer)
                            .padding(end = 8.dp)
                            .testTag("top_bar_now_playing")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GalaxyCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ON AIR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GalaxyCyan
                            )
                        }
                    }
                }

                // Tiered Menu (3 dots) for Sleep Timer, History, and Add Custom Stream
                Box {
                    Surface(
                        shape = CircleShape,
                        color = GalaxySurface,
                        border = BorderStroke(1.dp, GalaxyCardBorder),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(onClick = { menuExpanded = true })
                            .testTag("top_bar_menu_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More actions",
                                tint = GalaxyCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(GalaxySurface)
                            .border(1.dp, GalaxyCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        val sleepCountdownText = if (sleepSec != null && sleepSec > 0) {
                            val m = sleepSec / 60
                            val s = sleepSec % 60
                            String.format("Sleep Timer (%02d:%02d)", m, s)
                        } else {
                            "Sleep Timer"
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = sleepCountdownText,
                                    color = GalaxyTextPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Bedtime,
                                    contentDescription = null,
                                    tint = if (sleepSec != null && sleepSec > 0) GalaxyCyan else GalaxyTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onOpenSleepTimer()
                            },
                            modifier = Modifier.testTag("top_bar_sleep_timer")
                        )

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
                                onOpenHistory()
                            },
                            modifier = Modifier.testTag("top_bar_history")
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Add Custom Stream",
                                    color = GalaxyTextPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = GalaxyCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onOpenAddStation()
                            },
                            modifier = Modifier.testTag("top_bar_add_station")
                        )
                    }
                }
            }
        }
    }
}
