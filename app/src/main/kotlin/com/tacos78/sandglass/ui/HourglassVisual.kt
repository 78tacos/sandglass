package com.tacos78.sandglass.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.sin

@Composable
fun HourglassVisual(
    remainingFraction: Float,
    running: Boolean,
    modifier: Modifier = Modifier,
) {
    val sand = MaterialTheme.colorScheme.primary
    val sandDeep = MaterialTheme.colorScheme.secondary
    val frame = MaterialTheme.colorScheme.tertiary
    val glass = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    val glassFill = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f)

    val grainPhase by rememberInfiniteTransition(label = "sand-fall").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "grain-phase",
    )

    val remaining = remainingFraction.coerceIn(0f, 1f)
    val pouring = running && remaining > 0f && remaining < 1f

    Canvas(
        modifier = modifier
            .aspectRatio(0.62f)
            .semantics {
                contentDescription = "Hourglass, ${(remaining * 100).toInt()} percent remaining"
            },
    ) {
        val pad = size.minDimension * 0.04f
        val capWidth = size.width * 0.68f
        val capHeight = size.height * 0.055f
        val capLeft = (size.width - capWidth) / 2f
        val glassTop = pad + capHeight
        val glassBottom = size.height - pad - capHeight
        val midY = (glassTop + glassBottom) / 2f
        val centerX = size.width / 2f
        val bulbHalf = capWidth * 0.42f
        val neckHalf = size.width * 0.028f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(sand.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(centerX, midY),
                radius = size.minDimension * 0.72f,
            ),
            radius = size.minDimension * 0.72f,
            center = Offset(centerX, midY),
        )

        val upperGlass = bulbPath(
            centerX = centerX,
            topY = glassTop,
            bottomY = midY,
            topHalf = bulbHalf,
            bottomHalf = neckHalf,
        )
        val lowerGlass = bulbPath(
            centerX = centerX,
            topY = midY,
            bottomY = glassBottom,
            topHalf = neckHalf,
            bottomHalf = bulbHalf,
        )

        drawPath(upperGlass, glassFill)
        drawPath(lowerGlass, glassFill)

        clipPath(upperGlass) {
            drawUpperSand(
                remaining = remaining,
                centerX = centerX,
                glassTop = glassTop,
                midY = midY,
                bulbHalf = bulbHalf,
                neckHalf = neckHalf,
                sand = sand,
                sandDeep = sandDeep,
            )
        }
        clipPath(lowerGlass) {
            drawLowerSand(
                elapsed = 1f - remaining,
                centerX = centerX,
                midY = midY,
                glassBottom = glassBottom,
                bulbHalf = bulbHalf,
                neckHalf = neckHalf,
                sand = sand,
                sandDeep = sandDeep,
            )
        }

        if (pouring) {
            drawFallingSand(
                centerX = centerX,
                midY = midY,
                glassBottom = glassBottom,
                phase = grainPhase,
                sand = sand,
            )
        }

        val stroke = Stroke(width = size.minDimension * 0.018f, cap = StrokeCap.Round)
        drawPath(upperGlass, glass, style = stroke)
        drawPath(lowerGlass, glass, style = stroke)

        val highlight = Path().apply {
            moveTo(centerX - bulbHalf * 0.55f, glassTop + (midY - glassTop) * 0.18f)
            quadraticTo(
                centerX - bulbHalf * 0.72f,
                (glassTop + midY) / 2f,
                centerX - neckHalf * 2.2f,
                midY - (midY - glassTop) * 0.12f,
            )
        }
        drawPath(
            highlight,
            Color.White.copy(alpha = 0.35f),
            style = Stroke(width = size.minDimension * 0.012f, cap = StrokeCap.Round),
        )

        val capRadius = CornerRadius(capHeight * 0.45f, capHeight * 0.45f)
        drawRoundRect(
            color = frame,
            topLeft = Offset(capLeft, pad),
            size = Size(capWidth, capHeight),
            cornerRadius = capRadius,
        )
        drawRoundRect(
            color = frame,
            topLeft = Offset(capLeft, size.height - pad - capHeight),
            size = Size(capWidth, capHeight),
            cornerRadius = capRadius,
        )
    }
}

private fun bulbPath(
    centerX: Float,
    topY: Float,
    bottomY: Float,
    topHalf: Float,
    bottomHalf: Float,
): Path {
    val mid = (topY + bottomY) / 2f
    return Path().apply {
        moveTo(centerX - topHalf, topY)
        lineTo(centerX + topHalf, topY)
        quadraticTo(centerX + topHalf * 0.92f, mid, centerX + bottomHalf, bottomY)
        lineTo(centerX - bottomHalf, bottomY)
        quadraticTo(centerX - topHalf * 0.92f, mid, centerX - topHalf, topY)
        close()
    }
}

private fun DrawScope.drawUpperSand(
    remaining: Float,
    centerX: Float,
    glassTop: Float,
    midY: Float,
    bulbHalf: Float,
    neckHalf: Float,
    sand: Color,
    sandDeep: Color,
) {
    if (remaining <= 0.001f) return
    val chamber = midY - glassTop
    val surfaceY = lerp(midY - chamber * 0.04f, glassTop + chamber * 0.08f, remaining)
    val t = ((surfaceY - glassTop) / chamber).coerceIn(0f, 1f)
    val half = lerp(bulbHalf, neckHalf, t)
    val path = Path().apply {
        moveTo(centerX - half, surfaceY)
        quadraticTo(centerX, surfaceY + chamber * 0.04f * remaining, centerX + half, surfaceY)
        lineTo(centerX + neckHalf, midY)
        lineTo(centerX - neckHalf, midY)
        close()
    }
    drawPath(
        path,
        Brush.verticalGradient(
            colors = listOf(sand, sandDeep),
            startY = surfaceY,
            endY = midY,
        ),
    )
}

private fun DrawScope.drawLowerSand(
    elapsed: Float,
    centerX: Float,
    midY: Float,
    glassBottom: Float,
    bulbHalf: Float,
    neckHalf: Float,
    sand: Color,
    sandDeep: Color,
) {
    if (elapsed <= 0.001f) return
    val chamber = glassBottom - midY
    val surfaceY = lerp(glassBottom - chamber * 0.06f, midY + chamber * 0.08f, elapsed)
    val t = ((surfaceY - midY) / chamber).coerceIn(0f, 1f)
    val half = lerp(neckHalf, bulbHalf, t)
    val mound = chamber * 0.05f * elapsed
    val path = Path().apply {
        moveTo(centerX - neckHalf, midY)
        lineTo(centerX + neckHalf, midY)
        lineTo(centerX + half, surfaceY)
        quadraticTo(centerX, surfaceY - mound, centerX - half, surfaceY)
        close()
    }
    drawPath(
        path,
        Brush.verticalGradient(
            colors = listOf(sand, sandDeep),
            startY = midY,
            endY = glassBottom,
        ),
    )
}

private fun DrawScope.drawFallingSand(
    centerX: Float,
    midY: Float,
    glassBottom: Float,
    phase: Float,
    sand: Color,
) {
    val streamHeight = (glassBottom - midY) * 0.55f
    drawLine(
        color = sand.copy(alpha = 0.85f),
        start = Offset(centerX, midY),
        end = Offset(centerX, midY + streamHeight * 0.35f),
        strokeWidth = size.minDimension * 0.012f,
        cap = StrokeCap.Round,
    )
    repeat(7) { index ->
        val travel = (phase + index / 7f) % 1f
        val y = midY + streamHeight * travel
        val wobble = sin((travel * 8f + index) * Math.PI.toFloat()) * size.minDimension * 0.008f
        drawCircle(
            color = sand.copy(alpha = 0.9f - travel * 0.4f),
            radius = size.minDimension * (0.012f - travel * 0.004f),
            center = Offset(centerX + wobble, y),
        )
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction
