package com.rockbyte.lighton.page.screen

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import com.rockbyte.lighton.R

// 单通道滑条：轨道为该分量 0→满 的水平渐变（即 0..255 的渐变），拖动或点按时把触点 x 映射为 0..1 回调；
// 拇指 2dp 白边 + 填充当前分量颜色，随拖动实时变化
@Composable
private fun ChannelSlider(
    tag: String,
    value: Float,
    fullColor: Color,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val thumbDp = dimensionResource(R.dimen.lighton_slider_thumb_size)
    val thumbRadius = with(density) { thumbDp.toPx() / 2f }
    val borderPx = with(density) { dimensionResource(R.dimen.lighton_slider_thumb_border).toPx() }
    val trackHeight = with(density) { dimensionResource(R.dimen.lighton_slider_track_height).toPx() }
    // 拇指填充当前分量颜色：fullColor 的三个通道按 value 缩放（fullColor 本身只有当前通道非零）
    val thumbColor = Color(fullColor.red * value, fullColor.green * value, fullColor.blue * value)

    fun update(x: Float, width: Float) {
        onValueChange((x / width).coerceIn(0f, 1f))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbDp)
            .testTag(tag)
            // 绝对坐标映射（而非增量），跨 touchSlop 也不会丢值
            .pointerInput(Unit) {
                detectTapGestures { position -> update(position.x, size.width.toFloat()) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    update(change.position.x, size.width.toFloat())
                }
            }
            .drawBehind {
                val trackY = (size.height - trackHeight) / 2f
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(Color.Black, fullColor)),
                    topLeft = Offset(0f, trackY),
                    size = Size(size.width, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2f),
                )
                val thumbCenter = Offset(value * size.width, size.height / 2f)
                drawCircle(Color.White, radius = thumbRadius, center = thumbCenter)
                drawCircle(thumbColor, radius = thumbRadius - borderPx, center = thumbCenter)
            },
    )
}

// 取色模式的三条 RGB 滑条：调用方负责把它摆在圆点正下方
@Composable
fun RgbSlidersScreen(
    red: Float,
    green: Float,
    blue: Float,
    onRedChange: (Float) -> Unit,
    onGreenChange: (Float) -> Unit,
    onBlueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = dimensionResource(R.dimen.lighton_screen_gutter))
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChannelSlider(tag = "sliderR", value = red, fullColor = Color.Red, onValueChange = onRedChange)
        Spacer(Modifier.height(dimensionResource(R.dimen.lighton_related_gap)))
        ChannelSlider(tag = "sliderG", value = green, fullColor = Color.Green, onValueChange = onGreenChange)
        Spacer(Modifier.height(dimensionResource(R.dimen.lighton_related_gap)))
        ChannelSlider(tag = "sliderB", value = blue, fullColor = Color.Blue, onValueChange = onBlueChange)
    }
}
