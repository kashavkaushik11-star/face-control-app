package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * LifecycleOwner implementation enabling CameraX to run in a background AccessibilityService.
 */
class ServiceLifecycleOwner : LifecycleOwner {
    private val registry = LifecycleRegistry(this)
    init {
        registry.currentState = Lifecycle.State.CREATED
    }
    fun start() {
        registry.currentState = Lifecycle.State.STARTED
        registry.currentState = Lifecycle.State.RESUMED
    }
    fun stop() {
        registry.currentState = Lifecycle.State.DESTROYED
    }
    override val lifecycle: Lifecycle get() = registry
}

/**
 * Manages the floating full-screen cursor overlay, camera tracking, and dwell click dispatch
 * across all Android apps.
 */
class SystemOverlayManager(private val service: FaceControlAccessibilityService) {

    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val lifecycleOwner = ServiceLifecycleOwner()
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var overlayView: CursorOverlayView? = null
    private var isOverlayAttached = false

    // Screen dimensions
    private var screenWidth = 1080f
    private var screenHeight = 2400f

    // Cursor position and smoothing
    private var cursorX = 540f
    private var cursorY = 1200f
    private var targetX = 540f
    private var targetY = 1200f

    // Neutral calibration
    private var neutralPitch = 0f
    private var neutralYaw = 0f
    private var isCalibrated = false
    private val deadzone = 2.0f
    private val sensitivityX = 2.2f
    private val sensitivityY = 2.2f

    // Dwell click engine
    private var dwellAnchorX = 540f
    private var dwellAnchorY = 1200f
    private var dwellStartTime = 0L
    private var dwellProgress = 0f
    private val dwellDurationMs = 1200L
    private var clickCooldownUntil = 0L

    // Blink tracking
    private var isEyesClosed = false
    private var eyeClosedStartTime = 0L
    private val blinkThreshold = 0.28f

    // Tracking state
    private var isTrackingPaused = false
    private var hasFace = false

    // Audio / Haptic feedback
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
        } catch (e: Exception) {
            // ignore
        }
        try {
            @Suppress("DEPRECATION")
            vibrator = service.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } catch (e: Exception) {
            // ignore
        }

        val metrics = service.resources.displayMetrics
        screenWidth = metrics.widthPixels.toFloat()
        screenHeight = metrics.heightPixels.toFloat()
        cursorX = screenWidth / 2f
        cursorY = screenHeight / 2f
    }

    fun startOverlay() {
        if (isOverlayAttached) return

        val view = CursorOverlayView(service)
        overlayView = view

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, // Lets user touch normally underneath!
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager.addView(view, layoutParams)
            isOverlayAttached = true
            startCameraPipeline()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopOverlay() {
        if (!isOverlayAttached) return
        try {
            lifecycleOwner.stop()
            overlayView?.let { windowManager.removeView(it) }
            overlayView = null
            isOverlayAttached = false
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCameraPipeline() {
        lifecycleOwner.start()

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()

        val detector = FaceDetection.getClient(options)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(service)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImage(imageProxy, detector)
                }

                val selector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(service))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(
        imageProxy: ImageProxy,
        detector: com.google.mlkit.vision.face.FaceDetector
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val now = System.currentTimeMillis()
                if (faces.isNotEmpty()) {
                    val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() } ?: faces[0]
                    hasFace = true

                    val rawPitch = face.headEulerAngleX
                    val rawYaw = face.headEulerAngleY

                    // Initial auto-calibration on first face detection
                    if (!isCalibrated) {
                        neutralPitch = rawPitch
                        neutralYaw = rawYaw
                        isCalibrated = true
                    }

                    // Process eye blinks
                    val leftEye = face.leftEyeOpenProbability ?: 1f
                    val rightEye = face.rightEyeOpenProbability ?: 1f
                    val bothClosed = leftEye < blinkThreshold && rightEye < blinkThreshold

                    if (bothClosed) {
                        if (!isEyesClosed) {
                            isEyesClosed = true
                            eyeClosedStartTime = now
                        } else {
                            // Long blink (1.2s) safety toggle pause/resume
                            if (now - eyeClosedStartTime >= 1200L && eyeClosedStartTime != 0L) {
                                isTrackingPaused = !isTrackingPaused
                                eyeClosedStartTime = 0L
                                playClickFeedback()
                            }
                        }
                    } else {
                        if (isEyesClosed) {
                            val duration = now - eyeClosedStartTime
                            isEyesClosed = false
                            if (duration in 140L..480L && !isTrackingPaused) {
                                // Deliberate blink click!
                                triggerSystemTap(cursorX, cursorY)
                            }
                            eyeClosedStartTime = 0L
                        }
                    }

                    if (!isTrackingPaused) {
                        val deltaPitch = rawPitch - neutralPitch
                        val deltaYaw = rawYaw - neutralYaw

                        val effYaw = if (abs(deltaYaw) < deadzone) 0f else deltaYaw - (if (deltaYaw > 0) deadzone else -deadzone)
                        val effPitch = if (abs(deltaPitch) < deadzone) 0f else deltaPitch - (if (deltaPitch > 0) deadzone else -deadzone)

                        val centerX = screenWidth / 2f
                        val centerY = screenHeight / 2f

                        targetX = (centerX + effYaw * sensitivityX * (screenWidth / 26f)).coerceIn(40f, screenWidth - 40f)
                        targetY = (centerY - effPitch * sensitivityY * (screenHeight / 24f)).coerceIn(40f, screenHeight - 40f)

                        // Smooth movement (EMA)
                        val alpha = 0.28f
                        cursorX = cursorX * (1f - alpha) + targetX * alpha
                        cursorY = cursorY * (1f - alpha) + targetY * alpha

                        // Dwell Click Engine
                        val distFromAnchor = abs(cursorX - dwellAnchorX) + abs(cursorY - dwellAnchorY)
                        if (distFromAnchor < 45f) {
                            if (now >= clickCooldownUntil) {
                                if (dwellStartTime == 0L) {
                                    dwellStartTime = now
                                }
                                val elapsed = now - dwellStartTime
                                dwellProgress = (elapsed / dwellDurationMs.toFloat()).coerceIn(0f, 1f)
                                if (dwellProgress >= 1f) {
                                    // Trigger Dwell Click!
                                    triggerSystemTap(cursorX, cursorY)
                                    dwellStartTime = 0L
                                    dwellProgress = 0f
                                }
                            }
                        } else {
                            // Moved to a new location
                            dwellAnchorX = cursorX
                            dwellAnchorY = cursorY
                            dwellStartTime = now
                            dwellProgress = 0f
                        }
                    } else {
                        dwellProgress = 0f
                    }
                } else {
                    hasFace = false
                    dwellProgress = 0f
                }

                // Request overlay redraw
                overlayView?.postInvalidate()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun triggerSystemTap(x: Float, y: Float) {
        clickCooldownUntil = System.currentTimeMillis() + 900L
        playClickFeedback()
        FaceControlAccessibilityService.dispatchTap(x, y)
    }

    private fun playClickFeedback() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
        } catch (e: Exception) {
            // ignore
        }
        try {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(35)
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Custom full-screen transparent view that renders the glowing cursor and dwell ring
     * over all apps on the phone.
     */
    private inner class CursorOverlayView(context: Context) : View(context) {

        private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF") // Cyber Cyan
            style = Paint.Style.STROKE
            strokeWidth = 5f
            strokeCap = Paint.Cap.ROUND
        }

        private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.FILL
        }

        private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3300E5FF")
            style = Paint.Style.FILL
        }

        private val dwellTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#44FFB703")
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

        private val dwellArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFB703") // Solar Amber
            style = Paint.Style.STROKE
            strokeWidth = 9f
            strokeCap = Paint.Cap.ROUND
        }

        private val arcRect = RectF()

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (!hasFace) return

            val cx = cursorX
            val cy = cursorY

            // Color turns coral red if paused
            if (isTrackingPaused) {
                cursorPaint.color = Color.parseColor("#EF476F")
                centerDotPaint.color = Color.parseColor("#EF476F")
            } else {
                cursorPaint.color = Color.parseColor("#00E5FF")
                centerDotPaint.color = Color.parseColor("#00E5FF")
            }

            // Outer soft glow
            canvas.drawCircle(cx, cy, 38f, haloPaint)

            // Dwell Progress Ring
            if (dwellProgress > 0f && !isTrackingPaused) {
                arcRect.set(cx - 48f, cy - 48f, cx + 48f, cy + 48f)
                canvas.drawArc(arcRect, 0f, 360f, false, dwellTrackPaint)
                canvas.drawArc(arcRect, -90f, dwellProgress * 360f, false, dwellArcPaint)
            }

            // Crosshair lines
            val len = 22f
            val gap = 8f
            canvas.drawLine(cx - len - gap, cy, cx - gap, cy, cursorPaint)
            canvas.drawLine(cx + gap, cy, cx + len + gap, cy, cursorPaint)
            canvas.drawLine(cx, cy - len - gap, cx, cy - gap, cursorPaint)
            canvas.drawLine(cx, cy + gap, cx, cy + len + gap, cursorPaint)

            // Center target dot
            canvas.drawCircle(cx, cy, 6f, centerDotPaint)
        }
    }
}
