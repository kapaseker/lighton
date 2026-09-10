package com.rockbyte.lighton.page

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rockbyte.lighton.R
import com.rockbyte.lighton.page.screen.EyeCareColorsScreen
import com.rockbyte.lighton.page.screen.RgbSlidersScreen
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
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

    // 取色模式是短生命周期交互状态，页面本地持有；颜色本身由 ViewModel 持久化
    var colorMode by rememberSaveable { mutableStateOf(false) }
    // 进出取色模式的尺寸切换用 spring 平滑过渡；拖动调大小用 snap（避免动画追赶手指）
    var smoothDotTransition by remember { mutableStateOf(false) }
    var rootSize by androidx.compose.runtime.remember { mutableStateOf(IntSize.Zero) }
    // 取色模式下圆点尺寸 = 屏幕短边的一半
    val colorDotPx = min(rootSize.width, rootSize.height) / 2f
    // 取色模式下圆点以 spring 动画过渡到取色尺寸，退出后平滑恢复原设置大小
    val dotTargetPx = if (colorMode && colorDotPx > 0f) colorDotPx else dotSize
    val animatedDotSizePx by animateFloatAsState(
        targetValue = dotTargetPx,
        animationSpec = if (smoothDotTransition) {
            spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)
        } else {
            snap()
        },
        label = "dotSize",
    )
    val gutterPx = with(density) { dimensionResource(R.dimen.lighton_screen_gutter).toPx() }
    // 滑条组高度：用于把组顶边锚定在圆点下缘（align Center 定位的是组中心）
    var slidersHeightPx by androidx.compose.runtime.remember { mutableStateOf(0f) }
    // 护眼色板高度：同上，锚定在圆点上缘
    var eyeCareHeightPx by remember { mutableStateOf(0f) }

    // pointerInput 内读取最新值，避免闭包捕获过期状态
    val currentDotSize by rememberUpdatedState(dotSize)
    val currentColorMode by rememberUpdatedState(colorMode)

    // 亮度统一由此写入窗口：启动时恢复已保存值，拖动时跟随状态
    LaunchedEffect(state.brightness) {
        if (state.brightness >= 0f) {
            window.attributes = window.attributes.apply { screenBrightness = state.brightness }
        }
    }

    // 系统返回与右上角取消按钮一致：恢复进入前颜色并退出取色模式
    BackHandler(enabled = colorMode) {
        viewModel.cancelColorEditing()
        smoothDotTransition = true
        colorMode = false
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
                        // 拖动调大小必须即时跟手
                        smoothDotTransition = false
                        // 取色模式下网格拖动让位给取色滑条手势
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
                // 点击九宫格中间格（圆点区域）进入取色模式；退出由右上角按钮负责
                detectTapGestures { position ->
                    val inCenter = position.x >= size.width / 3f &&
                        position.x < size.width * 2f / 3f &&
                        position.y >= size.height / 3f &&
                        position.y < size.height * 2f / 3f
                    if (!currentColorMode && inCenter) {
                        // 进入时快照颜色（供取消恢复）并保证滑条有确定初值
                        viewModel.beginColorEditing()
                        smoothDotTransition = true
                        colorMode = true
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // 圆点即灯：已设置颜色时显示该色，未设置时用默认前景色
        val dotColor = if (state.red >= 0f) {
            Color(state.red, state.green, state.blue)
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

        if (colorMode) {
            // 护眼色板挂在圆点正上方，offset 跟随 spring 动画的圆点半径联动（与下方滑条镜像）
            EyeCareColorsScreen(
                colors = EyeCareColors,
                selectedIndex = state.eyeCareIndex,
                onSelect = viewModel::onEyeCareColorSelect,
                modifier = Modifier
                    .align(Alignment.Center)
                    .onSizeChanged { eyeCareHeightPx = it.height.toFloat() }
                    .offset {
                        IntOffset(
                            0,
                            -(animatedDotSizePx / 2f + gutterPx + eyeCareHeightPx / 2f).roundToInt(),
                        )
                    },
            )
        }

        if (colorMode) {
            // 滑条组挂在圆点正下方，offset 跟随 spring 动画的圆点半径联动
            // 未设置颜色时先按白色展示，等 initColorIfUnset 落到 uiState
            RgbSlidersScreen(
                red = if (state.red < 0f) 1f else state.red.coerceIn(0f, 1f),
                green = if (state.green < 0f) 1f else state.green.coerceIn(0f, 1f),
                blue = if (state.blue < 0f) 1f else state.blue.coerceIn(0f, 1f),
                onRedChange = viewModel::onRedChange,
                onGreenChange = viewModel::onGreenChange,
                onBlueChange = viewModel::onBlueChange,
                modifier = Modifier
                    .align(Alignment.Center)
                    .onSizeChanged { slidersHeightPx = it.height.toFloat() }
                    .offset {
                        IntOffset(
                            0,
                            (animatedDotSizePx / 2f + gutterPx + slidersHeightPx / 2f).roundToInt(),
                        )
                    },
            )
        }

        // 取色模式操作区：确认（保存）/ 取消（恢复进入前颜色）双浮层按钮，随取色模式淡入淡出；
        // 取消按钮保持在最角落，延续原关闭按钮的位置记忆
        AnimatedVisibility(
            visible = colorMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(dimensionResource(R.dimen.lighton_screen_gutter)),
        ) {
            Row {
                OverlayIconButton(
                    tag = "saveColorMode",
                    iconRes = R.drawable.ic_check,
                    contentDescriptionRes = R.string.save,
                    onClick = {
                        viewModel.confirmColorEditing()
                        smoothDotTransition = true
                        colorMode = false
                    },
                )
                Spacer(Modifier.width(dimensionResource(R.dimen.lighton_item_gap)))
                OverlayIconButton(
                    tag = "cancelColorMode",
                    iconRes = R.drawable.ic_close,
                    contentDescriptionRes = R.string.cancel,
                    onClick = {
                        viewModel.cancelColorEditing()
                        smoothDotTransition = true
                        colorMode = false
                    },
                )
            }
        }
    }
}

// 浮层图标按钮：48dp 触控区带半透明圆形底（点按区可见），24dp 图标弱化白降低夜间刺眼；按压时图标变暗作为反馈
@Composable
private fun OverlayIconButton(
    tag: String,
    iconRes: Int,
    contentDescriptionRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = modifier
            .testTag(tag)
            .size(dimensionResource(R.dimen.lighton_touch_target))
            .clip(CircleShape)
            .background(colorResource(R.color.lighton_overlay_scrim))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = stringResource(contentDescriptionRes),
            colorFilter = ColorFilter.tint(
                colorResource(R.color.lighton_overlay_icon).copy(alpha = if (pressed) 0.6f else 1f),
            ),
            modifier = Modifier.size(dimensionResource(R.dimen.lighton_app_bar_icon_size)),
        )
    }
}
