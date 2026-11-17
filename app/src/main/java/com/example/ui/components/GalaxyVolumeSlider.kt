package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GalaxyCardBorder
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxySurface
import com.example.ui.theme.GalaxySurfaceVariant
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary
import com.example.ui.theme.GalaxyVoid
import kotlin.math.roundToInt

@Composable
fun GalaxyVolumeSlider(
    volume: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier,
    showCardContainer: Boolean = true,
    compact: Boolean = false,
    testTagPrefix: String = "volume"
) {
    val effectiveVolume = if (isMuted) 0f else volume.coerceIn(0f, 1f)
    val percent = (effectiveVolume * 100f).roundToInt()
    val isEffectivelyMuted = isMuted || effectiveVolume <= 0.005f

    val volumeIcon = when {
        isEffectivelyMuted -> Icons.AutoMirrored.Filled.VolumeOff
        effectiveVolume < 0.5f -> Icons.AutoMirrored.Filled.VolumeDown
        else -> Icons.AutoMirrored.Filled.VolumeUp
    }

    val sliderCd = stringResource(R.string.volume_slider_cd)
    val muteCd = if (isEffectivelyMuted) {
        stringResource(R.string.volume_unmute_cd)
    } else {
        stringResource(R.string.volume_mute_cd)
    }

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 8.dp else 12.dp,
                    vertical = if (compact) 2.dp else 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("${testTagPrefix}_mute_button")
            ) {
                Icon(
                    imageVector = volumeIcon,
                    contentDescription = muteCd,
                    tint = if (isEffectivelyMuted) GalaxyCyan else GalaxyTextPrimary,
                    modifier = Modifier.size(if (compact) 20.dp else 22.dp)
                )
            }

            Slider(
                value = effectiveVolume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = GalaxyCyan,
                    activeTrackColor = GalaxyCyan,
                    inactiveTrackColor = GalaxyCardBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = sliderCd }
                    .testTag("${testTagPrefix}_slider")
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = if (isEffectivelyMuted) "MUTE" else "$percent%",
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEffectivelyMuted) GalaxyCyan else GalaxyTextSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(44.dp)
                    .testTag("${testTagPrefix}_percent_text")
            )
        }
    }

    if (showCardContainer) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GalaxySurface,
            border = BorderStroke(
                1.dp,
                if (isEffectivelyMuted) GalaxyCyan.copy(alpha = 0.5f) else GalaxyCardBorder
            ),
            modifier = modifier.fillMaxWidth()
        ) {
            content()
        }
    } else {
        Surface(
            color = GalaxySurfaceVariant.copy(alpha = 0.7f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GalaxyCardBorder),
            modifier = modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun VolumeControlDialog(
    volume: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf(0f to "Mute", 0.25f to "25%", 0.5f to "50%", 0.75f to "75%", 1f to "100%")
    val effectivePercent = ((if (isMuted) 0f else volume.coerceIn(0f, 1f)) * 100f).roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GalaxySurface,
        titleContentColor = GalaxyTextPrimary,
        textContentColor = GalaxyTextSecondary,
        title = {
            Text(
                text = "Cosmic Volume",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = GalaxyCyan
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GalaxyVolumeSlider(
                    volume = volume,
                    isMuted = isMuted,
                    onVolumeChange = onVolumeChange,
                    onToggleMute = onToggleMute,
                    showCardContainer = false,
                    testTagPrefix = "dialog_volume"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (presetVol, label) ->
                        val targetPct = (presetVol * 100f).roundToInt()
                        val selected = effectivePercent == targetPct
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (presetVol == 0f) {
                                    if (!isMuted) onToggleMute()
                                } else {
                                    onVolumeChange(presetVol)
                                }
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GalaxyCyan,
                                selectedLabelColor = GalaxyVoid,
                                containerColor = GalaxySurfaceVariant,
                                labelColor = GalaxyTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GalaxyCyan,
                    contentColor = GalaxyVoid
                )
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}
