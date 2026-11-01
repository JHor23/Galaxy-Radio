package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioQualityPreset
import com.example.player.PlayerUiState
import com.example.ui.theme.GalaxyCardBorder
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyElectricBlue
import com.example.ui.theme.GalaxySurface
import com.example.ui.theme.GalaxySurfaceVariant
import com.example.ui.theme.GalaxyTextMuted
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary
import com.example.ui.theme.GalaxyViolet
import com.example.ui.theme.GalaxyVoid

@Composable
fun QualityDataScreen(
    playerState: PlayerUiState,
    onSelectQuality: (AudioQualityPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Live Session Telemetry Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GalaxySurface),
            border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GalaxyCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = null,
                                    tint = GalaxyCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Live Stream Data Meter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GalaxyTextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (playerState.isPlaying) GalaxyCyan.copy(alpha = 0.2f) else GalaxySurfaceVariant
                    ) {
                        Text(
                            text = if (playerState.isPlaying) "ONLINE" else "STANDBY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playerState.isPlaying) GalaxyCyan else GalaxyTextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricBox(
                        title = "DATA CONSUMED",
                        value = playerState.formattedDataUsage,
                        highlight = true
                    )
                    MetricBox(
                        title = "SESSION TIME",
                        value = playerState.formattedDuration,
                        highlight = false
                    )
                    MetricBox(
                        title = "ACTIVE BITRATE",
                        value = "${playerState.audioQuality.targetBitrateKbps}k",
                        highlight = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Preset Section Header
        Text(
            text = "Selectable Audio Quality Presets",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GalaxyTextSecondary
        )
        Text(
            text = "Fine-tune bandwidth consumption for cellular roaming or high fidelity Wi-Fi",
            fontSize = 12.sp,
            color = GalaxyTextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Preset Cards
        AudioQualityPreset.values().forEach { preset ->
            val isSelected = playerState.audioQuality == preset
            QualityPresetCard(
                preset = preset,
                isSelected = isSelected,
                onSelect = { onSelectQuality(preset) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Data Optimization Tips
        Text(
            text = "Cosmic Bandwidth Optimization",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GalaxyTextSecondary
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = GalaxySurface),
            border = BorderStroke(1.dp, GalaxyCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = null,
                        tint = GalaxyCyan,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ExoPlayer dynamic jitter buffers adapt seamlessly to variable 4G/5G and Wi-Fi networks.",
                        fontSize = 12.sp,
                        color = GalaxyTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Filled.DataSaverOn,
                        contentDescription = null,
                        tint = GalaxyElectricBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Switching to Data Saver mode limits background packet bursts, conserving up to 75% mobile data.",
                        fontSize = 12.sp,
                        color = GalaxyTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    highlight: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = GalaxyTextMuted,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) GalaxyCyan else GalaxyTextPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun QualityPresetCard(
    preset: AudioQualityPreset,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) GalaxySurfaceVariant else GalaxySurface,
        border = BorderStroke(
            1.dp,
            if (isSelected) GalaxyCyan else GalaxyCardBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .testTag("quality_preset_${preset.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) GalaxyCyan else GalaxyCardBorder,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = GalaxyVoid,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = preset.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) GalaxyCyan else GalaxyTextPrimary
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GalaxySurface
                    ) {
                        Text(
                            text = preset.dataRateEstimate,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = GalaxyElectricBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = preset.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = GalaxyTextSecondary),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
