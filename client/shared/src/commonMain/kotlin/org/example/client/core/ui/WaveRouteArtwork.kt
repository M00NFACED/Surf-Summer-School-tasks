package org.example.client.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.client.core.theme.wave

val ArtworkBlockHeight: Dp = 120.dp
val ArtworkCornerRadius: Dp = 16.dp

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
        drawRect(Brush.verticalGradient(palette.skyStops))
        when (style) {
            ArtworkStyle.MORNING_BOULDERS -> drawBoulders(palette, bouldersGeometry(seed))
            ArtworkStyle.EVENING_SPIRES -> drawSpires(palette, spiresGeometry(seed))
        }
    }
}

private fun DrawScope.drawBoulders(palette: ArtworkPalette, geometry: BouldersGeometry) {
    drawCircle(
        color = palette.sun.copy(alpha = 0.55f),
        radius = size.minDimension * geometry.sunRadius,
        center = geometry.sun.toOffset(size),
    )
    geometry.layers.forEach { layer ->
        drawPath(
            path = smoothSilhouette(layer.humps, layer.baseY, size),
            color = palette.rock.copy(alpha = layer.alpha),
        )
    }
    geometry.holds.forEach { hold ->
        drawCircle(
            color = palette.route,
            radius = size.minDimension * 0.018f,
            center = hold.toOffset(size),
        )
    }
    val arcPath = Path()
    val first = geometry.holds.first()
    arcPath.moveTo(size.width * first.x, size.height * first.y)
    geometry.holds.drop(1).forEach { arcPath.lineTo(size.width * it.x, size.height * it.y) }
    drawPath(
        path = arcPath,
        color = palette.route.copy(alpha = 0.28f),
        style = Stroke(width = size.minDimension * 0.008f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawSpires(palette: ArtworkPalette, geometry: SpiresGeometry) {
    drawCircle(
        color = palette.sun,
        radius = size.minDimension * geometry.sunRadius,
        center = geometry.sun.toOffset(size),
    )
    drawPath(
        path = filledRidge(geometry.far, geometry.baseY, size),
        color = palette.rock.copy(alpha = 0.28f),
    )
    drawPath(
        path = filledRidge(geometry.mid, geometry.baseY, size),
        color = palette.rock.copy(alpha = 0.5f),
    )
    drawPath(
        path = filledRidge(geometry.near, geometry.baseY, size),
        color = palette.rock.copy(alpha = 0.88f),
    )
    val rope = Path().apply {
        val start = geometry.rope.first()
        val end = geometry.rope.last()
        moveTo(size.width * start.x, size.height * start.y)
        lineTo(size.width * end.x, size.height * end.y)
    }
    drawPath(
        path = rope,
        color = palette.route,
        style = Stroke(
            width = size.minDimension * 0.016f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(geometry.ropeDashes),
        ),
    )
}

private fun smoothSilhouette(
    humps: List<ArtworkPoint>,
    baseY: Float,
    size: androidx.compose.ui.geometry.Size,
): Path {
    val points = humps.map { ArtworkPoint(it.x, it.y) }
    val path = Path()
    val base = size.height * baseY
    path.moveTo(0f, base)
    path.lineTo(0f, size.height * points.first().y)
    for (index in 0 until points.size - 1) {
        val current = points[index]
        val next = points[index + 1]
        path.quadraticTo(
            size.width * current.x,
            size.height * current.y,
            size.width * (current.x + next.x) / 2f,
            size.height * (current.y + next.y) / 2f,
        )
    }
    val last = points.last()
    path.lineTo(size.width * last.x, size.height * last.y)
    path.lineTo(size.width, base)
    path.close()
    return path
}

private fun filledRidge(
    peaks: List<ArtworkPoint>,
    baseY: Float,
    size: androidx.compose.ui.geometry.Size,
): Path {
    val path = Path()
    path.moveTo(0f, size.height * baseY)
    peaks.forEach { path.lineTo(size.width * it.x, size.height * it.y) }
    path.lineTo(size.width, size.height * baseY)
    path.close()
    return path
}

private fun ArtworkPoint.toOffset(size: androidx.compose.ui.geometry.Size) =
    Offset(size.width * x, size.height * y)

@Composable
fun WaveSlotImage(
    style: ArtworkStyle,
    seed: Int,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = ArtworkCornerRadius,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ArtworkBlockHeight)
            .clip(RoundedCornerShape(cornerRadius)),
    ) {
        WaveRouteArtwork(style = style, seed = seed, modifier = Modifier.fillMaxSize())
    }
}
