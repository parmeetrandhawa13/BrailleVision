package com.example.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.data.CameraGuidance
import com.example.data.GuidanceDirection
import java.nio.ByteBuffer

class CameraGuidanceAnalyzer(
    private val onGuidanceUpdated: (CameraGuidance) -> Unit
) : ImageAnalysis.Analyzer {

    @Volatile
    var latestBitmap: android.graphics.Bitmap? = null
        private set

    private var consecutiveCenteredFrames = 0
    private var lastGuidanceTime = 0L
    private val guidanceThrottleMs = 150L
    private var lastBitmapCaptureTime = 0L

    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastGuidanceTime < guidanceThrottleMs) {
            image.close()
            return
        }
        lastGuidanceTime = now

        try {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val width = image.width
            val height = image.height
            val rowStride = plane.rowStride
            val pixelStride = plane.pixelStride

            // Analyze grid of luminance & edge gradients to determine document position
            val guidance = computeGuidance(buffer, width, height, rowStride, pixelStride)

            // Only extract bitmap when frame is stable/centered or periodically to prevent camera lag
            if (guidance.isCentered || latestBitmap == null || now - lastBitmapCaptureTime > 2000L) {
                try {
                    latestBitmap = image.toBitmap()
                    lastBitmapCaptureTime = now
                } catch (e: Throwable) {}
            }

            onGuidanceUpdated(guidance)
        } catch (e: Exception) {
            // Safe fallback
        } finally {
            image.close()
        }
    }

    private fun computeGuidance(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        rowStride: Int,
        pixelStride: Int
    ): CameraGuidance {
        var totalLuminance = 0L
        var leftMass = 0L
        var rightMass = 0L
        var topMass = 0L
        var bottomMass = 0L
        var centerMass = 0L

        val stepX = (width / 30).coerceAtLeast(1)
        val stepY = (height / 30).coerceAtLeast(1)
        var sampledPixels = 0

        val midX = width / 2
        val midY = height / 2
        val centerBoxHalfW = width / 4
        val centerBoxHalfH = height / 4

        for (y in 0 until height step stepY) {
            val rowOffset = y * rowStride
            for (x in 0 until width step stepX) {
                val index = rowOffset + x * pixelStride
                if (index < buffer.limit()) {
                    val lum = buffer.get(index).toInt() and 0xFF
                    totalLuminance += lum
                    sampledPixels++

                    // Measure variance from average (edge/dot contrast)
                    if (lum < 200) { // contrast indicator (braille shadows or text ink)
                        if (x < midX - centerBoxHalfW / 2) leftMass += lum
                        if (x > midX + centerBoxHalfW / 2) rightMass += lum
                        if (y < midY - centerBoxHalfH / 2) topMass += lum
                        if (y > midY + centerBoxHalfH / 2) bottomMass += lum

                        if (kotlin.math.abs(x - midX) <= centerBoxHalfW && kotlin.math.abs(y - midY) <= centerBoxHalfH) {
                            centerMass += lum
                        }
                    }
                }
            }
        }

        val avgBrightness = if (sampledPixels > 0) totalLuminance.toFloat() / (sampledPixels * 255f) else 0.5f
        val horizontalDiff = (rightMass - leftMass).toFloat() / ((leftMass + rightMass + 1L).toFloat())
        val verticalDiff = (bottomMass - topMass).toFloat() / ((topMass + bottomMass + 1L).toFloat())

        // Balance threshold
        val isHorizontallyBalanced = kotlin.math.abs(horizontalDiff) < 0.28f
        val isVerticallyBalanced = kotlin.math.abs(verticalDiff) < 0.32f
        val hasAdequateContrast = (leftMass + rightMass + centerMass) > 5000L

        val direction: GuidanceDirection
        val isCurrentlyCentered: Boolean

        if (!hasAdequateContrast && avgBrightness < 0.15f) {
            direction = GuidanceDirection.MOVE_CLOSER
            isCurrentlyCentered = false
            consecutiveCenteredFrames = 0
        } else if (horizontalDiff < -0.32f) {
            // Left has heavier mass -> document is more to the left -> move camera left to center it
            direction = GuidanceDirection.MOVE_LEFT
            isCurrentlyCentered = false
            consecutiveCenteredFrames = 0
        } else if (horizontalDiff > 0.32f) {
            // Right has heavier mass -> move camera right
            direction = GuidanceDirection.MOVE_RIGHT
            isCurrentlyCentered = false
            consecutiveCenteredFrames = 0
        } else if (verticalDiff < -0.35f) {
            direction = GuidanceDirection.MOVE_UP
            isCurrentlyCentered = false
            consecutiveCenteredFrames = 0
        } else if (verticalDiff > 0.35f) {
            direction = GuidanceDirection.MOVE_DOWN
            isCurrentlyCentered = false
            consecutiveCenteredFrames = 0
        } else {
            // It's in the target zone!
            consecutiveCenteredFrames++
            if (consecutiveCenteredFrames >= 4) {
                direction = GuidanceDirection.PERFECTLY_CENTERED
                isCurrentlyCentered = true
            } else {
                direction = GuidanceDirection.HOLD_STEADY
                isCurrentlyCentered = false
            }
        }

        return CameraGuidance(
            direction = direction,
            horizontalOffset = horizontalDiff.coerceIn(-1f, 1f),
            verticalOffset = verticalDiff.coerceIn(-1f, 1f),
            brightness = avgBrightness,
            isCentered = isCurrentlyCentered,
            stabilityCount = consecutiveCenteredFrames
        )
    }
}
