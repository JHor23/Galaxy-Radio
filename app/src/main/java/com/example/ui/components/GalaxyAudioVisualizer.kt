package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyElectricBlue
import com.example.ui.theme.GalaxyViolet

@Composable
fun MinimalistAudioWave(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    barWidth: Dp = 3.dp,
    maxHeight: Dp = 18.dp,
    activeColor: Color = GalaxyCyan,
    inactiveColor: Color = GalaxyCyan.copy(alpha = 0.35f)
) {
    val transition = rememberInfiniteTransition(label = "audio_bars")
    val anim1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b1"
    )
    val anim2 by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b2"
    )
    val anim3 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b3"
    )
    val anim4 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(620, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b4"
    )

    val factors = listOf(anim1, anim2, anim3, anim4)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val factor = if (isPlaying) factors[i % factors.size] else 0.2f
            val height = maxHeight * factor
            Surface(
                modifier = Modifier
                    .width(barWidth)
                    .height(height.coerceAtLeast(3.dp)),
                shape = RoundedCornerShape(1.5.dp),
                color = if (isPlaying) activeColor else inactiveColor
            ) {}
        }
    }
}

@Composable
fun OrbitalPlanetaryVisualizer(
    isPlaying: Boolean,
    isBuffering: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 220.dp
) {
    val transition = rememberInfiniteTransition(label = "orbital_pulse")

    val pulseScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotateAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_angle"
    )

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.72f

            // Outer orbital ring
            drawCircle(
                color = GalaxyCyan.copy(alpha = if (isPlaying) 0.22f else 0.08f),
                radius = baseRadius * (if (isPlaying) pulseScale else 1f),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Middle orbital ring
            drawCircle(
                color = GalaxyViolet.copy(alpha = if (isPlaying) 0.35f else 0.12f),
                radius = baseRadius * 0.82f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner orbit ring
            drawCircle(
                color = GalaxyElectricBlue.copy(alpha = if (isPlaying) 0.5f else 0.2f),
                radius = baseRadius * 0.62f,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Orbital satellite beacon
            if (isPlaying || isBuffering) {
                val rad = Math.toRadians(rotateAngle.toDouble())
                val satRadius = baseRadius * 0.82f
                val satX = center.x + (satRadius * kotlin.math.cos(rad)).toFloat()
                val satY = center.y + (satRadius * kotlin.math.sin(rad)).toFloat()
                drawCircle(
                    color = GalaxyCyan,
                    radius = 4.5.dp.toPx(),
                    center = Offset(satX, satY)
                )
            }
        }
    }
}
