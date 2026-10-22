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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClickTrigger
import com.example.model.ControlSettings
import com.example.model.PointerStyle
import com.example.ui.components.HandsFreeButton
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TechSurface
import com.example.ui.theme.TechSurfaceBorder
import com.example.ui.theme.TechSurfaceVariant
import com.example.viewmodel.FaceControlViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: FaceControlViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isHi = settings.isHindi

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Language Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHi) "भाषा (Language)" else "Language Selection",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.updateSettings { it.copy(isHindi = true) }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isHi) CyberCyan.copy(alpha = 0.2f) else TechSurfaceVariant,
                            border = BorderStroke(1.5.dp, if (isHi) CyberCyan else TechSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isHi) {
                                    Icon(Icons.Default.Check, null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = "हिन्दी (Hindi)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHi) CyberCyan else Color.White
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.updateSettings { it.copy(isHindi = false) }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (!isHi) CyberCyan.copy(alpha = 0.2f) else TechSurfaceVariant,
                            border = BorderStroke(1.5.dp, if (!isHi) CyberCyan else TechSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isHi) {
                                    Icon(Icons.Default.Check, null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = "English",
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isHi) CyberCyan else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Click Trigger Options
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = SolarAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHi) "क्लिक ट्रिगर विधि (Click Trigger)" else "Click Trigger Mechanism",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ClickTrigger.entries.forEach { trigger ->
                        val isSelected = settings.clickTrigger == trigger
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.updateSettings { it.copy(clickTrigger = trigger) }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SolarAmber.copy(alpha = 0.18f) else TechSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) SolarAmber else TechSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isHi) trigger.labelHi else trigger.labelEn,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) SolarAmber else Color.White
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SolarAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dwell Duration Picker
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
                            text = if (isHi) "ड्वेल होल्ड समय (Dwell Time)" else "Dwell Click Duration",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "${settings.dwellTimeMs} ms (${(settings.dwellTimeMs / 1000f * 10).roundToInt() / 10f}s)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val dwellOptions = listOf(700L, 1000L, 1200L, 1500L, 2000L)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        dwellOptions.forEach { ms ->
                            val isSel = settings.dwellTimeMs == ms
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.updateSettings { it.copy(dwellTimeMs = ms) }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) CyberCyan.copy(alpha = 0.2f) else TechSurfaceVariant,
                                border = BorderStroke(1.dp, if (isSel) CyberCyan else TechSurfaceBorder)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${(ms / 1000f * 10).roundToInt() / 10f}s",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = if (isSel) CyberCyan else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pointer Appearance Style
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mouse,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHi) "कर्सर स्टाइल (Pointer Style)" else "Cursor Appearance Style",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PointerStyle.entries.forEach { style ->
                            val isSel = settings.pointerStyle == style
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.updateSettings { it.copy(pointerStyle = style) }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) ElectricViolet.copy(alpha = 0.25f) else TechSurfaceVariant,
                                border = BorderStroke(1.dp, if (isSel) ElectricViolet else TechSurfaceBorder)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isHi) style.labelHi else style.labelEn,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSel) ElectricViolet else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Feedback & Display Toggles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHi) "प्रतिक्रिया व स्क्रीन विकल्प (Feedback & Display)" else "Feedback & Display Options",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Haptic Feedback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHi) "वाइब्रेशन फ़ीडबैक (Haptic)" else "Haptic Vibration",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Switch(
                            checked = settings.hapticFeedback,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings { it.copy(hapticFeedback = checked) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldGreen,
                                checkedTrackColor = EmeraldGreen.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Beeps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hearing, null, tint = SolarAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHi) "क्लिक साउंड बीप (Audio Beep)" else "Audio Click Tone",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Switch(
                            checked = settings.soundFeedback,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings { it.copy(soundFeedback = checked) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SolarAmber,
                                checkedTrackColor = SolarAmber.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Floating Mini Camera Feed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHi) "फ़्लोटिंग फ्रंट कैमरा प्रिव्यू" else "Floating Mini-Camera Overlay",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Switch(
                            checked = settings.showMiniCamera,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings { it.copy(showMiniCamera = checked) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = CyberCyan.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }

        // Reset to default button
        item {
            HandsFreeButton(
                id = "settings_reset_btn",
                label = if (isHi) "डिफ़ॉल्ट सेटिंग्स रीसेट करें" else "Reset All Settings To Default",
                icon = Icons.Default.Refresh,
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                accentColor = Color.LightGray,
                onClick = {
                    viewModel.updateSettings { ControlSettings(isHindi = it.isHindi) }
                    viewModel.showActionNotification(
                        if (isHi) "सेटिंग्स रीसेट हुईं" else "Settings Reset to Default"
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
