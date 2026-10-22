package com.example.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Rect
import com.example.model.FaceTrackingData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    private val onFaceTracked: (FaceTrackingData) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)

    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()
    private var currentFps = 0

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        // Calculate processing FPS
        frameCount++
        val now = System.currentTimeMillis()
        if (now - lastFpsTimestamp >= 1000) {
            currentFps = frameCount
            frameCount = 0
            lastFpsTimestamp = now
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    // Pick the largest/most prominent face in view
                    val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() } ?: faces[0]
                    val bounds = face.boundingBox

                    val trackingData = FaceTrackingData(
                        eulerX = face.headEulerAngleX,
                        eulerY = face.headEulerAngleY,
                        eulerZ = face.headEulerAngleZ,
                        leftEyeOpenProb = face.leftEyeOpenProbability,
                        rightEyeOpenProb = face.rightEyeOpenProbability,
                        smilingProb = face.smilingProbability,
                        hasFace = true,
                        faceBounds = Rect(
                            bounds.left.toFloat(),
                            bounds.top.toFloat(),
                            bounds.right.toFloat(),
                            bounds.bottom.toFloat()
                        ),
                        fps = currentFps,
                        timestamp = now
                    )
                    onFaceTracked(trackingData)
                } else {
                    onFaceTracked(
                        FaceTrackingData(
                            hasFace = false,
                            fps = currentFps,
                            timestamp = now
                        )
                    )
                }
            }
            .addOnFailureListener {
                onFaceTracked(
                    FaceTrackingData(
                        hasFace = false,
                        fps = currentFps,
                        timestamp = now
                    )
                )
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
