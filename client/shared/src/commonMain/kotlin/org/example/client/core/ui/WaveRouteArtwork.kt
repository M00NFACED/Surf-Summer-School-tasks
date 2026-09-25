package org.example.client.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave

private data class RouteArtworkColors(
    val skyTop: Color,
    val skyBottom: Color,
    val sun: Color,
    val ridgeFar: Color,
    val ridgeMid: Color,
    val ridgeNear: Color,
    val route: Color,
)

@Composable
private fun artworkColors(): RouteArtworkColors {
    val accentSoft = MaterialTheme.wave.accentSoft
    return if (isSystemInDarkTheme()) {
        RouteArtworkColors(
            skyTop = Color(0xFF0F1C19),
            skyBottom = Color(0xFF16302B),
            sun = Color(0xFF3E7A6E),
            ridgeFar = Color(0xFF24443D),
            ridgeMid = Color(0xFF1B342E),
            ridgeNear = Color(0xFF11221E),
            route = Color(0xFF8FD3C5),
        )
    } else {
        RouteArtworkColors(
            skyTop = accentSoft,
            skyBottom = Color(0xFFF4F9F7),
            sun = Color(0xFFB6DDD4),
            ridgeFar = Color(0xFFBFDCD4),
            ridgeMid = Color(0xFF8DC2B7),
            ridgeNear = Color(0xFF5C9A8D),
            route = Color(0xFFFFFFFF),
        )
    }
}

@Composable
fun WaveRouteArtwork(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val colors = artworkColors()
    Canvas(modifier = modifier) {
        drawRect(Brush.verticalGradient(listOf(colors.skyTop, colors.skyBottom)))
        drawCircle(
            color = colors.sun.copy(alpha = alpha),
            radius = size.minDimension * 0.12f,
            center = Offset(size.width * 0.78f, size.height * 0.26f),
        )
        drawRidge(colors.ridgeFar, listOf(0.50f to 0.34f, 0.66f to 0.22f, 0.84f to 0.32f, 1.04f to 0.20f))
        drawRidge(colors.ridgeMid, listOf(0.22f to 0.50f, 0.44f to 0.38f, 0.66f to 0.56f, 1.04f to 0.42f))
        drawRoute(colors.route, alpha)
        drawRidge(colors.ridgeNear, listOf(0.02f to 0.70f, 0.26f to 0.60f, 0.52f to 0.76f, 0.78f to 0.64f, 1.04f to 0.74f))
    }
}

private fun DrawScope.drawRidge(color: Color, peaks: List<Pair<Float, Float>>) {
    val path = Path()
    path.moveTo(0f, size.height)
    peaks.forEach { (xRatio, yRatio) -> path.lineTo(size.width * xRatio, size.height * yRatio) }
    path.lineTo(size.width, size.height)
    path.close()
    drawPath(path = path, color = color)
}

private fun DrawScope.drawRoute(color: Color, alpha: Float) {
    val start = Offset(size.width * 0.14f, size.height * 0.82f)
    val control = Offset(size.width * 0.44f, size.height * 0.52f)
    val end = Offset(size.width * 0.86f, size.height * 0.30f)
    val path = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(control.x, control.y, end.x, end.y)
    }
    drawPath(
        path = path,
        color = color.copy(alpha = alpha * 0.9f),
        style = Stroke(width = size.minDimension * 0.016f, cap = StrokeCap.Round),
    )
    drawCircle(color = color.copy(alpha = alpha * 0.3f), radius = size.minDimension * 0.07f, center = end)
    drawCircle(color = color.copy(alpha = alpha), radius = size.minDimension * 0.028f, center = end)
    drawCircle(color = color.copy(alpha = alpha), radius = size.minDimension * 0.02f, center = start)
}

@Composable
fun WaveSlotImage(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
) {
    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        WaveRouteArtwork(modifier = Modifier.fillMaxSize())
    }
}
