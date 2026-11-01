package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.EqualizerManager
import com.example.model.EqPresetType
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EqualizerScreen(
    equalizerManager: EqualizerManager,
    modifier: Modifier = Modifier
) {
    val state by equalizerManager.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Master Control Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GalaxySurface),
            border = BorderStroke(1.dp, GalaxyCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GalaxyCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Equalizer,
                                contentDescription = null,
                                tint = GalaxyCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Acoustic Equalizer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = GalaxyTextPrimary
                        )
                        Text(
                            text = if (state.isEnabled) "DSP Engine Active" else "Bypassed (Flat Output)",
                            fontSize = 12.sp,
                            color = if (state.isEnabled) GalaxyCyan else GalaxyTextMuted
                        )
                    }
                }

                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = { equalizerManager.toggleEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GalaxyVoid,
                        checkedTrackColor = GalaxyCyan,
                        uncheckedThumbColor = GalaxyTextMuted,
                        uncheckedTrackColor = GalaxySurfaceVariant
                    ),
                    modifier = Modifier.testTag("equalizer_master_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sound Presets Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cosmic Presets",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GalaxyTextSecondary
            )

            IconButton(
                onClick = { equalizerManager.applyPreset(EqPresetType.FLAT) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.RestartAlt,
                    contentDescription = "Reset Flat",
                    tint = GalaxyCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EqPresetType.values().forEach { preset ->
                val isSelected = state.activePreset == preset
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) GalaxyCyan else GalaxySurface,
                    border = BorderStroke(1.dp, if (isSelected) GalaxyCyan else GalaxyCardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { equalizerManager.applyPreset(preset) }
                        .testTag("eq_preset_${preset.name}")
                ) {
                    Text(
                        text = preset.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GalaxyVoid else GalaxyTextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 5-Band Frequency Sliders
        Text(
            text = "Frequency Calibration",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GalaxyTextSecondary
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GalaxySurface),
            border = BorderStroke(1.dp, GalaxyCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                state.bands.forEach { band ->
                    val gainDb = band.currentLevelMb / 100f
                    val gainString = if (gainDb > 0) "+%.1f dB".format(gainDb) else "%.1f dB".format(gainDb)

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = band.displayFreq,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GalaxyTextPrimary
                            )
                            Text(
                                text = gainString,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (gainDb > 0) GalaxyCyan else if (gainDb < 0) GalaxyViolet else GalaxyTextSecondary
                            )
                        }

                        Slider(
                            value = band.currentLevelMb.toFloat(),
                            onValueChange = { newLevel ->
                                equalizerManager.setBandLevel(band.bandIndex, newLevel.toInt())
                            },
                            valueRange = band.minLevelMb.toFloat()..band.maxLevelMb.toFloat(),
                            enabled = state.isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = GalaxyCyan,
                                activeTrackColor = GalaxyCyan,
                                inactiveTrackColor = GalaxyCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("eq_slider_band_${band.bandIndex}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bass Boost & Soundstage Enhancement
        Text(
            text = "Cosmic Audio FX",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GalaxyTextSecondary
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GalaxySurface),
            border = BorderStroke(1.dp, GalaxyCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bass Boost
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = null,
                                tint = GalaxyCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sub-Bass Harmonic Boost",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = GalaxyTextPrimary
                            )
                        }
                        Text(
                            text = "${state.bassBoostStrength / 10}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GalaxyCyan
                        )
                    }

                    Slider(
                        value = state.bassBoostStrength.toFloat(),
                        onValueChange = { equalizerManager.setBassBoost(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = state.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = GalaxyCyan,
                            activeTrackColor = GalaxyCyan,
                            inactiveTrackColor = GalaxyCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slider_bass_boost")
                    )
                }

                // 3D Virtualizer / Spatial
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SurroundSound,
                                contentDescription = null,
                                tint = GalaxyElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "3D Galactic Virtualizer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = GalaxyTextPrimary
                            )
                        }
                        Text(
                            text = "${state.virtualizerStrength / 10}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GalaxyElectricBlue
                        )
                    }

                    Slider(
                        value = state.virtualizerStrength.toFloat(),
                        onValueChange = { equalizerManager.setVirtualizer(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = state.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = GalaxyElectricBlue,
                            activeTrackColor = GalaxyElectricBlue,
                            inactiveTrackColor = GalaxyCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slider_virtualizer")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }
}
