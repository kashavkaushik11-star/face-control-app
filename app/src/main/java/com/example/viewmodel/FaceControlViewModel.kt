package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CalibrationData
import com.example.model.ClickTrigger
import com.example.model.ControlSettings
import com.example.model.FaceTrackingData
import com.example.model.HeadDirection
import com.example.model.InteractiveTarget
import com.example.model.PointerStyle
import com.example.service.FaceControlAccessibilityService
import com.example.util.FeedbackUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class FaceControlViewModel(application: Application) : AndroidViewModel(application) {

    val feedbackUtil = FeedbackUtil(application.applicationContext)

    // Raw tracking state
    private val _trackingData = MutableStateFlow(FaceTrackingData())
    val trackingData: StateFlow<FaceTrackingData> = _trackingData.asStateFlow()

    // Calibration state
    private val _calibrationData = MutableStateFlow(CalibrationData())
    val calibrationData: StateFlow<CalibrationData> = _calibrationData.asStateFlow()

    // Control settings
    private val _settings = MutableStateFlow(ControlSettings())
    val settings: StateFlow<ControlSettings> = _settings.asStateFlow()

    // Screen dimensions
    private var screenWidth = 1080f
    private var screenHeight = 2400f

    // Cursor coordinates (smoothed)
    private val _cursorPosition = MutableStateFlow(Offset(540f, 1200f))
    val cursorPosition: StateFlow<Offset> = _cursorPosition.asStateFlow()

    // Head movement direction
    private val _headDirection = MutableStateFlow(HeadDirection.CENTER)
    val headDirection: StateFlow<HeadDirection> = _headDirection.asStateFlow()

    // Interactive targets on screen
    private val interactiveTargets = mutableMapOf<String, InteractiveTarget>()

    // Current hovered target
    private val _hoveredTargetId = MutableStateFlow<String?>(null)
    val hoveredTargetId: StateFlow<String?> = _hoveredTargetId.asStateFlow()

    // Dwell progress (0f to 1f)
    private val _dwellProgress = MutableStateFlow(0f)
    val dwellProgress: StateFlow<Float> = _dwellProgress.asStateFlow()

    // Action feedback toast/banner
    private val _lastActionText = MutableStateFlow<String?>("Ready / तैयार")
    val lastActionText: StateFlow<String?> = _lastActionText.asStateFlow()

    // Internal timing for dwell and blinks
    private var dwellStartTime = 0L
    private var lastClickedTargetId: String? = null
    private var clickCooldownUntil = 0L

    private var eyeClosedStartTime: Long = 0L
    private var isEyesCurrentlyClosed = false

    private var smileTriggerCooldown = 0L

    fun setScreenDimensions(width: Float, height: Float) {
        if (width > 0 && height > 0) {
            screenWidth = width
            screenHeight = height
            if (_cursorPosition.value == Offset(540f, 1200f)) {
                _cursorPosition.value = Offset(width / 2f, height / 2f)
            }
        }
    }

    fun registerTarget(target: InteractiveTarget) {
        interactiveTargets[target.id] = target
    }

    fun unregisterTarget(id: String) {
        interactiveTargets.remove(id)
        if (_hoveredTargetId.value == id) {
            _hoveredTargetId.value = null
            _dwellProgress.value = 0f
        }
    }

    fun onFaceDataReceived(data: FaceTrackingData) {
        _trackingData.value = data
        val now = System.currentTimeMillis()

        if (!data.hasFace) {
            _dwellProgress.value = 0f
            return
        }

        // Process Long-Blink Safety Pause / Resume Detection
        val leftEye = data.leftEyeOpenProb ?: 1f
        val rightEye = data.rightEyeOpenProb ?: 1f
        val areEyesClosed = leftEye < _settings.value.blinkThreshold && rightEye < _settings.value.blinkThreshold

        if (areEyesClosed) {
            if (!isEyesCurrentlyClosed) {
                isEyesCurrentlyClosed = true
                eyeClosedStartTime = now
            } else {
                // Check if long blink held for safety toggle (1200ms)
                if (now - eyeClosedStartTime >= 1200L && eyeClosedStartTime != 0L) {
                    toggleTrackingPause()
                    eyeClosedStartTime = 0L // reset so it triggers only once
                }
            }
        } else {
            if (isEyesCurrentlyClosed) {
                val closedDuration = now - eyeClosedStartTime
                isEyesCurrentlyClosed = false
                // Deliberate blink trigger (120ms to 450ms)
                if (closedDuration in 120L..500L && !_settings.value.isTrackingPaused) {
                    handleBlinkClick()
                }
                eyeClosedStartTime = 0L
            }
        }

        // If tracking is paused, do not update cursor position or process clicks
        if (_settings.value.isTrackingPaused) {
            _dwellProgress.value = 0f
            return
        }

        // Smile detection
        val smileProb = data.smilingProb ?: 0f
        if (_settings.value.clickTrigger == ClickTrigger.SMILE && smileProb >= _settings.value.smileThreshold) {
            if (now >= smileTriggerCooldown) {
                smileTriggerCooldown = now + 1200L
                triggerCurrentAction("Smile Detected / मुस्कान पहचानी गई")
            }
        }

        // Calculate Head Orientation & Direction
        val neutralPitch = _calibrationData.value.neutralPitch
        val neutralYaw = _calibrationData.value.neutralYaw
        val neutralRoll = _calibrationData.value.neutralRoll

        var deltaPitch = data.eulerX - neutralPitch
        var deltaYaw = data.eulerY - neutralYaw
        val deltaRoll = data.eulerZ - neutralRoll

        // Determine Head Direction
        val dir = when {
            deltaPitch > 8f -> HeadDirection.UP
            deltaPitch < -8f -> HeadDirection.DOWN
            deltaYaw > 9f -> HeadDirection.RIGHT
            deltaYaw < -9f -> HeadDirection.LEFT
            deltaRoll > 12f -> HeadDirection.TILT_RIGHT
            deltaRoll < -12f -> HeadDirection.TILT_LEFT
            else -> HeadDirection.CENTER
        }
        _headDirection.value = dir

        // Apply Deadzone
        val deadzone = _settings.value.deadZoneDegrees
        val effYaw = if (abs(deltaYaw) < deadzone) 0f else deltaYaw - (if (deltaYaw > 0) deadzone else -deadzone)
        val effPitch = if (abs(deltaPitch) < deadzone) 0f else deltaPitch - (if (deltaPitch > 0) deadzone else -deadzone)

        // Map to Screen Coordinates
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f

        // Head yaw moves horizontally. In front camera mirror: turning head to user's right has EulerY > 0 -> screen right
        val targetX = (centerX + effYaw * _settings.value.sensitivityX * (screenWidth / 26f))
            .coerceIn(30f, screenWidth - 30f)

        // Head pitch moves vertically. Nodding head UP has EulerX > 0 -> screen top
        val targetY = (centerY - effPitch * _settings.value.sensitivityY * (screenHeight / 24f))
            .coerceIn(30f, screenHeight - 30f)

        // Exponential Moving Average (EMA) smoothing for jitter-free precision
        val alpha = _settings.value.smoothingFactor.coerceIn(0.05f, 0.9f)
        val currentPos = _cursorPosition.value
        val smoothX = currentPos.x * (1f - alpha) + targetX * alpha
        val smoothY = currentPos.y * (1f - alpha) + targetY * alpha
        val newPos = Offset(smoothX, smoothY)
        _cursorPosition.value = newPos

        // Process Interactive Hover & Dwell Click
        processHoverAndDwell(newPos, now)
    }

    private fun processHoverAndDwell(cursor: Offset, now: Long) {
        val allowDwell = _settings.value.clickTrigger == ClickTrigger.DWELL_ONLY ||
                _settings.value.clickTrigger == ClickTrigger.DWELL_AND_BLINK

        // Find which target the cursor is over
        val hit = interactiveTargets.values.firstOrNull { it.bounds.contains(cursor) }

        if (hit != null) {
            val currentHovered = _hoveredTargetId.value
            if (currentHovered != hit.id) {
                // Just entered a new target
                _hoveredTargetId.value = hit.id
                dwellStartTime = now
                _dwellProgress.value = 0f
                if (_settings.value.hapticFeedback) {
                    feedbackUtil.vibrateHover()
                }
                if (_settings.value.soundFeedback) {
                    feedbackUtil.playDwellStartSound()
                }
            } else {
                // Still hovering over same target
                if (allowDwell && now >= clickCooldownUntil) {
                    val elapsed = now - dwellStartTime
                    val dwellTotal = _settings.value.dwellTimeMs.toFloat()
                    val progress = (elapsed / dwellTotal).coerceIn(0f, 1f)
                    _dwellProgress.value = progress

                    if (progress >= 1f) {
                        // Dwell Click Triggered!
                        executeTargetClick(hit, isDwell = true)
                    }
                }
            }
        } else {
            // Not over any target
            if (_hoveredTargetId.value != null) {
                _hoveredTargetId.value = null
                _dwellProgress.value = 0f
            }
        }
    }

    private fun handleBlinkClick() {
        val allowBlink = _settings.value.clickTrigger == ClickTrigger.BLINK_ONLY ||
                _settings.value.clickTrigger == ClickTrigger.DWELL_AND_BLINK

        if (!allowBlink) return

        val hoveredId = _hoveredTargetId.value
        if (hoveredId != null) {
            val target = interactiveTargets[hoveredId]
            if (target != null) {
                executeTargetClick(target, isDwell = false)
                return
            }
        }

        // If not over an in-app target, dispatch system Accessibility tap if active
        val cursor = _cursorPosition.value
        if (FaceControlAccessibilityService.isConnected()) {
            FaceControlAccessibilityService.dispatchTap(cursor.x, cursor.y)
            showActionNotification("System Tap / स्क्रीन पर टैप: (${cursor.x.toInt()}, ${cursor.y.toInt()})")
            if (_settings.value.hapticFeedback) feedbackUtil.vibrateClick()
            if (_settings.value.soundFeedback) feedbackUtil.playClickSound()
        }
    }

    private fun executeTargetClick(target: InteractiveTarget, isDwell: Boolean) {
        clickCooldownUntil = System.currentTimeMillis() + 900L
        _dwellProgress.value = 0f
        lastClickedTargetId = target.id

        if (_settings.value.hapticFeedback) {
            feedbackUtil.vibrateClick()
        }
        if (_settings.value.soundFeedback) {
            feedbackUtil.playClickSound()
        }

        val triggerMethod = if (isDwell) "Dwell" else "Blink"
        showActionNotification("$triggerMethod: ${target.label}")

        // Execute action
        try {
            target.onTrigger()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerCurrentAction(label: String) {
        val hoveredId = _hoveredTargetId.value
        if (hoveredId != null) {
            interactiveTargets[hoveredId]?.let {
                executeTargetClick(it, isDwell = false)
            }
        } else {
            showActionNotification(label)
        }
    }

    fun calibrateNeutralCenter() {
        val data = _trackingData.value
        if (data.hasFace) {
            _calibrationData.value = CalibrationData(
                neutralPitch = data.eulerX,
                neutralYaw = data.eulerY,
                neutralRoll = data.eulerZ,
                isCalibrated = true
            )
            // Re-center cursor
            _cursorPosition.value = Offset(screenWidth / 2f, screenHeight / 2f)
            showActionNotification(
                if (_settings.value.isHindi) "सेंटर कैलिब्रेट हुआ!" else "Neutral Center Calibrated!"
            )
            feedbackUtil.vibrateClick()
        } else {
            showActionNotification(
                if (_settings.value.isHindi) "चेहरा नहीं दिखा, कृपया कैमरे के सामने आएँ" else "No face detected. Face the camera."
            )
            feedbackUtil.playAlertSound()
        }
    }

    fun toggleTrackingPause() {
        val newState = !_settings.value.isTrackingPaused
        _settings.value = _settings.value.copy(isTrackingPaused = newState)
        _dwellProgress.value = 0f

        val msg = if (newState) {
            if (_settings.value.isHindi) "नियंत्रण रोका गया (Paused)" else "Tracking Paused"
        } else {
            if (_settings.value.isHindi) "नियंत्रण चालू (Resumed)" else "Tracking Resumed"
        }
        showActionNotification(msg)
        feedbackUtil.speak(if (_settings.value.isHindi) (if (newState) "नियंत्रण रोक दिया गया" else "नियंत्रण चालू") else (if (newState) "Paused" else "Resumed"))
        feedbackUtil.vibrateClick()
    }

    fun updateSettings(transform: (ControlSettings) -> ControlSettings) {
        _settings.value = transform(_settings.value)
    }

    fun showActionNotification(text: String) {
        _lastActionText.value = text
    }

    override fun onCleared() {
        super.onCleared()
        feedbackUtil.release()
    }
}
