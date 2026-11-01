package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sleep.SleepTimerManager
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleepTimerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val timerState by SleepTimerManager.timerState.collectAsState()

    var customMinutes by remember { mutableIntStateOf(timerState.selectedMinutes.coerceIn(5, 120)) }
    val presetOptions = listOf(15, 30, 45, 60, 90, 120)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GalaxyDeep),
            border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("sleep_timer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GalaxyViolet.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, GalaxyViolet),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Bedtime,
                                    contentDescription = null,
                                    tint = GalaxyCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cosmic Sleep Timer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = GalaxyTextPrimary
                            )
                            Text(
                                text = "Fade out stream automatically",
                                fontSize = 11.sp,
                                color = GalaxyTextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = GalaxyTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Active Countdown Display
                if (timerState.isActive) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GalaxySurfaceVariant,
                        border = BorderStroke(1.dp, GalaxyCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TIMER RUNNING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = GalaxyCyan
                            )
                            Text(
                                text = timerState.formattedRemaining,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = GalaxyTextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = "Playback will stop smoothly",
                                fontSize = 11.sp,
                                color = GalaxyTextSecondary
                            )

                            // Quick extend buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OutlinedButton(
                                    onClick = { SleepTimerManager.addMinutes(context, 5) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GalaxyCyan),
                                    modifier = Modifier.testTag("sleep_timer_add_5m")
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("+5m", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { SleepTimerManager.addMinutes(context, 15) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GalaxyCyan),
                                    modifier = Modifier.testTag("sleep_timer_add_15m")
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("+15m", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { SleepTimerManager.cancelTimer() },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, GalaxyCardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GalaxyTextSecondary),
                                    modifier = Modifier.testTag("sleep_timer_cancel")
                                ) {
                                    Icon(imageVector = Icons.Filled.TimerOff, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Turn Off", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Preset Chips
                Text(
                    text = "Select Duration",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GalaxyTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetOptions.forEach { minutes ->
                        val isSelected = customMinutes == minutes
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GalaxyCyan else GalaxySurface,
                            border = BorderStroke(1.dp, if (isSelected) GalaxyCyan else GalaxyCardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { customMinutes = minutes }
                                .testTag("sleep_preset_${minutes}m")
                        ) {
                            Text(
                                text = "${minutes}m",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) GalaxyVoid else GalaxyTextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Minutes Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom Duration",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = GalaxyTextSecondary
                    )
                    Text(
                        text = "${customMinutes} minutes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GalaxyCyan
                    )
                }

                Slider(
                    value = customMinutes.toFloat(),
                    onValueChange = { customMinutes = it.toInt() },
                    valueRange = 5f..120f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = GalaxyCyan,
                        activeTrackColor = GalaxyCyan,
                        inactiveTrackColor = GalaxyCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sleep_timer_slider")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Button(
                    onClick = {
                        SleepTimerManager.startTimer(context, customMinutes)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GalaxyCyan,
                        contentColor = GalaxyVoid
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_sleep_timer_button")
                ) {
                    Text(
                        text = if (timerState.isActive) "Update Timer (${customMinutes}m)" else "Start Sleep Timer (${customMinutes}m)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
