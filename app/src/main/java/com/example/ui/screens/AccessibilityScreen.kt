package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FaceControlAccessibilityService
import com.example.ui.components.HandsFreeButton
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TechSurface
import com.example.ui.theme.TechSurfaceBorder
import com.example.ui.theme.TechSurfaceVariant
import com.example.viewmodel.FaceControlViewModel

@Composable
fun AccessibilityScreen(
    viewModel: FaceControlViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isHi = settings.isHindi
    val isServiceActive by FaceControlAccessibilityService.isServiceActive.collectAsState()
    val statusMsg by FaceControlAccessibilityService.statusMessage.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Service Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isServiceActive) EmeraldGreen.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.12f)
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isServiceActive) EmeraldGreen else CoralRed.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isServiceActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isServiceActive) EmeraldGreen else CoralRed,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isServiceActive) {
                                        if (isHi) "एक्सेसिबिलिटी सर्विस सक्रिय है" else "Accessibility Service Active"
                                    } else {
                                        if (isHi) "एक्सेसिबिलिटी सर्विस बंद है" else "Accessibility Service Not Active"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = if (isServiceActive) {
                                        if (isHi) "सिस्टम जेस्चर और ग्लोबल बटन तैयार हैं" else "Global actions and gestures ready"
                                    } else {
                                        if (isHi) "अन्य ऐप्स को नियंत्रित करने के लिए सक्षम करें" else "Enable to control other apps and phone buttons"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Open Settings Action Button
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                viewModel.showActionNotification("Could not open Settings automatically")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isServiceActive) TechSurfaceVariant else CyberCyan
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = if (isServiceActive) CyberCyan else Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHi) "Android एक्सेसिबिलिटी सेटिंग्स खोलें" else "Open Android Accessibility Settings",
                            color = if (isServiceActive) CyberCyan else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Setup Guide Steps
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHi) "एक्सेसिबिलिटी सर्विस चालू करने की विधि" else "How to Enable System-Wide Control",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val stepsHi = listOf(
                        "ऊपर दिए गए 'Android एक्सेसिबिलिटी सेटिंग्स खोलें' बटन पर टैप करें।",
                        "सूची में 'Downloaded apps' या 'Installed services' में 'Face Control Hands-Free Service' खोजें।",
                        "स्विच को ON करें और 'Allow' पर पुष्टि करें ताकि ऐप जेस्चर और ग्लोबल एक्शन ले सके।",
                        "पुष्टि के बाद इस ऐप में वापस आएँ। अब आप पूरे फोन पर बिना हाथ लगाए एक्शन ले सकते हैं!"
                    )

                    val stepsEn = listOf(
                        "Tap the 'Open Android Accessibility Settings' button above.",
                        "Scroll to 'Downloaded apps' or 'Installed services' and find 'Face Control Hands-Free Service'.",
                        "Toggle the switch to ON and confirm 'Allow' to authorize gestures and navigation.",
                        "Return to this app. You can now trigger Back, Home, and Taps hands-free across Android!"
                    )

                    val steps = if (isHi) stepsHi else stepsEn

                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CyberCyan.copy(alpha = 0.2f),
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = CyberCyan
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Test Global Actions Bench
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHi) "ग्लोबल एक्शन टेस्ट पैड (Hands-Free Test Bench)" else "Global Actions Test Bench",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHi) "सिर से कर्सर लाकर इन बटनों को टेस्ट करें:" else "Hover cursor over any button to test system actions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HandsFreeButton(
                            id = "acc_test_back",
                            label = if (isHi) "बैक (Back)" else "Global Back",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp),
                            accentColor = CyberCyan,
                            onClick = {
                                FaceControlAccessibilityService.performBack()
                            }
                        )

                        HandsFreeButton(
                            id = "acc_test_home",
                            label = if (isHi) "होम (Home)" else "Global Home",
                            icon = Icons.Default.Home,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp),
                            accentColor = EmeraldGreen,
                            onClick = {
                                FaceControlAccessibilityService.performHome()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HandsFreeButton(
                            id = "acc_test_recents",
                            label = if (isHi) "हाल के ऐप्स" else "Recent Apps",
                            icon = Icons.Default.ViewCarousel,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp),
                            accentColor = ElectricViolet,
                            onClick = {
                                FaceControlAccessibilityService.performRecents()
                            }
                        )

                        HandsFreeButton(
                            id = "acc_test_notifs",
                            label = if (isHi) "सूचनाएँ" else "Notifications",
                            icon = Icons.Default.Notifications,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp),
                            accentColor = SolarAmber,
                            onClick = {
                                FaceControlAccessibilityService.performNotifications()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HandsFreeButton(
                            id = "acc_test_settings",
                            label = if (isHi) "त्वरित सेटिंग्स" else "Quick Settings",
                            icon = Icons.Default.Settings,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp),
                            accentColor = CyberCyan,
                            onClick = {
                                FaceControlAccessibilityService.performQuickSettings()
                            }
                        )

                        HandsFreeButton(
                            id = "acc_test_tap",
                            label = if (isHi) "सिस्टम टैप" else "Dispatch Tap",
                            icon = Icons.Default.TouchApp,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp),
                            accentColor = EmeraldGreen,
                            onClick = {
                                val pos = viewModel.cursorPosition.value
                                FaceControlAccessibilityService.dispatchTap(pos.x, pos.y)
                            }
                        )
                    }
                }
            }
        }

        // Limitations & Safety notice
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurfaceVariant),
                border = BorderStroke(1.dp, TechSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SolarAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHi) "महत्वपूर्ण सुरक्षा व सिस्टम सीमाएँ" else "Security & System Boundaries",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHi) {
                            "Android ऑपरेटिंग सिस्टम की सुरक्षा नीतियों के अनुसार, बैंकिंग ऐप्स, पासवर्ड कीबोर्ड और सुरक्षित सिस्टम स्क्रीन पर अनधिकृत क्लिक प्रतिबंधित होते हैं। कैमरे की रोशनी और हेड-मूवमेंट दूरी नियंत्रण की सटीकता को प्रभावित कर सकती है।"
                        } else {
                            "Under Android OS security architecture, secure surfaces (banking apps, system PIN pads, secure credential screens) deliberately block external simulated input for privacy and user safety. Camera lighting and viewing distance directly affect tracking accuracy."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
