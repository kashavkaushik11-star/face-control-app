package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ControlSettings
import com.example.model.FaceTrackingData
import com.example.viewmodel.FaceControlViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Face Control", appName)
    }

    @Test
    fun `viewmodel initializes and handles face tracking data`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = FaceControlViewModel(app)

        assertNotNull(viewModel.settings.value)
        assertEquals(true, viewModel.settings.value.isHindi)

        // Simulate face data
        val faceData = FaceTrackingData(
            eulerX = 10f,
            eulerY = 5f,
            hasFace = true
        )
        viewModel.onFaceDataReceived(faceData)

        assertEquals(10f, viewModel.trackingData.value.eulerX)
        assertEquals(true, viewModel.trackingData.value.hasFace)

        // Calibrate neutral center
        viewModel.calibrateNeutralCenter()
        assertEquals(true, viewModel.calibrationData.value.isCalibrated)
        assertEquals(10f, viewModel.calibrationData.value.neutralPitch)
    }
}
