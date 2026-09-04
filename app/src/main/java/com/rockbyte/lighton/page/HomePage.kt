package com.rockbyte.lighton.page

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rockbyte.lighton.R
import com.rockbyte.lighton.page.screen.HueRingScreen
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import org.koin.androidx.compose.koinViewModel

// 九宫格手势：左列竖滑调亮度，底行横滑调圆点大小；左下重叠格按滑动方向决定
private enum class DragMode { None, Undecided, Brightness, DotSize }

@Composable
fun HomePage(viewModel: HomeViewModel = koinViewModel()) {
    val density = LocalDensity.current
    val minDotSize = with(density) { dimensionResource(R.dimen.lighton_min_dot_size).toPx() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dotSize = if (state.dotSize > 0f) state.dotSize else minDotSize
    val window = LocalActivity.current!!.window

    // 取色模式是短生命周期交互状态，页面本地持有；色相本身由 ViewModel 持久化
    var colorMode by rememberSaveable { mutableStateOf(false) }
    var rootSize by androidx.compose.runtime.remember { mutableStateOf(IntSize.Zero) }
    // 色相环直径 = 屏幕短边的一半
    val ringDiameterPx = min(rootSize.width, rootSize.height) / 2f
    // 取色模式下圆点尺寸动画过渡到色相环直径，退出后恢复原设置大小
    val dotTargetPx = if (colorMode && ringDiameterPx > 0f) ringDiameterPx else dotSize
    val animatedDotSizePx by animateFloatAsState(
        targetValue = dotTargetPx,
        // 进入取色模式用 tween 平滑放大到环直径；拖动调大小与退出恢复用 snap 立即生效，
        // 避免连续拖动时动画重启导致圆点"追赶"手指
        animationSpec = if (colorMode) tween(300, easing = FastOutSlowInEasing) else snap(),
        label = "dotSize",
    )

    // pointerInput 内读取最新值，避免闭包捕获过期状态
    val currentDotSize by rememberUpdatedState(dotSize)
    val currentColorMode by rememberUpdatedState(colorMode)

    // 亮度统一由此写入窗口：启动时恢复已保存值，拖动时跟随状态
    LaunchedEffect(state.brightness) {
        if (state.brightness >= 0f) {
            window.attributes = window.attributes.apply { screenBrightness = state.brightness }
        }
    }

    Box(
        Modifier
            .testTag("homeRoot")
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { rootSize = it }
            .pointerInput(minDotSize) {
                var mode = DragMode.None
                // 尺寸在协程内累计：跨 slop 的首个增量与后续事件可能同帧到达，
                // 此时 state 尚未重组，读 currentDotSize 会覆盖丢失增量
                var dragDotSize = 0f
                detectDragGestures(
                    onDragStart = { start ->
                        mode = DragMode.None
                        // 取色模式下网格拖动让位给色相环手势
                        if (!currentColorMode) {
                            val inLeftColumn = start.x < size.width / 3f
                            val inBottomRow = start.y > size.height * 2f / 3f
                            mode = when {
                                inLeftColumn && inBottomRow -> DragMode.Undecided
                                inLeftColumn -> DragMode.Brightness
                                inBottomRow -> DragMode.DotSize
                                else -> DragMode.None
                            }
                        }
                        dragDotSize = currentDotSize
                    },
                    onDragEnd = {
                        mode = DragMode.None
                        viewModel.save()
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
                            dragDotSize = (dragDotSize + dragAmount.x)
                                .coerceIn(minDotSize, maxDotSize)
                            viewModel.onDotSizeChange(dragDotSize)
                        }

                        else -> Unit
                    }
                }
            }
            .pointerInput(minDotSize) {
                // 点击顶部中间格：进入/退出取色模式；退出时持久化色相
                detectTapGestures { position ->
                    val inTopMiddle = position.x >= size.width / 3f &&
                        position.x < size.width * 2f / 3f &&
                        position.y < size.height / 3f
                    if (inTopMiddle) {
                        if (currentColorMode) viewModel.save()
                        colorMode = !colorMode
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // 圆点即灯：已设置色相时显示该色，未设置时用默认前景色
        val dotColor = if (state.hue >= 0f) {
            Color.hsv(state.hue, 1f, 1f)
        } else {
            colorResource(R.color.lighton_foreground)
        }
        // requiredSize：允许圆点超出父布局约束，保证放到最大时是正圆且铺满全屏
        Box(
            Modifier
                .testTag("dot")
                .requiredSize(with(density) { animatedDotSizePx.toDp() })
                .clip(CircleShape)
                .background(dotColor),
        )

        if (colorMode && ringDiameterPx > 0f) {
            HueRingScreen(
                hue = state.hue,
                diameterPx = ringDiameterPx,
                onHueChange = viewModel::onHueChange,
            )
        }
    }
}
