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

@Composable
private fun artworkPalette(style: ArtworkStyle): ArtworkPalette =
    if (isSystemInDarkTheme()) darkArtworkPalette(style) else lightArtworkPalette(style, MaterialTheme.wave.accentSoft)

@Composable
fun WaveRouteArtwork(
    style: ArtworkStyle,
    seed: Int,
    modifier: Modifier = Modifier,
) {
    val palette = artworkPalette(style)
    Canvas(modifier = modifier) {
        drawRect(Brush.verticalGradient(listOf(palette.skyTop, palette.skyBottom)))
        drawSun(palette)
        drawRidge(palette.ridgeFar, ridgeProfile(style, seed, layer = 0))
        drawRidge(palette.ridgeMid, ridgeProfile(style, seed, layer = 1))
        drawRoute(palette, routeGeometry(style, seed), style)
        drawRidge(palette.ridgeNear, ridgeProfile(style, seed, layer = 2))
    }
}

private fun DrawScope.drawSun(palette: ArtworkPalette) {
    drawCircle(
        color = palette.sun,
        radius = size.minDimension * 0.12f,
        center = Offset(size.width * 0.78f, size.height * 0.24f),
    )
    drawCircle(
        color = palette.sun.copy(alpha = 0.25f),
        radius = size.minDimension * 0.2f,
        center = Offset(size.width * 0.78f, size.height * 0.24f),
    )
}

private fun DrawScope.drawRidge(color: Color, peaks: List<ArtworkPoint>) {
    val path = Path()
    path.moveTo(0f, size.height)
    peaks.forEach { path.lineTo(size.width * it.x, size.height * it.y) }
    path.lineTo(size.width, size.height)
    path.close()
    drawPath(path = path, color = color)
}

private fun DrawScope.drawRoute(
    palette: ArtworkPalette,
    geometry: RouteGeometry,
    style: ArtworkStyle,
) {
    val start = Offset(size.width * geometry.start.x, size.height * geometry.start.y)
    val control = Offset(size.width * geometry.control.x, size.height * geometry.control.y)
    val end = Offset(size.width * geometry.end.x, size.height * geometry.end.y)
    val path = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(control.x, control.y, end.x, end.y)
    }
    drawPath(
        path = path,
        color = palette.route.copy(alpha = 0.9f),
        style = Stroke(width = size.minDimension * 0.016f, cap = StrokeCap.Round),
    )
    if (style == ArtworkStyle.EVENING_SPIRES) {
        drawPath(
            path = Path().apply {
                moveTo(end.x, end.y)
                lineTo(end.x, end.y - size.height * 0.22f)
            },
            color = palette.route.copy(alpha = 0.75f),
            style = Stroke(width = size.minDimension * 0.008f, cap = StrokeCap.Round),
        )
    }
    drawCircle(color = palette.route.copy(alpha = 0.3f), radius = size.minDimension * 0.07f, center = end)
    drawCircle(color = palette.route, radius = size.minDimension * 0.026f, center = end)
    drawCircle(color = palette.route, radius = size.minDimension * 0.02f, center = start)
}

@Composable
fun WaveSlotImage(
    style: ArtworkStyle,
    seed: Int,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
) {
    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        WaveRouteArtwork(style = style, seed = seed, modifier = Modifier.fillMaxSize())
    }
}
