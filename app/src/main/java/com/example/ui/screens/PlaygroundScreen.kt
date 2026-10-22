package com.example.ui.screens

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HandsFreeButton
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TechSurface
import com.example.ui.theme.TechSurfaceBorder
import com.example.ui.theme.TechSurfaceVariant
import com.example.viewmodel.FaceControlViewModel
import kotlin.random.Random

@Composable
fun PlaygroundScreen(
    viewModel: FaceControlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isHi = settings.isHindi

    // Media player state
    var isPlaying by remember { mutableStateOf(false) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var volumeLevel by remember { mutableIntStateOf(7) }
    val playlist = listOf("Cyber Drift 2026", "Acoustic Horizon", "Relaxing Binaural", "Morning Sunshine")

    // Flashlight state
    var isTorchOn by remember { mutableStateOf(false) }
    fun toggleTorch() {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId != null) {
                isTorchOn = !isTorchOn
                cameraManager.setTorchMode(cameraId, isTorchOn)
                viewModel.showActionNotification(
                    if (isTorchOn) (if (isHi) "टॉर्च चालू" else "Torch ON") else (if (isHi) "टॉर्च बंद" else "Torch OFF")
                )
            }
        } catch (e: Exception) {
            isTorchOn = !isTorchOn
            viewModel.showActionNotification(if (isHi) "टॉर्च टॉगल हुआ" else "Torch Toggled")
        }
    }

    // Hands-free Dial Pad state
    var dialNumber by remember { mutableStateOf("") }

    // Target Precision Practice Game state
    var targetScore by remember { mutableIntStateOf(0) }
    var targetPositionIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Intro Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurfaceVariant),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyberCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = "Hands-free controls",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isHi) "स्पर्शहीन नियंत्रण डेमो (Playground)" else "Hands-Free Playground",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = if (isHi) "सिर घुमाकर कर्सर लाएँ और होल्ड (Dwell) करें" else "Hover cursor over any card to trigger action",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Hands-Free Media Controller
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHi) "म्यूजिक प्लेयर" else "Media Player",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (isPlaying) (if (isHi) "चल रहा है ▶" else "PLAYING ▶") else (if (isHi) "रुका हुआ ⏸" else "PAUSED ⏸"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (isPlaying) EmeraldGreen else SolarAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Track info display
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = TechSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = playlist[currentTrackIndex % playlist.size],
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                                Text(
                                    text = if (isHi) "वॉल्यूम: $volumeLevel/10" else "Volume: $volumeLevel/10",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Play / Pause / Prev / Next buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HandsFreeButton(
                            id = "media_prev",
                            label = if (isHi) "पिछला" else "Prev",
                            icon = Icons.Default.FastRewind,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            accentColor = CyberCyan,
                            onClick = {
                                currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else playlist.size - 1
                                viewModel.showActionNotification(
                                    if (isHi) "पिछला गाना: ${playlist[currentTrackIndex]}" else "Previous track: ${playlist[currentTrackIndex]}"
                                )
                            }
                        )

                        HandsFreeButton(
                            id = "media_play_pause",
                            label = if (isPlaying) (if (isHi) "रोकें" else "Pause") else (if (isHi) "चलाएँ" else "Play"),
                            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(72.dp),
                            accentColor = if (isPlaying) SolarAmber else EmeraldGreen,
                            onClick = {
                                isPlaying = !isPlaying
                                viewModel.showActionNotification(
                                    if (isPlaying) (if (isHi) "गाना शुरू हुआ" else "Playback Started") else (if (isHi) "गाना रुका" else "Playback Paused")
                                )
                            }
                        )

                        HandsFreeButton(
                            id = "media_next",
                            label = if (isHi) "अगला" else "Next",
                            icon = Icons.Default.FastForward,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            accentColor = CyberCyan,
                            onClick = {
                                currentTrackIndex = (currentTrackIndex + 1) % playlist.size
                                viewModel.showActionNotification(
                                    if (isHi) "अगला गाना: ${playlist[currentTrackIndex]}" else "Next track: ${playlist[currentTrackIndex]}"
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Volume controls row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HandsFreeButton(
                            id = "media_vol_down",
                            label = if (isHi) "वॉल्यूम -" else "Vol -",
                            icon = Icons.Default.VolumeDown,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            accentColor = SolarAmber,
                            onClick = {
                                volumeLevel = (volumeLevel - 1).coerceAtLeast(0)
                                viewModel.showActionNotification("Volume: $volumeLevel/10")
                            }
                        )

                        HandsFreeButton(
                            id = "media_vol_up",
                            label = if (isHi) "वॉल्यूम +" else "Vol +",
                            icon = Icons.Default.VolumeUp,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            accentColor = EmeraldGreen,
                            onClick = {
                                volumeLevel = (volumeLevel + 1).coerceAtMost(10)
                                viewModel.showActionNotification("Volume: $volumeLevel/10")
                            }
                        )

                        HandsFreeButton(
                            id = "media_vol_mute",
                            label = if (isHi) "म्यूट" else "Mute",
                            icon = Icons.Default.VolumeMute,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            accentColor = Color.LightGray,
                            onClick = {
                                volumeLevel = 0
                                viewModel.showActionNotification(if (isHi) "म्यूट किया गया" else "Muted")
                            }
                        )
                    }
                }
            }
        }

        // Section 2: Quick Assistant Tools
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHi) "त्वरित टूल्स (Quick Tools)" else "Quick Assistance Tools",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Torch Toggle
                        HandsFreeButton(
                            id = "tool_torch",
                            label = if (isTorchOn) (if (isHi) "टॉर्च बंद" else "Torch Off") else (if (isHi) "टॉर्च चालू" else "Torch On"),
                            icon = if (isTorchOn) Icons.Default.FlashlightOff else Icons.Default.FlashlightOn,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp),
                            accentColor = if (isTorchOn) SolarAmber else CyberCyan,
                            onClick = { toggleTorch() }
                        )

                        // Voice Announcement
                        HandsFreeButton(
                            id = "tool_speak",
                            label = if (isHi) "बोलकर बताएँ" else "Voice Say",
                            icon = Icons.Default.RecordVoiceOver,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp),
                            accentColor = ElectricViolet,
                            onClick = {
                                val msg = if (isHi) "नमस्ते! फेस कंट्रोल पूरी तरह सक्रिय है।" else "Hello! Face Control is fully active."
                                viewModel.feedbackUtil.speak(msg)
                                viewModel.showActionNotification(msg)
                            }
                        )
                    }
                }
            }
        }

        // Section 3: Target Practice Precision Game
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHi) "कर्सर अभ्यास खेल (Target Practice)" else "Cursor Precision Practice",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberCyan.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, CyberCyan)
                        ) {
                            Text(
                                text = if (isHi) "अंक: $targetScore" else "Score: $targetScore",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = CyberCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isHi) "सक्रिय टारगेट बॉक्स पर कर्सर ले जाकर ड्वेल करें:" else "Move your face cursor to the highlighted target:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2x2 grid of interactive targets
                    val targetLabels = listOf("Top Left", "Top Right", "Bottom Left", "Bottom Right")
                    val targetLabelsHi = listOf("ऊपर बायाँ", "ऊपर दायाँ", "नीचे बायाँ", "नीचे दायाँ")

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HandsFreeButton(
                            id = "game_target_0",
                            label = if (isHi) targetLabelsHi[0] else targetLabels[0],
                            subLabel = if (targetPositionIndex == 0) "★ TARGET" else null,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            accentColor = if (targetPositionIndex == 0) SolarAmber else TechSurfaceBorder,
                            onClick = {
                                if (targetPositionIndex == 0) {
                                    targetScore += 10
                                    targetPositionIndex = (targetPositionIndex + 1 + Random.nextInt(3)) % 4
                                    viewModel.showActionNotification(if (isHi) "निशाना सही लगा! +10" else "Hit Target! +10")
                                }
                            }
                        )

                        HandsFreeButton(
                            id = "game_target_1",
                            label = if (isHi) targetLabelsHi[1] else targetLabels[1],
                            subLabel = if (targetPositionIndex == 1) "★ TARGET" else null,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            accentColor = if (targetPositionIndex == 1) SolarAmber else TechSurfaceBorder,
                            onClick = {
                                if (targetPositionIndex == 1) {
                                    targetScore += 10
                                    targetPositionIndex = (targetPositionIndex + 1 + Random.nextInt(3)) % 4
                                    viewModel.showActionNotification(if (isHi) "निशाना सही लगा! +10" else "Hit Target! +10")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HandsFreeButton(
                            id = "game_target_2",
                            label = if (isHi) targetLabelsHi[2] else targetLabels[2],
                            subLabel = if (targetPositionIndex == 2) "★ TARGET" else null,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            accentColor = if (targetPositionIndex == 2) SolarAmber else TechSurfaceBorder,
                            onClick = {
                                if (targetPositionIndex == 2) {
                                    targetScore += 10
                                    targetPositionIndex = (targetPositionIndex + 1 + Random.nextInt(3)) % 4
                                    viewModel.showActionNotification(if (isHi) "निशाना सही लगा! +10" else "Hit Target! +10")
                                }
                            }
                        )

                        HandsFreeButton(
                            id = "game_target_3",
                            label = if (isHi) targetLabelsHi[3] else targetLabels[3],
                            subLabel = if (targetPositionIndex == 3) "★ TARGET" else null,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            accentColor = if (targetPositionIndex == 3) SolarAmber else TechSurfaceBorder,
                            onClick = {
                                if (targetPositionIndex == 3) {
                                    targetScore += 10
                                    targetPositionIndex = (targetPositionIndex + 1 + Random.nextInt(3)) % 4
                                    viewModel.showActionNotification(if (isHi) "निशाना सही लगा! +10" else "Hit Target! +10")
                                }
                            }
                        )
                    }
                }
            }
        }

        // Section 4: Hands-free Phone Keypad
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHi) "हाथ लगाए बिना डायल पैड" else "Hands-Free Dialpad",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = if (dialNumber.isEmpty()) "___" else dialNumber,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp
                            ),
                            color = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1: 1, 2, 3
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("1", "2", "3").forEach { digit ->
                            HandsFreeButton(
                                id = "dial_$digit",
                                label = digit,
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                accentColor = CyberCyan,
                                onClick = { dialNumber += digit }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 2: 4, 5, 6
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("4", "5", "6").forEach { digit ->
                            HandsFreeButton(
                                id = "dial_$digit",
                                label = digit,
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                accentColor = CyberCyan,
                                onClick = { dialNumber += digit }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 3: 7, 8, 9
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("7", "8", "9").forEach { digit ->
                            HandsFreeButton(
                                id = "dial_$digit",
                                label = digit,
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                accentColor = CyberCyan,
                                onClick = { dialNumber += digit }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 4: Clear, 0, Call
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        HandsFreeButton(
                            id = "dial_clear",
                            label = if (isHi) "साफ़" else "Clear",
                            icon = Icons.Default.Backspace,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            accentColor = SolarAmber,
                            onClick = {
                                if (dialNumber.isNotEmpty()) dialNumber = dialNumber.dropLast(1)
                            }
                        )

                        HandsFreeButton(
                            id = "dial_0",
                            label = "0",
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            accentColor = CyberCyan,
                            onClick = { dialNumber += "0" }
                        )

                        HandsFreeButton(
                            id = "dial_call",
                            label = if (isHi) "कॉल" else "Call",
                            icon = Icons.Default.Call,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            accentColor = EmeraldGreen,
                            onClick = {
                                viewModel.showActionNotification(
                                    if (isHi) "कॉल लगाया जा रहा है: $dialNumber" else "Simulating call to $dialNumber"
                                )
                                viewModel.feedbackUtil.speak(if (isHi) "कॉल डायल किया गया" else "Calling number")
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom dock
        }
    }
}
