package com.example.model

import androidx.compose.ui.geometry.Rect

data class FaceTrackingData(
    val eulerX: Float = 0f, // Head Pitch (nodding up (+) / down (-))
    val eulerY: Float = 0f, // Head Yaw (turning right (+) / left (-))
    val eulerZ: Float = 0f, // Head Roll (tilting right (+) / left (-))
    val leftEyeOpenProb: Float? = null,
    val rightEyeOpenProb: Float? = null,
    val smilingProb: Float? = null,
    val hasFace: Boolean = false,
    val faceBounds: Rect? = null,
    val fps: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class CalibrationData(
    val neutralPitch: Float = 0f,
    val neutralYaw: Float = 0f,
    val neutralRoll: Float = 0f,
    val isCalibrated: Boolean = false
)

enum class ClickTrigger(val labelEn: String, val labelHi: String) {
    DWELL_ONLY("Dwell Timer Only", "केवल ड्वेल टाइमर"),
    BLINK_ONLY("Eye Blink Only", "केवल पलक झपकाना"),
    DWELL_AND_BLINK("Dwell + Eye Blink", "ड्वेल + पलक दोनों"),
    SMILE("Smile / Grin", "मुस्कान / स्माइल")
}

enum class PointerStyle(val labelEn: String, val labelHi: String) {
    CROSSHAIR("Precision Crosshair", "क्रॉसहेयर कर्सर"),
    GLOWING_DOT("Glowing Dot", "चमकता डॉट"),
    RADAR_RETICLE("Target Radar", "टारगेट राडार")
}

enum class HeadDirection {
    CENTER,
    UP,
    DOWN,
    LEFT,
    RIGHT,
    TILT_LEFT,
    TILT_RIGHT
}

data class ControlSettings(
    val sensitivityX: Float = 1.8f,
    val sensitivityY: Float = 1.8f,
    val deadZoneDegrees: Float = 2.0f,
    val dwellTimeMs: Long = 1200L,
    val smoothingFactor: Float = 0.25f,
    val clickTrigger: ClickTrigger = ClickTrigger.DWELL_AND_BLINK,
    val pointerStyle: PointerStyle = PointerStyle.CROSSHAIR,
    val blinkThreshold: Float = 0.30f,
    val smileThreshold: Float = 0.65f,
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = true,
    val showMiniCamera: Boolean = true,
    val showTelemetry: Boolean = true,
    val isTrackingPaused: Boolean = false,
    val isHindi: Boolean = true
)

data class InteractiveTarget(
    val id: String,
    val bounds: Rect,
    val label: String,
    val onTrigger: () -> Unit
)
