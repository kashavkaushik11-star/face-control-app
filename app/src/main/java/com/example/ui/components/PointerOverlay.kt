package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.HeadDirection
import com.example.model.PointerStyle
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SolarAmber
import com.example.viewmodel.FaceControlViewModel

@Composable
fun PointerOverlay(
    viewModel: FaceControlViewModel,
    modifier: Modifier = Modifier
) {
    val cursorPosition by viewModel.cursorPosition.collectAsState()
    val dwellProgress by viewModel.dwellProgress.collectAsState()
    val hoveredTargetId by viewModel.hoveredTargetId.collectAsState()
    val headDirection by viewModel.headDirection.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val trackingData by viewModel.trackingData.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorPulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = cursorPosition.x
            val cy = cursorPosition.y
            val isHovered = hoveredTargetId != null
            val isPaused = settings.isTrackingPaused
            val hasFace = trackingData.hasFace

            val cursorColor = when {
                isPaused -> CoralRed
                !hasFace -> Color.Gray
                isHovered -> SolarAmber
                else -> CyberCyan
            }

            val baseRadius = when {
                isHovered -> 22.dp.toPx() * pulseScale
                else -> 16.dp.toPx()
            }

            // Outer Soft Halo
            drawCircle(
                color = cursorColor.copy(alpha = if (isHovered) 0.35f else 0.18f),
                radius = baseRadius + 10.dp.toPx(),
                center = Offset(cx, cy)
            )

            // Dwell Progress Ring (Arc)
            if (dwellProgress > 0f) {
                val ringRadius = baseRadius + 6.dp.toPx()
                val arcSize = Size(ringRadius * 2, ringRadius * 2)
                val topLeft = Offset(cx - ringRadius, cy - ringRadius)

                // Background track
                drawArc(
                    color = SolarAmber.copy(alpha = 0.25f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = 4.dp.toPx())
                )

                // Active progress sweep
                drawArc(
                    color = SolarAmber,
                    startAngle = -90f,
                    sweepAngle = dwellProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Render Cursor Graphic according to user style
            when (settings.pointerStyle) {
                PointerStyle.CROSSHAIR -> {
                    // Precision Crosshair lines
                    val lineLen = 14.dp.toPx()
                    val gap = 5.dp.toPx()
                    val strokeW = 2.5.dp.toPx()

                    // Horizontal lines
                    drawLine(
                        color = cursorColor,
                        start = Offset(cx - lineLen - gap, cy),
                        end = Offset(cx - gap, cy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = cursorColor,
                        start = Offset(cx + gap, cy),
                        end = Offset(cx + lineLen + gap, cy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )

                    // Vertical lines
                    drawLine(
                        color = cursorColor,
                        start = Offset(cx, cy - lineLen - gap),
                        end = Offset(cx, cy - gap),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = cursorColor,
                        start = Offset(cx, cy + gap),
                        end = Offset(cx, cy + lineLen + gap),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )

                    // Center dot
                    drawCircle(
                        color = cursorColor,
                        radius = 3.5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }

                PointerStyle.GLOWING_DOT -> {
                    drawCircle(
                        color = cursorColor,
                        radius = 8.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }

                PointerStyle.RADAR_RETICLE -> {
                    // Outer reticle circle with 4 corner ticks
                    drawCircle(
                        color = cursorColor,
                        radius = baseRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = cursorColor,
                        radius = 4.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            // Direction Arrow indicators when moving head
            if (!isPaused && hasFace && headDirection != HeadDirection.CENTER) {
                val arrowDist = baseRadius + 14.dp.toPx()
                val arrowOffset = when (headDirection) {
                    HeadDirection.UP -> Offset(cx, cy - arrowDist)
                    HeadDirection.DOWN -> Offset(cx, cy + arrowDist)
                    HeadDirection.LEFT -> Offset(cx - arrowDist, cy)
                    HeadDirection.RIGHT -> Offset(cx + arrowDist, cy)
                    else -> null
                }

                if (arrowOffset != null) {
                    drawCircle(
                        color = EmeraldGreen,
                        radius = 4.dp.toPx(),
                        center = arrowOffset
                    )
                }
            }
        }
    }
}
