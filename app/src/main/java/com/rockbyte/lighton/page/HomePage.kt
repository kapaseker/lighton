package com.rockbyte.lighton.page

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rockbyte.lighton.R
import kotlin.math.abs
import kotlin.math.hypot
import org.koin.androidx.compose.koinViewModel

// 九宫格手势：左列竖滑调亮度，底行横滑调圆点大小；左下重叠格按滑动方向决定
private enum class DragMode { None, Undecided, Brightness, DotSize }

@Composable
fun HomePage(viewModel: HomeViewModel = koinViewModel()) {
    val density = LocalDensity.current
    val minDotSize = with(density) { dimensionResource(R.dimen.lighton_min_dot_size).toPx() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dotSize = if (state.dotSize > 0f) state.dotSize else minDotSize
    // pointerInput 内读取最新值，避免闭包捕获过期状态
    val currentDotSize by rememberUpdatedState(dotSize)
    val window = LocalActivity.current!!.window

    // 亮度统一由此写入窗口：启动时恢复已保存值，拖动时跟随状态
    LaunchedEffect(state.brightness) {
        if (state.brightness >= 0f) {
            window.attributes = window.attributes.apply { screenBrightness = state.brightness }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(minDotSize) {
                var mode = DragMode.None
                detectDragGestures(
                    onDragStart = { start ->
                        val inLeftColumn = start.x < size.width / 3f
                        val inBottomRow = start.y > size.height * 2f / 3f
                        mode = when {
                            inLeftColumn && inBottomRow -> DragMode.Undecided
                            inLeftColumn -> DragMode.Brightness
                            inBottomRow -> DragMode.DotSize
                            else -> DragMode.None
                        }
                    },
                    onDragEnd = {
                        mode = DragMode.None
                        viewModel.onDragEnd()
                    },
                    onDragCancel = { mode = DragMode.None },
                ) { change, dragAmount ->
                    change.consume()
                    if (mode == DragMode.Undecided) {
                        mode = if (abs(dragAmount.x) > abs(dragAmount.y)) {
                            DragMode.DotSize
                        } else {
                            DragMode.Brightness
                        }
                    }
                    when (mode) {
                        DragMode.Brightness -> {
                            val current = window.attributes.screenBrightness
                                .let { if (it < 0f) 0.5f else it }
                            viewModel.onBrightnessChange(
                                (current - dragAmount.y / size.height).coerceIn(0f, 1f),
                            )
                        }

                        DragMode.DotSize -> {
                            val maxDotSize = hypot(size.width.toFloat(), size.height.toFloat())
                            viewModel.onDotSizeChange(
                                (currentDotSize + dragAmount.x).coerceIn(minDotSize, maxDotSize),
                            )
                        }

                        else -> Unit
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // requiredSize：允许圆点超出父布局约束，保证放到最大时是正圆且铺满全屏
        Box(
            Modifier
                .testTag("dot")
                .requiredSize(with(density) { dotSize.toDp() })
                .clip(CircleShape)
                .background(colorResource(R.color.lighton_foreground)),
        )
    }
}
