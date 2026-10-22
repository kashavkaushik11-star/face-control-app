package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.FaceControlAccessibilityService
import com.example.ui.components.MiniCameraPreview
import com.example.ui.components.PointerOverlay
import com.example.ui.components.QuickActionDock
import com.example.ui.screens.AccessibilityScreen
import com.example.ui.screens.CalibrationScreen
import com.example.ui.screens.PlaygroundScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechSurface
import com.example.ui.theme.TechSurfaceBorder
import com.example.ui.theme.TechSurfaceVariant
import com.example.viewmodel.FaceControlViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FaceControlViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

enum class AppTab(val titleEn: String, val titleHi: String, val icon: ImageVector) {
    PLAYGROUND("Playground", "नियंत्रण", Icons.Default.SportsEsports),
    ACCESSIBILITY("Accessibility", "एक्सेसिबिलिटी", Icons.Default.AccessibilityNew),
    CALIBRATION("Calibration", "कैलिब्रेशन", Icons.Default.CenterFocusStrong),
    SETTINGS("Settings", "सेटिंग्स", Icons.Default.Settings)
}

@Composable
fun MainAppScreen(viewModel: FaceControlViewModel) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val settings by viewModel.settings.collectAsState()
    val trackingData by viewModel.trackingData.collectAsState()
    val isAccessibilityActive by FaceControlAccessibilityService.isServiceActive.collectAsState()
    val lastActionText by viewModel.lastActionText.collectAsState()
    val isHi = settings.isHindi

    var selectedTab by remember { mutableIntStateOf(0) }

    // Handle back button on sub-tabs
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            viewModel.showActionNotification(
                if (isHi) "कैमरा अनुमति मिल गई! चेहरा ट्रैक हो रहा है।" else "Camera permission granted! Tracking ready."
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(TechDarkBackground)
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        LaunchedEffect(widthPx, heightPx) {
            viewModel.setScreenDimensions(widthPx, heightPx)
        }

        if (!hasCameraPermission) {
            // Permission Request State
            CameraPermissionRequiredScreen(
                isHindi = isHi,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            )
        } else {
            // Main Hands-Free Interface
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = TechDarkBackground,
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TechDarkBackground)
                            .statusBarsPadding()
                    ) {
                        // Header bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Title & Logo
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CyberCyan.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                                    modifier = Modifier.size(34.dp)
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
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isHi) "फेस कंट्रोल" else "Face Control",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isHi) "हाथ लगाए बिना नियंत्रण" else "Hands-Free Navigation",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = CyberCyan
                                    )
                                }
                            }

                            // Status Pills (Face Status, Accessibility, Language)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Face status indicator pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (trackingData.hasFace) EmeraldGreen.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, if (trackingData.hasFace) EmeraldGreen else CoralRed)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (trackingData.hasFace) EmeraldGreen else CoralRed)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (trackingData.hasFace) "FACE" else "LOST",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = if (trackingData.hasFace) EmeraldGreen else CoralRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Language Switcher Button
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = TechSurfaceVariant,
                                    border = BorderStroke(1.dp, TechSurfaceBorder),
                                    modifier = Modifier.clickable {
                                        viewModel.updateSettings { it.copy(isHindi = !it.isHindi) }
                                    }
                                ) {
                                    Text(
                                        text = if (isHi) "EN" else "हिन्दी",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = SolarAmber
                                    )
                                }
                            }
                        }

                        // Last action feedback banner
                        if (lastActionText != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = TechSurfaceVariant.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = lastActionText.orEmpty(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = SolarAmber,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // Quick Action Accessible Dock (Back, Home, Recents, Pause, Calibrate)
                        QuickActionDock(
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        // Bottom Navigation Tabs
                        NavigationBar(
                            containerColor = TechSurface,
                            tonalElevation = 8.dp
                        ) {
                            AppTab.entries.forEachIndexed { index, tab ->
                                val isSelected = selectedTab == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { selectedTab = index },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = if (isHi) tab.titleHi else tab.titleEn
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (isHi) tab.titleHi else tab.titleEn,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CyberCyan,
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan.copy(alpha = 0.15f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Active Tab Screen
                    when (selectedTab) {
                        0 -> PlaygroundScreen(viewModel = viewModel)
                        1 -> AccessibilityScreen(viewModel = viewModel)
                        2 -> CalibrationScreen(viewModel = viewModel)
                        3 -> SettingsScreen(viewModel = viewModel)
                    }

                    // Floating Mini Camera Preview (in top-right corner)
                    MiniCameraPreview(
                        viewModel = viewModel,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 12.dp)
                    )
                }
            }

            // High Priority Layer: Pointer Overlay (Crosshair & Dwell Ring rendered at top)
            PointerOverlay(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun CameraPermissionRequiredScreen(
    isHindi: Boolean,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = CyberCyan.copy(alpha = 0.15f),
                border = BorderStroke(2.dp, CyberCyan),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isHindi) "कैमरा अनुमति आवश्यक है" else "Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isHindi) {
                    "हाथ लगाए बिना चेहरे और सिर की गतिविधियों से फोन को नियंत्रित करने के लिए फ्रंट कैमरे की अनुमति आवश्यक है। वीडियो कहीं भेजा या सहेजा नहीं जाता, सभी गणनाएँ सीधे आपके फोन पर होती हैं।"
                } else {
                    "Face Control uses the front camera on-device to track head movements and eye blinks for hands-free navigation. Video is processed strictly on-device in real-time and never recorded or transmitted."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (isHindi) "कैमरा अनुमति दें" else "Grant Camera Permission",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
