package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InteractiveTarget
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TechSurface
import com.example.ui.theme.TechSurfaceBorder
import com.example.ui.theme.TechSurfaceVariant
import com.example.viewmodel.FaceControlViewModel

@Composable
fun HandsFreeButton(
    id: String,
    label: String,
    subLabel: String? = null,
    icon: ImageVector? = null,
    viewModel: FaceControlViewModel,
    modifier: Modifier = Modifier,
    accentColor: Color = CyberCyan,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val hoveredTargetId by viewModel.hoveredTargetId.collectAsState()
    val dwellProgress by viewModel.dwellProgress.collectAsState()
    val isHovered = hoveredTargetId == id

    // Cleanly register/unregister in ViewModel target registry
    DisposableEffect(id, label, enabled) {
        onDispose {
            viewModel.unregisterTarget(id)
        }
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isHovered) SolarAmber else if (enabled) TechSurfaceBorder else TechSurfaceBorder.copy(alpha = 0.3f),
        animationSpec = tween(150),
        label = "borderColor"
    )

    val animatedElevation by animateFloatAsState(
        targetValue = if (isHovered) 8f else 1f,
        label = "elevation"
    )

    Surface(
        modifier = modifier
            .testTag("hands_free_btn_$id")
            .onGloballyPositioned { coordinates ->
                if (enabled) {
                    val rootBounds = coordinates.boundsInRoot()
                    viewModel.registerTarget(
                        InteractiveTarget(
                            id = id,
                            bounds = rootBounds,
                            label = label,
                            onTrigger = onClick
                        )
                    )
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) {
                viewModel.feedbackUtil.vibrateClick()
                viewModel.feedbackUtil.playClickSound()
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = if (isHovered) TechSurfaceVariant else TechSurface,
        border = BorderStroke(if (isHovered) 2.5.dp else 1.dp, animatedBorderColor),
        shadowElevation = animatedElevation.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Hover dwell progress fill background
            if (isHovered && dwellProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    SolarAmber.copy(alpha = 0.18f),
                                    SolarAmber.copy(alpha = 0.35f * dwellProgress)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isHovered) SolarAmber else accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Progress ring around icon when hovered
                    if (isHovered && dwellProgress > 0f) {
                        CircularProgressIndicator(
                            progress = { dwellProgress },
                            modifier = Modifier.size(42.dp),
                            color = SolarAmber,
                            strokeWidth = 3.dp,
                            trackColor = Color.Transparent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = if (isHovered) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (isHovered) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                if (subLabel != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = if (isHovered) SolarAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
