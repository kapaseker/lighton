package com.rockbyte.lighton.page.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.IntOffset
import com.rockbyte.lighton.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// 色相环渐变：红起点顺时针每 60° 一停，与 HSV 色相 0°-360° 对应
private val HueWheel =
    listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)

// 触点相对圆心的角度换算为色相值（0° 在 3 点钟方向，顺时针增加，与 sweepGradient 一致）
private fun hueAt(center: Offset, position: Offset): Float {
    val degrees = Math.toDegrees(atan2(position.y - center.y, position.x - center.x).toDouble())
    return (((degrees % 360.0) + 360.0) % 360.0).toFloat()
}

// 取色模式的色相圆环：铺满父容器居中绘制，直径由调用方给定（屏幕短边一半）；拖动屏幕任意处改变色相
@Composable
fun HueRingScreen(
    hue: Float,
    diameterPx: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val strokeWidth = dimensionResource(R.dimen.lighton_color_ring_stroke)
    val indicatorSize = dimensionResource(R.dimen.lighton_color_indicator_size)
    val indicatorBorder = dimensionResource(R.dimen.lighton_color_indicator_border)
    val strokePx = with(density) { strokeWidth.toPx() }
    val indicatorSizePx = with(density) { indicatorSize.toPx() }
    val ringRadius = diameterPx / 2f
    // 指示器沿环带中线走位
    val indicatorOrbitRadius = ringRadius - strokePx / 2f
    val indicatorAngle = hue.coerceAtLeast(0f)
    println("HUE DIAG hue=$hue angle=$indicatorAngle orbit=$indicatorOrbitRadius stroke=$strokePx ind=$indicatorSizePx diam=$diameterPx")

    Box(
        modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                val center = Offset(size.width / 2f, size.height / 2f)
                detectDragGestures(
                    onDragStart = { start -> onHueChange(hueAt(center, start)) },
                ) { change, _ ->
                    change.consume()
                    onHueChange(hueAt(center, change.position))
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // 圆环容器以左上角为原点，指示器和 Canvas 都相对它定位
        Box(
            Modifier
                .testTag("hueRing")
                .requiredSize(with(density) { diameterPx.toDp() }),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                // Stroke 以半径为中心线：内收半个线宽，让环带外缘恰好贴合给定直径
                drawCircle(
                    brush = Brush.sweepGradient(HueWheel),
                    radius = ringRadius - strokePx / 2f,
                    style = Stroke(width = strokePx),
                )
            }

            val angle = Math.toRadians(indicatorAngle.toDouble())
            val indicatorOffset = IntOffset(
                (ringRadius + indicatorOrbitRadius * cos(angle) - indicatorSizePx / 2f)
                    .roundToInt(),
                (ringRadius + indicatorOrbitRadius * sin(angle) - indicatorSizePx / 2f)
                    .roundToInt(),
            )
            Box(
                Modifier
                    // 用 layout 直接 place 指示器（offset 的 layer 放置不会反映到语义坐标）；
                    // testTag 必须在 layout 内侧，语义 bounds 才包含 place 偏移
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.place(indicatorOffset)
                        }
                    }
                    .testTag("hueIndicator")
                    .size(indicatorSize)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(indicatorBorder, Color.White, CircleShape),
            )
        }
    }
}
