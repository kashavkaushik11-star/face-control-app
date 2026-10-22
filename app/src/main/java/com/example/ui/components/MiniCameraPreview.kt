package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraPreviewView
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TechSurface
import com.example.ui.theme.TechSurfaceBorder
import com.example.viewmodel.FaceControlViewModel

@Composable
fun MiniCameraPreview(
    viewModel: FaceControlViewModel,
    modifier: Modifier = Modifier
) {
    val trackingData by viewModel.trackingData.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var isExpanded by remember { mutableStateOf(true) }

    if (!settings.showMiniCamera) return

    Surface(
        modifier = modifier
            .width(if (isExpanded) 190.dp else 52.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = TechSurface.copy(alpha = 0.94f),
        border = BorderStroke(
            1.5.dp,
            if (trackingData.hasFace) EmeraldGreen.copy(alpha = 0.8f) else CoralRed.copy(alpha = 0.6f)
        ),
        shadowElevation = 8.dp
    ) {
        if (!isExpanded) {
            // Collapsed pill
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clickable { isExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (trackingData.hasFace) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Expand Camera",
                    tint = if (trackingData.hasFace) EmeraldGreen else CoralRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Expanded preview with camera feed & gauges
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (trackingData.hasFace) EmeraldGreen else CoralRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (trackingData.hasFace) "FACE OK" else "SEARCHING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (trackingData.hasFace) EmeraldGreen else CoralRed
                        )
                    }

                    Row {
                        Text(
                            text = "${trackingData.fps} FPS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Collapse",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { isExpanded = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Front Camera Surface View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black)
                ) {
                    CameraPreviewView(
                        modifier = Modifier.fillMaxSize(),
                        onFaceTracked = { viewModel.onFaceDataReceived(it) }
                    )

                    // Target Reticle in Camera Center
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Telemetry meters (Pitch, Yaw, Blink)
                val leftEye = trackingData.leftEyeOpenProb ?: 0f
                val rightEye = trackingData.rightEyeOpenProb ?: 0f
                val smile = trackingData.smilingProb ?: 0f

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Eyes / आँखें",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.LightGray
                        )
                        Text(
                            text = "L: ${(leftEye * 100).toInt()}%  R: ${(rightEye * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (leftEye < settings.blinkThreshold || rightEye < settings.blinkThreshold) SolarAmber else CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { leftEye },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (leftEye < settings.blinkThreshold) SolarAmber else CyberCyan,
                            trackColor = TechSurfaceBorder
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        LinearProgressIndicator(
                            progress = { rightEye },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (rightEye < settings.blinkThreshold) SolarAmber else CyberCyan,
                            trackColor = TechSurfaceBorder
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Angles info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pitch: ${trackingData.eulerX.toInt()}°",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Yaw: ${trackingData.eulerY.toInt()}°",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick buttons row on preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Calibrate Center button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.calibrateNeutralCenter() },
                        color = CyberCyan.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = "Calibrate",
                                tint = CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Calibrate",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CyberCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Pause / Resume toggle button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleTrackingPause() },
                        color = if (settings.isTrackingPaused) EmeraldGreen.copy(alpha = 0.25f) else CoralRed.copy(alpha = 0.2f),
                        border = BorderStroke(
                            1.dp,
                            if (settings.isTrackingPaused) EmeraldGreen.copy(alpha = 0.6f) else CoralRed.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (settings.isTrackingPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (settings.isTrackingPaused) "Resume" else "Pause",
                                tint = if (settings.isTrackingPaused) EmeraldGreen else CoralRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (settings.isTrackingPaused) "Resume" else "Pause",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (settings.isTrackingPaused) EmeraldGreen else CoralRed
                            )
                        }
                    }
                }
            }
        }
    }
}
