package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BrailleAmberBright

@Composable
fun AudioWaveIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    waveColor: Color = BrailleAmberBright,
    maxHeight: Dp = 28.dp
) {
    val transition = rememberInfiniteTransition(label = "audio_wave")

    val bar1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isActive) 1f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b1"
    )

    val bar2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isActive) 0.2f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(460, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b2"
    )

    val bar3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isActive) 0.9f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b3"
    )

    val bar4 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = if (isActive) 0.35f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b4"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = listOf(bar1, bar2, bar3, bar4, bar2)
        bars.forEach { fraction ->
            val h = if (isActive) maxHeight * fraction else 6.dp
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.coerceAtLeast(4.dp))
                    .background(waveColor, RoundedCornerShape(2.dp))
            )
        }
    }
}
