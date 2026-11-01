package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyDeep
import com.example.ui.theme.GalaxyViolet
import com.example.ui.theme.GalaxyVoid
import kotlin.random.Random

private data class Star(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Int,
    val color: Color
)

@Composable
fun GalaxyStarfield(
    modifier: Modifier = Modifier,
    starCount: Int = 55
) {
    val infiniteTransition = rememberInfiniteTransition(label = "galaxy_twinkle")
    val twinklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle_phase"
    )

    // Pre-calculate fixed star positions using a constant seed
    val stars = remember(starCount) {
        val random = Random(42)
        List(starCount) {
            val colorPicker = random.nextFloat()
            val color = when {
                colorPicker > 0.85f -> GalaxyCyan
                colorPicker > 0.70f -> GalaxyViolet.copy(alpha = 0.9f)
                else -> Color.White
            }
            Star(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                radius = random.nextFloat() * 2.2f + 0.8f,
                baseAlpha = random.nextFloat() * 0.5f + 0.25f,
                twinkleSpeed = random.nextInt(1, 4),
                color = color
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep cosmic background with subtle nebula gradients
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    GalaxyDeep,
                    GalaxyVoid
                ),
                center = Offset(w * 0.3f, h * 0.2f),
                radius = w * 1.2f
            )
        )

        // Soft celestial nebula accent in top right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GalaxyCyan.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(w * 0.85f, h * 0.15f),
                radius = w * 0.6f
            ),
            radius = w * 0.6f,
            center = Offset(w * 0.85f, h * 0.15f)
        )

        // Soft celestial violet haze in bottom left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GalaxyViolet.copy(alpha = 0.07f),
                    Color.Transparent
                ),
                center = Offset(w * 0.15f, h * 0.85f),
                radius = w * 0.7f
            ),
            radius = w * 0.7f,
            center = Offset(w * 0.15f, h * 0.85f)
        )

        // Draw twinkling stars
        stars.forEach { star ->
            val twinkleMod = kotlin.math.sin(twinklePhase * star.twinkleSpeed + (star.xRatio * 10f)).toFloat()
            val currentAlpha = (star.baseAlpha + twinkleMod * 0.25f).coerceIn(0.1f, 0.95f)
            drawCircle(
                color = star.color.copy(alpha = currentAlpha),
                radius = star.radius,
                center = Offset(star.xRatio * w, star.yRatio * h)
            )
        }
    }
}
