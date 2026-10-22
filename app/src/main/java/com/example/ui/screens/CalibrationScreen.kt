package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HeadDirection
import com.example.ui.components.HandsFreeButton
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TechSurface
import com.example.ui.theme.TechSurfaceBorder
import com.example.ui.theme.TechSurfaceVariant
import com.example.viewmodel.FaceControlViewModel
import kotlin.math.roundToInt

@Composable
fun CalibrationScreen(
    viewModel: FaceControlViewModel,
    modifier: Modifier = Modifier
) {
    val trackingData by viewModel.trackingData.collectAsState()
    val calibrationData by viewModel.calibrationData.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val headDirection by viewModel.headDirection.collectAsState()
    val isHi = settings.isHindi

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Step 1: Calibrate Neutral Center Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyberCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CenterFocusStrong,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isHi) "चरण 1: न्यूट्रल सेंटर कैलिब्रेशन" else "Step 1: Neutral Pose Calibration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = if (isHi) "फोन को सामान्य दूरी पर रखकर सीधे स्क्रीन की ओर देखें" else "Hold phone comfortably and look straight at center",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2D Head Orientation Radar
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(TechSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cx = w / 2f
                            val cy = h / 2f

                            // Target rings
                            drawCircle(
                                color = TechSurfaceBorder,
                                radius = w * 0.45f,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            drawCircle(
                                color = TechSurfaceBorder.copy(alpha = 0.6f),
                                radius = w * 0.25f,
                                style = Stroke(width = 1.dp.toPx())
                            )
                            // Crosshair axis
                            drawLine(
                                color = TechSurfaceBorder.copy(alpha = 0.5f),
                                start = Offset(0f, cy),
                                end = Offset(w, cy),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = TechSurfaceBorder.copy(alpha = 0.5f),
                                start = Offset(cx, 0f),
                                end = Offset(cx, h),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Head deviation point
                            val pitchOffset = (-(trackingData.eulerX - calibrationData.neutralPitch) * 2.5f).coerceIn(-cy * 0.8f, cy * 0.8f)
                            val yawOffset = ((trackingData.eulerY - calibrationData.neutralYaw) * 2.5f).coerceIn(-cx * 0.8f, cx * 0.8f)
                            val headPos = Offset(cx + yawOffset, cy + pitchOffset)

                            // Radar beacon dot
                            drawCircle(
                                color = if (trackingData.hasFace) CyberCyan else CoralRed,
                                radius = 9.dp.toPx(),
                                center = headPos
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.5.dp.toPx(),
                                center = headPos
                            )
                        }

                        if (!trackingData.hasFace) {
                            Text(
                                text = if (isHi) "चेहरा नहीं दिखा" else "No Face",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CoralRed
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Angle indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Pitch (ऊपर/नीचे)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${trackingData.eulerX.roundToInt()}°",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = CyberCyan
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Yaw (दायाँ/बायाँ)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${trackingData.eulerY.roundToInt()}°",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = CyberCyan
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Roll (झुकाव)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${trackingData.eulerZ.roundToInt()}°",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = CyberCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Hands-Free Calibrate Button
                    HandsFreeButton(
                        id = "calib_neutral_action",
                        label = if (isHi) "वर्तमान स्थिति को न्यूट्रल सेट करें" else "Set Current Pose As Neutral Center",
                        subLabel = if (calibrationData.isCalibrated) "✓ Calibrated (${calibrationData.neutralPitch.toInt()}°, ${calibrationData.neutralYaw.toInt()}°)" else "Tap or Dwell to Calibrate",
                        icon = Icons.Default.CenterFocusStrong,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp),
                        accentColor = CyberCyan,
                        onClick = {
                            viewModel.calibrateNeutralCenter()
                        }
                    )
                }
            }
        }

        // Step 2: Eye Blink & Wink Testing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SolarAmber.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.RemoveRedEye,
                                    contentDescription = null,
                                    tint = SolarAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isHi) "चरण 2: आँखों की पलक टेस्ट (Blink Test)" else "Step 2: Eye Blink Calibration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = if (isHi) "पलक झपकाकर गेज का परीक्षण करें" else "Blink or wink to test eye openness detection",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val leftEye = trackingData.leftEyeOpenProb ?: 1f
                    val rightEye = trackingData.rightEyeOpenProb ?: 1f
                    val threshold = settings.blinkThreshold

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Eye Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = TechSurfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (leftEye < threshold) SolarAmber else TechSurfaceBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isHi) "बायाँ नेत्र (Left)" else "Left Eye",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${(leftEye * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = if (leftEye < threshold) SolarAmber else CyberCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (leftEye < threshold) (if (isHi) "बंद ✕" else "CLOSED ✕") else (if (isHi) "खुला ✓" else "OPEN ✓"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (leftEye < threshold) SolarAmber else EmeraldGreen
                                )
                            }
                        }

                        // Right Eye Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = TechSurfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (rightEye < threshold) SolarAmber else TechSurfaceBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isHi) "दायाँ नेत्र (Right)" else "Right Eye",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${(rightEye * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = if (rightEye < threshold) SolarAmber else CyberCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (rightEye < threshold) (if (isHi) "बंद ✕" else "CLOSED ✕") else (if (isHi) "खुला ✓" else "OPEN ✓"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (rightEye < threshold) SolarAmber else EmeraldGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Smile detection test
                    val smile = trackingData.smilingProb ?: 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHi) "मुस्कान स्तर (Smile Meter): ${(smile * 100).toInt()}%" else "Smile Level: ${(smile * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { smile },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldGreen,
                        trackColor = TechSurfaceBorder
                    )
                }
            }
        }

        // Quick calibration sliders (Deadzone & Sensitivity)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHi) "डेडज़ोन व संवेदनशीलता (Deadzone & Sensitivity)" else "Deadzone & Sensitivity Calibration",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sensitivity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isHi) "कर्सर संवेदनशीलता (Sensitivity)" else "Cursor Sensitivity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(settings.sensitivityX * 10).roundToInt() / 10f}x",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CyberCyan
                        )
                    }
                    Slider(
                        value = settings.sensitivityX,
                        onValueChange = { newVal ->
                            viewModel.updateSettings { it.copy(sensitivityX = newVal, sensitivityY = newVal) }
                        },
                        valueRange = 0.8f..3.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = TechSurfaceBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Deadzone Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isHi) "स्थिर डेडज़ोन (Deadzone Degrees)" else "Neutral Deadzone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${settings.deadZoneDegrees.toInt()}°",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = SolarAmber
                        )
                    }
                    Slider(
                        value = settings.deadZoneDegrees,
                        onValueChange = { newVal ->
                            viewModel.updateSettings { it.copy(deadZoneDegrees = newVal) }
                        },
                        valueRange = 0.5f..5.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = SolarAmber,
                            activeTrackColor = SolarAmber,
                            inactiveTrackColor = TechSurfaceBorder
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
