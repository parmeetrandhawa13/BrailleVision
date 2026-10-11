package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CameraGuidance
import com.example.data.GuidanceDirection
import com.example.ui.theme.BrailleAmberBright
import com.example.ui.theme.BrailleBlack
import com.example.ui.theme.BrailleSuccessGreen

@Composable
fun GuidanceOverlay(
    guidance: CameraGuidance,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val frameColor by animateColorAsState(
        targetValue = if (guidance.isCentered) BrailleSuccessGreen else BrailleAmberBright,
        animationSpec = tween(300),
        label = "frameColor"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Viewfinder reticle canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val rectW = canvasW * 0.82f * pulseScale
            val rectH = canvasH * 0.55f * pulseScale
            val left = (canvasW - rectW) / 2f
            val top = (canvasH - rectH) / 2f

            // Scrim outside rect
            drawRect(
                color = Color.Black.copy(alpha = 0.35f)
            )

            // Transparent cut out border
            drawRoundRect(
                color = frameColor,
                topLeft = Offset(left, top),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = if (guidance.isCentered) 6.dp.toPx() else 3.5.dp.toPx())
            )

            // Tactile alignment crosshairs in center
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f
            val crosshairSize = 24.dp.toPx()

            drawLine(
                color = frameColor.copy(alpha = 0.8f),
                start = Offset(centerX - crosshairSize, centerY),
                end = Offset(centerX + crosshairSize, centerY),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = frameColor.copy(alpha = 0.8f),
                start = Offset(centerX, centerY - crosshairSize),
                end = Offset(centerX, centerY + crosshairSize),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Top prominent Audio & Visual Guidance Banner
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (guidance.isCentered) BrailleSuccessGreen else BrailleBlack.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(
                    2.5.dp,
                    if (guidance.isCentered) Color.White else BrailleAmberBright
                ),
                shadowElevation = 8.dp,
                modifier = Modifier.testTag("guidance_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val icon = when (guidance.direction) {
                        GuidanceDirection.MOVE_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
                        GuidanceDirection.MOVE_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
                        GuidanceDirection.MOVE_UP -> Icons.Default.ArrowUpward
                        GuidanceDirection.MOVE_DOWN -> Icons.Default.ArrowDownward
                        GuidanceDirection.MOVE_CLOSER -> Icons.Default.ZoomIn
                        GuidanceDirection.MOVE_BACK -> Icons.Default.FitScreen
                        GuidanceDirection.PERFECTLY_CENTERED -> Icons.Default.CheckCircle
                        GuidanceDirection.HOLD_STEADY -> Icons.Default.FitScreen
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = guidance.direction.instruction,
                        tint = if (guidance.isCentered) BrailleBlack else BrailleAmberBright,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = guidance.direction.instruction,
                        color = if (guidance.isCentered) BrailleBlack else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Direction Arrow Indicators at screen boundaries
            when (guidance.direction) {
                GuidanceDirection.MOVE_LEFT -> {
                    Text(
                        text = "◀ MOVE CAMERA LEFT",
                        color = BrailleAmberBright,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .background(BrailleBlack.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                GuidanceDirection.MOVE_RIGHT -> {
                    Text(
                        text = "MOVE CAMERA RIGHT ▶",
                        color = BrailleAmberBright,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .background(BrailleBlack.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                GuidanceDirection.PERFECTLY_CENTERED -> {
                    Text(
                        text = "✓ PERFECTLY CENTERED • HOLD STILL",
                        color = BrailleSuccessGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .background(BrailleBlack.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                else -> {}
            }
        }
    }
}
