package com.rockbyte.lighton.page.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import com.rockbyte.lighton.R

// 每行护眼色个数，放不下的自动换行
private const val ROW_CAPACITY = 6

// 取色模式的护眼色预设色板：调用方负责把它摆在圆点上方。
// 色圆视觉与 RGB 滑条拇指一致（白边圆形）但更大；选中项外加白色描边环
@Composable
fun EyeCareColorsScreen(
    colors: List<Color>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        colors.withIndex().chunked(ROW_CAPACITY).forEach { row ->
            Row {
                row.forEach { (index, color) ->
                    EyeCareSwatch(
                        index = index,
                        color = color,
                        selected = index == selectedIndex,
                        onSelect = onSelect,
                    )
                }
            }
        }
    }
}

// 单个护眼色圆：48dp 触控区承载 40dp 色圆，相邻触控区的留白即视觉间距
@Composable
private fun EyeCareSwatch(
    index: Int,
    color: Color,
    selected: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val touchTarget = dimensionResource(R.dimen.lighton_touch_target)
    val swatchSize = dimensionResource(R.dimen.lighton_sample_swatch_size)
    val borderPx = with(density) { dimensionResource(R.dimen.lighton_slider_thumb_border).toPx() }

    Box(
        modifier = modifier
            .size(touchTarget)
            .testTag("eyeCareSwatch$index")
            .pointerInput(Unit) { detectTapGestures { onSelect(index) } }
            .drawBehind {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = swatchSize.toPx() / 2f
                // 与滑条拇指同款白边：白底圆 + 内缩 borderPx 的颜色圆
                drawCircle(Color.White, radius, center)
                drawCircle(color, radius - borderPx, center)
                if (selected) {
                    // 选中态：隔 borderPx 画白色描边环，外沿恰好贴住触控区边界
                    drawCircle(
                        color = Color.White,
                        radius = radius + borderPx * 1.5f,
                        center = center,
                        style = Stroke(borderPx),
                    )
                }
            },
    )
}
