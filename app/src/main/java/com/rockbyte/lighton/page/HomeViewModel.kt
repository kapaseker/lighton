package com.rockbyte.lighton.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rockbyte.lighton.repo.SettingsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// brightness = -1f 表示未设置（跟随系统）；dotSize = 0f 表示未设置（用最小尺寸）；
// red = -1f 表示未设置颜色（用默认前景色），green/blue 忽略；颜色分量取值 0f..1f
data class HomeUiState(
    val brightness: Float = -1f,
    val dotSize: Float = 0f,
    val red: Float = -1f,
    val green: Float = -1f,
    val blue: Float = -1f,
)

class HomeViewModel(private val repo: SettingsRepo) : ViewModel() {

    // explicit backing field：field 持有 MutableStateFlow，类内按可变类型访问，对外只暴露只读 StateFlow
    val uiState: StateFlow<HomeUiState>
        field = MutableStateFlow(HomeUiState())

    init {
        viewModelScope.launch {
            repo.settings.collect { s ->
                uiState.value = HomeUiState(s.brightness, s.dotSize, s.red, s.green, s.blue)
            }
        }
    }

    fun onBrightnessChange(brightness: Float) = uiState.update { it.copy(brightness = brightness) }

    fun onDotSizeChange(dotSize: Float) = uiState.update { it.copy(dotSize = dotSize) }

    fun onRedChange(red: Float) = uiState.update { it.copy(red = red) }

    fun onGreenChange(green: Float) = uiState.update { it.copy(green = green) }

    fun onBlueChange(blue: Float) = uiState.update { it.copy(blue = blue) }

    // 进入取色模式时调用：未设置过颜色则初始化为白色，保证滑条有确定初值
    fun initColorIfUnset() = uiState.update {
        if (it.red < 0f) it.copy(red = 1f, green = 1f, blue = 1f) else it
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
            )
        }
    }
}
