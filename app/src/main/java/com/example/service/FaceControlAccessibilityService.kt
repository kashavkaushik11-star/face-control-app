package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

class FaceControlAccessibilityService : AccessibilityService() {

    private var overlayManager: SystemOverlayManager? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
        _isServiceActive.value = true
        _statusMessage.value = "Face Control Accessibility Service Connected"

        try {
            overlayManager = SystemOverlayManager(this)
            overlayManager?.startOverlay()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility events can be monitored here if needed
    }

    override fun onInterrupt() {
        _statusMessage.value = "Face Control Accessibility Service Interrupted"
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayManager?.stopOverlay()
        overlayManager = null
        if (instanceRef?.get() == this) {
            instanceRef = null
        }
        _isServiceActive.value = false
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        overlayManager?.stopOverlay()
        overlayManager = null
        _isServiceActive.value = false
        return super.onUnbind(intent)
    }

    companion object {
        private var instanceRef: WeakReference<FaceControlAccessibilityService>? = null

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _statusMessage = MutableStateFlow<String?>("Not active yet. Enable in Settings.")
        val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

        fun isConnected(): Boolean = _isServiceActive.value && instanceRef?.get() != null

        fun performBack(): Boolean {
            val service = instanceRef?.get() ?: return false
            val success = service.performGlobalAction(GLOBAL_ACTION_BACK)
            _statusMessage.value = if (success) "Executed Back Action" else "Back Action Failed"
            return success
        }

        fun performHome(): Boolean {
            val service = instanceRef?.get() ?: return false
            val success = service.performGlobalAction(GLOBAL_ACTION_HOME)
            _statusMessage.value = if (success) "Executed Home Action" else "Home Action Failed"
            return success
        }

        fun performRecents(): Boolean {
            val service = instanceRef?.get() ?: return false
            val success = service.performGlobalAction(GLOBAL_ACTION_RECENTS)
            _statusMessage.value = if (success) "Opened Recent Apps" else "Recents Failed"
            return success
        }

        fun performNotifications(): Boolean {
            val service = instanceRef?.get() ?: return false
            val success = service.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            _statusMessage.value = if (success) "Opened Notifications" else "Notifications Failed"
            return success
        }

        fun performQuickSettings(): Boolean {
            val service = instanceRef?.get() ?: return false
            val success = service.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            _statusMessage.value = if (success) "Opened Quick Settings" else "Quick Settings Failed"
            return success
        }

        fun performLock(): Boolean {
            val service = instanceRef?.get() ?: return false
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val success = service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                _statusMessage.value = if (success) "Screen Locked" else "Lock Failed"
                success
            } else {
                _statusMessage.value = "Lock Screen requires Android 9+"
                false
            }
        }

        fun dispatchTap(x: Float, y: Float): Boolean {
            val service = instanceRef?.get() ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val path = Path().apply {
                    moveTo(x, y)
                }
                val stroke = GestureDescription.StrokeDescription(path, 0, 50)
                val gesture = GestureDescription.Builder().addStroke(stroke).build()
                return service.dispatchGesture(
                    gesture,
                    object : GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            _statusMessage.value = "Tap dispatched at (${x.toInt()}, ${y.toInt()})"
                        }

                        override fun onCancelled(gestureDescription: GestureDescription?) {
                            _statusMessage.value = "Tap gesture cancelled"
                        }
                    },
                    null
                )
            }
            return false
        }

        fun dispatchSwipe(
            startX: Float,
            startY: Float,
            endX: Float,
            endY: Float,
            durationMs: Long = 250
        ): Boolean {
            val service = instanceRef?.get() ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val path = Path().apply {
                    moveTo(startX, startY)
                    lineTo(endX, endY)
                }
                val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
                val gesture = GestureDescription.Builder().addStroke(stroke).build()
                return service.dispatchGesture(gesture, null, null)
            }
            return false
        }
    }
}
