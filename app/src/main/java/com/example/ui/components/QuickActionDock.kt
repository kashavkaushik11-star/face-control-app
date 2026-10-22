package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.service.FaceControlAccessibilityService
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TechSurface
import com.example.viewmodel.FaceControlViewModel

@Composable
fun QuickActionDock(
    viewModel: FaceControlViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isAccessibilityActive by FaceControlAccessibilityService.isServiceActive.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = TechSurface.copy(alpha = 0.95f),
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Action
            HandsFreeButton(
                id = "dock_back",
                label = if (settings.isHindi) "पीछे" else "Back",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .padding(horizontal = 2.dp),
                accentColor = CyberCyan,
                onClick = {
                    val performed = FaceControlAccessibilityService.performBack()
                    if (!performed) {
                        viewModel.showActionNotification(
                            if (settings.isHindi) "बैक दबाया गया (ऐप स्तर)" else "Back Triggered (In-App)"
                        )
                    }
                }
            )

            // Home Action
            HandsFreeButton(
                id = "dock_home",
                label = if (settings.isHindi) "होम" else "Home",
                icon = Icons.Default.Home,
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .padding(horizontal = 2.dp),
                accentColor = EmeraldGreen,
                onClick = {
                    val performed = FaceControlAccessibilityService.performHome()
                    if (!performed) {
                        viewModel.showActionNotification(
                            if (settings.isHindi) "होम: एक्सेसिबिलिटी सर्विस सक्षम करें" else "Home: Enable Accessibility Service"
                        )
                    }
                }
            )

            // Recents Action
            HandsFreeButton(
                id = "dock_recents",
                label = if (settings.isHindi) "ऐप्स" else "Recents",
                icon = Icons.Default.ViewCarousel,
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .padding(horizontal = 2.dp),
                accentColor = ElectricViolet,
                onClick = {
                    val performed = FaceControlAccessibilityService.performRecents()
                    if (!performed) {
                        viewModel.showActionNotification(
                            if (settings.isHindi) "हाल के ऐप्स: सर्विस चालू करें" else "Recents: Enable Accessibility Service"
                        )
                    }
                }
            )

            // Notifications Action
            HandsFreeButton(
                id = "dock_notifs",
                label = if (settings.isHindi) "सूचनाएँ" else "Notifs",
                icon = Icons.Default.Notifications,
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .padding(horizontal = 2.dp),
                accentColor = SolarAmber,
                onClick = {
                    val performed = FaceControlAccessibilityService.performNotifications()
                    if (!performed) {
                        viewModel.showActionNotification(
                            if (settings.isHindi) "सूचनाएँ: सर्विस चालू करें" else "Notifications: Enable Service"
                        )
                    }
                }
            )

            // Pause / Resume Safety Toggle
            HandsFreeButton(
                id = "dock_pause",
                label = if (settings.isTrackingPaused) (if (settings.isHindi) "चालू" else "Resume") else (if (settings.isHindi) "रोकें" else "Pause"),
                icon = if (settings.isTrackingPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .padding(horizontal = 2.dp),
                accentColor = if (settings.isTrackingPaused) EmeraldGreen else CoralRed,
                onClick = {
                    viewModel.toggleTrackingPause()
                }
            )

            // Calibrate Neutral Center
            HandsFreeButton(
                id = "dock_calibrate",
                label = if (settings.isHindi) "केंद्र" else "Center",
                icon = Icons.Default.CenterFocusStrong,
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .padding(horizontal = 2.dp),
                accentColor = CyberCyan,
                onClick = {
                    viewModel.calibrateNeutralCenter()
                }
            )
        }
    }
}
