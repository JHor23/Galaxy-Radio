package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.RadioStationRepository
import com.example.ui.theme.GalaxyCardBorder
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyDeep
import com.example.ui.theme.GalaxyError
import com.example.ui.theme.GalaxySuccess
import com.example.ui.theme.GalaxySurface
import com.example.ui.theme.GalaxySurfaceVariant
import com.example.ui.theme.GalaxyTextMuted
import com.example.ui.theme.GalaxyTextPrimary
import com.example.ui.theme.GalaxyTextSecondary
import com.example.ui.theme.GalaxyVoid
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddStationBottomSheet(
    repository: RadioStationRepository,
    onDismiss: () -> Unit,
    onStationAdded: (name: String, url: String, genre: String, bitrate: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var streamUrl by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("Ambient Space") }
    var selectedBitrate by remember { mutableIntStateOf(128) }

    var isTesting by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf<Boolean?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val presetGenres = listOf(
        "Ambient Space", "Synthwave", "Lo-Fi", "Electronic",
        "Deep Chill", "Jazz / Funk", "Classical", "Rock", "Custom"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GalaxyDeep,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GalaxyCardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Radio,
                        contentDescription = null,
                        tint = GalaxyCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Add Custom Station",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = GalaxyTextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = GalaxyTextSecondary
                    )
                }
            }

            Text(
                text = "Enter any direct MP3, AAC, or Icecast/Shoutcast streaming link",
                fontSize = 13.sp,
                color = GalaxyTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Stream URL input
            OutlinedTextField(
                value = streamUrl,
                onValueChange = {
                    streamUrl = it
                    testResultText = null
                    isTestSuccess = null
                    validationError = null
                },
                label = { Text("Direct Streaming URL") },
                placeholder = { Text("https://example.com/stream.mp3") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = null,
                        tint = GalaxyCyan
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GalaxyCyan,
                    unfocusedBorderColor = GalaxyCardBorder,
                    focusedTextColor = GalaxyTextPrimary,
                    unfocusedTextColor = GalaxyTextPrimary,
                    cursorColor = GalaxyCyan,
                    focusedContainerColor = GalaxySurface,
                    unfocusedContainerColor = GalaxySurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stream_url_input")
            )

            // Test Stream Connection Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (streamUrl.isBlank()) {
                            validationError = "Please enter a stream URL first"
                            return@OutlinedButton
                        }
                        isTesting = true
                        testResultText = null
                        isTestSuccess = null
                        scope.launch {
                            val result = repository.testStreamConnection(streamUrl)
                            isTesting = false
                            result.onSuccess { data ->
                                isTestSuccess = true
                                testResultText = "Connected! Station: ${data.stationName} (${data.bitrate} kbps)"
                                if (name.isBlank() && data.stationName.isNotBlank() && data.stationName != "Online Radio") {
                                    name = data.stationName
                                }
                            }.onFailure { err ->
                                isTestSuccess = false
                                testResultText = "Test failed: ${err.message ?: "Could not reach stream"}"
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GalaxyCyan),
                    modifier = Modifier.testTag("test_connection_button")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = GalaxyCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Filled.NetworkCheck,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Test Connection", fontSize = 13.sp)
                }

                // Example URL autofill helper
                Text(
                    text = "Try sample stream",
                    fontSize = 12.sp,
                    color = GalaxyCyan,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            streamUrl = "https://ice1.somafm.com/spacestation-128-mp3"
                            name = "Space Station Soma"
                            selectedGenre = "Ambient Space"
                        }
                        .padding(4.dp)
                )
            }

            // Connection Test Feedback Badge
            AnimatedVisibility(visible = testResultText != null) {
                val isSuccess = isTestSuccess == true
                Surface(
                    color = if (isSuccess) GalaxySuccess.copy(alpha = 0.12f) else GalaxyError.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        if (isSuccess) GalaxySuccess.copy(alpha = 0.4f) else GalaxyError.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = if (isSuccess) GalaxySuccess else GalaxyError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = testResultText.orEmpty(),
                            fontSize = 12.sp,
                            color = if (isSuccess) GalaxyTextPrimary else GalaxyError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Station Name input
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    validationError = null
                },
                label = { Text("Station Name") },
                placeholder = { Text("e.g. Andromeda Chillout") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GalaxyCyan,
                    unfocusedBorderColor = GalaxyCardBorder,
                    focusedTextColor = GalaxyTextPrimary,
                    unfocusedTextColor = GalaxyTextPrimary,
                    cursorColor = GalaxyCyan,
                    focusedContainerColor = GalaxySurface,
                    unfocusedContainerColor = GalaxySurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("station_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Genre Tag Chips
            Text(
                text = "Select Genre / Tag",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GalaxyTextSecondary
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetGenres.forEach { genre ->
                    val isSelected = selectedGenre == genre
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) GalaxyCyan else GalaxySurfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) GalaxyCyan else GalaxyCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedGenre = genre }
                    ) {
                        Text(
                            text = genre,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GalaxyVoid else GalaxyTextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bitrate selector
            Text(
                text = "Stream Bitrate Preset",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GalaxyTextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(64, 128, 192, 320).forEach { br ->
                    val isSelected = selectedBitrate == br
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) GalaxyCyan.copy(alpha = 0.2f) else GalaxySurface,
                        border = BorderStroke(1.dp, if (isSelected) GalaxyCyan else GalaxyCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedBitrate = br }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$br",
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GalaxyCyan else GalaxyTextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "kbps",
                                color = GalaxyTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Validation Error
            if (validationError != null) {
                Text(
                    text = validationError.orEmpty(),
                    color = GalaxyError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (streamUrl.isBlank()) {
                        validationError = "Stream URL is required"
                        return@Button
                    }
                    if (!streamUrl.startsWith("http://") && !streamUrl.startsWith("https://")) {
                        validationError = "URL must start with http:// or https://"
                        return@Button
                    }
                    val finalName = name.ifBlank { "Custom Station" }
                    onStationAdded(finalName, streamUrl, selectedGenre, selectedBitrate)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GalaxyCyan,
                    contentColor = GalaxyVoid
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_station_button")
            ) {
                Text(
                    text = "Save Custom Station",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
