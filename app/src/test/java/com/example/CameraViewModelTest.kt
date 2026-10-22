package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.model.HeadDirection
import com.example.viewmodel.CameraViewModel
import com.example.viewmodel.HeadOrientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CameraViewModelTest {

    @Test
    fun `test CameraViewModel initialization`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = CameraViewModel(app)

        assertNotNull(viewModel.headOrientation.value)
        assertFalse(viewModel.headOrientation.value.hasFace)
        assertEquals(HeadDirection.CENTER, viewModel.headOrientation.value.direction)
        assertFalse(viewModel.isProcessingPaused.value)
    }

    @Test
    fun `test HeadOrientation helper properties`() {
        val center = HeadOrientation(yaw = 0f, pitch = 0f, direction = HeadDirection.CENTER)
        assertTrue(center.isCentered)
        assertFalse(center.isNoddingUp)
        assertFalse(center.isTurningLeft)

        val up = HeadOrientation(yaw = 0f, pitch = 15f, direction = HeadDirection.UP)
        assertTrue(up.isNoddingUp)
        assertEquals("15.0°", up.formattedPitch())

        val left = HeadOrientation(yaw = -12f, pitch = 0f, direction = HeadDirection.LEFT)
        assertTrue(left.isTurningLeft)
        assertEquals("-12.0°", left.formattedYaw())

        val right = HeadOrientation(yaw = 14f, pitch = 0f, direction = HeadDirection.RIGHT)
        assertTrue(right.isTurningRight)
        assertEquals("14.0°", right.formattedYaw())
    }

    @Test
    fun `test pause and resume processing`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = CameraViewModel(app)

        viewModel.pauseProcessing()
        assertTrue(viewModel.isProcessingPaused.value)

        viewModel.resumeProcessing()
        assertFalse(viewModel.isProcessingPaused.value)
    }

    @Test
    fun `test updateOrientation calculates head direction`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = CameraViewModel(app)

        // Yaw right: +15 degrees
        viewModel.updateOrientation(yaw = 15f, pitch = 0f)
        assertEquals(HeadDirection.RIGHT, viewModel.headOrientation.value.direction)
        assertTrue(viewModel.headOrientation.value.isTurningRight)

        // Yaw left: -15 degrees
        viewModel.updateOrientation(yaw = -15f, pitch = 0f)
        assertEquals(HeadDirection.LEFT, viewModel.headOrientation.value.direction)
        assertTrue(viewModel.headOrientation.value.isTurningLeft)

        // Pitch up: +12 degrees
        viewModel.updateOrientation(yaw = 0f, pitch = 12f)
        assertEquals(HeadDirection.UP, viewModel.headOrientation.value.direction)
        assertTrue(viewModel.headOrientation.value.isNoddingUp)

        // Pitch down: -12 degrees
        viewModel.updateOrientation(yaw = 0f, pitch = -12f)
        assertEquals(HeadDirection.DOWN, viewModel.headOrientation.value.direction)
        assertTrue(viewModel.headOrientation.value.isNoddingDown)
    }
}
