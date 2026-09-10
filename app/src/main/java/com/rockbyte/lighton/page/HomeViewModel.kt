package com.rockbyte.lighton.page

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rockbyte.lighton.repo.SettingsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// brightness = -1f 表示未设置（跟随系统）；dotSize = 0f 表示未设置（用最小尺寸）；
// red = -1f 表示未设置颜色（用默认前景色），green/blue 忽略；颜色分量取值 0f..1f；
// eyeCareIndex = -1 表示未选中护眼色，否则为 EyeCareColors 下标
data class HomeUiState(
    val brightness: Float = -1f,
    val dotSize: Float = 0f,
    val red: Float = -1f,
    val green: Float = -1f,
    val blue: Float = -1f,
    val eyeCareIndex: Int = -1,
)

// 内置护眼色预设：取色模式下悬于圆点上方，点选即生效
val EyeCareColors = listOf(
    Color(0xFFC7EDCC), // 绿豆沙
    Color(0xFFFFFFFF), // 银河白
    Color(0xFFFAF9DE), // 杏仁黄
    Color(0xFFFFF2E2), // 秋叶褐
    Color(0xFFFDE6E0), // 胭脂红
    Color(0xFFDCE2F1), // 海天蓝
    Color(0xFFE9EBFE), // 葛巾紫
    Color(0xFFEAEAEF), // 极光灰
    Color(0xFFE3EDCD), // 青草绿
    Color(0xFFCCE8CF), // 电脑管家
    Color(0xFF6E7B6C), // WPS护眼色
)

class HomeViewModel(private val repo: SettingsRepo) : ViewModel() {

    // explicit backing field：field 持有 MutableStateFlow，类内按可变类型访问，对外只暴露只读 StateFlow
    val uiState: StateFlow<HomeUiState>
        field = MutableStateFlow(HomeUiState())

    init {
        viewModelScope.launch {
            repo.settings.collect { s ->
                uiState.value = HomeUiState(s.brightness, s.dotSize, s.red, s.green, s.blue, s.eyeCareIndex)
            }
        }
    }

    fun onBrightnessChange(brightness: Float) = uiState.update { it.copy(brightness = brightness) }

    fun onDotSizeChange(dotSize: Float) = uiState.update { it.copy(dotSize = dotSize) }

    // 手动调 RGB 视为放弃护眼色预设，选中态随之取消
    fun onRedChange(red: Float) = uiState.update { it.copy(red = red, eyeCareIndex = -1) }

    fun onGreenChange(green: Float) = uiState.update { it.copy(green = green, eyeCareIndex = -1) }

    fun onBlueChange(blue: Float) = uiState.update { it.copy(blue = blue, eyeCareIndex = -1) }

    // 点选护眼色：直接变更当前颜色；持久化统一由确认（confirmColorEditing）触发
    fun onEyeCareColorSelect(index: Int) {
        val color = EyeCareColors.getOrNull(index) ?: return
        uiState.update {
            it.copy(red = color.red, green = color.green, blue = color.blue, eyeCareIndex = index)
        }
    }

    // 进入取色模式时调用：未设置过颜色则初始化为白色，保证滑条有确定初值
    fun initColorIfUnset() = uiState.update {
        if (it.red < 0f) it.copy(red = 1f, green = 1f, blue = 1f) else it
    }

    // 取色模式编辑快照：进入时备份颜色，取消时恢复；亮度/圆点尺寸在取色模式下不可编辑，不参与
    private var colorSnapshot: HomeUiState? = null

    // 进入取色模式：快照当前颜色并保证滑条有确定初值
    fun beginColorEditing() {
        colorSnapshot = uiState.value
        initColorIfUnset()
    }

    // 确认退出取色模式：丢弃快照并持久化当前颜色
    fun confirmColorEditing() {
        colorSnapshot = null
        save()
    }

    // 取消退出取色模式：恢复进入前颜色，不持久化（编辑期间无写入，store 仍是进入前的值）
    fun cancelColorEditing() {
        val snap = colorSnapshot
        if (snap != null) {
            uiState.update {
                it.copy(red = snap.red, green = snap.green, blue = snap.blue, eyeCareIndex = snap.eyeCareIndex)
            }
        }
        colorSnapshot = null
    }

    // 抬手或退出取色模式时持久化当前值
    fun save() {
        val state = uiState.value
        viewModelScope.launch {
            repo.save(
                brightness = state.brightness,
                dotSize = state.dotSize,
                red = state.red,
                green = state.green,
                blue = state.blue,
                eyeCareIndex = state.eyeCareIndex,
            )
        }
    }
}
