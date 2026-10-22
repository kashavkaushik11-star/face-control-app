package com.example.viewmodel

import android.app.Application
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.geometry.Rect
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import com.example.model.FaceTrackingData
import com.example.model.HeadDirection
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * Data class representing head orientation in degrees and discrete direction.
 */
data class HeadOrientation(
    val yaw: Float = 0f,       // Head horizontal rotation: Left (-) / Right (+)
    val pitch: Float = 0f,     // Head vertical tilt: Down (-) / Up (+)
    val roll: Float = 0f,      // Head tilt roll: Left (-) / Right (+)
    val direction: HeadDirection = HeadDirection.CENTER,
    val hasFace: Boolean = false,
    val neutralYaw: Float = 0f,
    val neutralPitch: Float = 0f,
    val effectiveYaw: Float = 0f,
    val effectivePitch: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isNoddingUp: Boolean get() = direction == HeadDirection.UP
    val isNoddingDown: Boolean get() = direction == HeadDirection.DOWN
    val isTurningLeft: Boolean get() = direction == HeadDirection.LEFT
    val isTurningRight: Boolean get() = direction == HeadDirection.RIGHT
    val isCentered: Boolean get() = direction == HeadDirection.CENTER

    fun formattedYaw(): String = "%.1f°".format(yaw)
    fun formattedPitch(): String = "%.1f°".format(pitch)
    fun formattedRoll(): String = "%.1f°".format(roll)
}

/**
 * Lifecycle state of the camera pipeline.
 */
sealed interface CameraState {
    data object Idle : CameraState
    data object Initializing : CameraState
    data object Streaming : CameraState
    data object Paused : CameraState
    data class Error(val message: String) : CameraState
}

/**
 * ViewModel that utilizes ML Kit's Face Detection to process the camera stream
 * and detect real-time head orientation (yaw, pitch, roll), eye states, and gestures.
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    private var faceDetectorInstance: com.google.mlkit.vision.face.FaceDetector? = null

    private fun getFaceDetector(): com.google.mlkit.vision.face.FaceDetector {
        var detector = faceDetectorInstance
        if (detector == null) {
            detector = FaceDetection.getClient(detectorOptions)
            faceDetectorInstance = detector
        }
        return detector
    }

    // CameraProvider reference
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    // Exposed StateFlows
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _headOrientation = MutableStateFlow(HeadOrientation())
    val headOrientation: StateFlow<HeadOrientation> = _headOrientation.asStateFlow()

    private val _trackingData = MutableStateFlow(FaceTrackingData())
    val trackingData: StateFlow<FaceTrackingData> = _trackingData.asStateFlow()

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _isProcessingPaused = MutableStateFlow(false)
    val isProcessingPaused: StateFlow<Boolean> = _isProcessingPaused.asStateFlow()

    // Calibration offsets
    private var neutralPitch = 0f
    private var neutralYaw = 0f
    private var neutralRoll = 0f
    private var isCalibrated = false
    private var deadZoneDegrees = 2.0f

    // FPS counter bookkeeping
    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()

    /**
     * Binds CameraX Preview and ImageAnalysis with ML Kit face detection to the given LifecycleOwner.
     */
    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        onCameraBound: (() -> Unit)? = null
    ) {
        _cameraState.value = CameraState.Initializing

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(imageProxy)
                        }
                    }

                val selector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageAnalysis
                )

                _cameraState.value = CameraState.Streaming
                onCameraBound?.invoke()
            } catch (e: Exception) {
                // If front camera fails (e.g. emulator without front cam), attempt fallback to back camera
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    try {
                        lensFacing = CameraSelector.LENS_FACING_BACK
                        val fallbackSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = surfaceProvider
                        }
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    processImageProxy(imageProxy)
                                }
                            }

                        cameraProvider?.unbindAll()
                        cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            fallbackSelector,
                            preview,
                            imageAnalysis
                        )
                        _cameraState.value = CameraState.Streaming
                        onCameraBound?.invoke()
                        return@addListener
                    } catch (fallbackEx: Exception) {
                        _cameraState.value = CameraState.Error(fallbackEx.localizedMessage ?: "Camera binding failed")
                    }
                } else {
                    _cameraState.value = CameraState.Error(e.localizedMessage ?: "Camera initialization error")
                }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Unbinds the camera from the lifecycle and frees active stream resources.
     */
    fun unbindCamera() {
        try {
            cameraProvider?.unbindAll()
            _cameraState.value = CameraState.Idle
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e.localizedMessage ?: "Error unbinding camera")
        }
    }

    /**
     * Analyzes incoming camera frame with ML Kit Face Detection.
     */
    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || _isProcessingPaused.value) {
            imageProxy.close()
            return
        }

        // Measure FPS
        frameCount++
        val now = System.currentTimeMillis()
        if (now - lastFpsTimestamp >= 1000) {
            _fps.value = frameCount
            frameCount = 0
            lastFpsTimestamp = now
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        getFaceDetector().process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    // Focus on the primary / closest face
                    val primaryFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() } ?: faces[0]

                    val rawPitch = primaryFace.headEulerAngleX  // Pitch: nod up (+) / nod down (-)
                    val rawYaw = primaryFace.headEulerAngleY    // Yaw: turn right (+) / turn left (-)
                    val rawRoll = primaryFace.headEulerAngleZ   // Roll: tilt right (+) / tilt left (-)

                    val bounds = primaryFace.boundingBox

                    val tracking = FaceTrackingData(
                        eulerX = rawPitch,
                        eulerY = rawYaw,
                        eulerZ = rawRoll,
                        leftEyeOpenProb = primaryFace.leftEyeOpenProbability,
                        rightEyeOpenProb = primaryFace.rightEyeOpenProbability,
                        smilingProb = primaryFace.smilingProbability,
                        hasFace = true,
                        faceBounds = Rect(
                            bounds.left.toFloat(),
                            bounds.top.toFloat(),
                            bounds.right.toFloat(),
                            bounds.bottom.toFloat()
                        ),
                        fps = _fps.value,
                        timestamp = now
                    )
                    _trackingData.value = tracking

                    // Compute head orientation with neutral offset and deadzone
                    val deltaPitch = rawPitch - neutralPitch
                    val deltaYaw = rawYaw - neutralYaw
                    val deltaRoll = rawRoll - neutralRoll

                    val effYaw = if (abs(deltaYaw) < deadZoneDegrees) 0f else deltaYaw - (if (deltaYaw > 0) deadZoneDegrees else -deadZoneDegrees)
                    val effPitch = if (abs(deltaPitch) < deadZoneDegrees) 0f else deltaPitch - (if (deltaPitch > 0) deadZoneDegrees else -deadZoneDegrees)

                    val direction = when {
                        effPitch > 7f -> HeadDirection.UP
                        effPitch < -7f -> HeadDirection.DOWN
                        effYaw > 8f -> HeadDirection.RIGHT
                        effYaw < -8f -> HeadDirection.LEFT
                        deltaRoll > 11f -> HeadDirection.TILT_RIGHT
                        deltaRoll < -11f -> HeadDirection.TILT_LEFT
                        else -> HeadDirection.CENTER
                    }

                    _headOrientation.value = HeadOrientation(
                        yaw = rawYaw,
                        pitch = rawPitch,
                        roll = rawRoll,
                        direction = direction,
                        hasFace = true,
                        neutralYaw = neutralYaw,
                        neutralPitch = neutralPitch,
                        effectiveYaw = effYaw,
                        effectivePitch = effPitch,
                        timestamp = now
                    )
                } else {
                    _trackingData.value = FaceTrackingData(
                        hasFace = false,
                        fps = _fps.value,
                        timestamp = now
                    )
                    _headOrientation.value = HeadOrientation(
                        hasFace = false,
                        direction = HeadDirection.CENTER,
                        neutralYaw = neutralYaw,
                        neutralPitch = neutralPitch,
                        timestamp = now
                    )
                }
            }
            .addOnFailureListener { e ->
                _cameraState.value = CameraState.Error(e.localizedMessage ?: "ML Kit Face Detection error")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * Calibrates current head orientation as the neutral center pose.
     */
    fun calibrateNeutral(): Boolean {
        val current = _trackingData.value
        if (current.hasFace) {
            neutralPitch = current.eulerX
            neutralYaw = current.eulerY
            neutralRoll = current.eulerZ
            isCalibrated = true
            return true
        }
        return false
    }

    /**
     * Resets calibration back to absolute 0 degrees.
     */
    fun resetCalibration() {
        neutralPitch = 0f
        neutralYaw = 0f
        neutralRoll = 0f
        isCalibrated = false
    }

    fun setDeadzone(degrees: Float) {
        deadZoneDegrees = degrees.coerceIn(0.5f, 10f)
    }

    fun pauseProcessing() {
        _isProcessingPaused.value = true
        _cameraState.value = CameraState.Paused
    }

    fun resumeProcessing() {
        _isProcessingPaused.value = false
        _cameraState.value = CameraState.Streaming
    }

    fun updateOrientation(yaw: Float, pitch: Float, roll: Float = 0f) {
        val now = System.currentTimeMillis()
        val deltaPitch = pitch - neutralPitch
        val deltaYaw = yaw - neutralYaw
        val effYaw = if (abs(deltaYaw) < deadZoneDegrees) 0f else deltaYaw - (if (deltaYaw > 0) deadZoneDegrees else -deadZoneDegrees)
        val effPitch = if (abs(deltaPitch) < deadZoneDegrees) 0f else deltaPitch - (if (deltaPitch > 0) deadZoneDegrees else -deadZoneDegrees)

        val direction = when {
            effPitch > 7f -> HeadDirection.UP
            effPitch < -7f -> HeadDirection.DOWN
            effYaw > 8f -> HeadDirection.RIGHT
            effYaw < -8f -> HeadDirection.LEFT
            roll > 11f -> HeadDirection.TILT_RIGHT
            roll < -11f -> HeadDirection.TILT_LEFT
            else -> HeadDirection.CENTER
        }

        _headOrientation.value = HeadOrientation(
            yaw = yaw,
            pitch = pitch,
            roll = roll,
            direction = direction,
            hasFace = true,
            neutralYaw = neutralYaw,
            neutralPitch = neutralPitch,
            effectiveYaw = effYaw,
            effectivePitch = effPitch,
            timestamp = now
        )
    }

    override fun onCleared() {
        super.onCleared()
        unbindCamera()
        faceDetectorInstance?.close()
        cameraExecutor.shutdown()
    }
}
