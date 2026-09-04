package com.rockbyte.lighton.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rockbyte.lighton.repo.SettingsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// brightness = -1f 表示未设置（跟随系统）；dotSize = 0f 表示未设置（用最小尺寸）
data class HomeUiState(val brightness: Float = -1f, val dotSize: Float = 0f)

class HomeViewModel(private val repo: SettingsRepo) : ViewModel() {

    // explicit backing field：field 持有 MutableStateFlow，类内按可变类型访问，对外只暴露只读 StateFlow
    val uiState: StateFlow<HomeUiState>
        field = MutableStateFlow(HomeUiState())

    init {
        viewModelScope.launch {
            repo.settings.collect { s -> uiState.value = HomeUiState(s.brightness, s.dotSize) }
        }
    }

    fun onBrightnessChange(brightness: Float) = uiState.update { it.copy(brightness = brightness) }

    fun onDotSizeChange(dotSize: Float) = uiState.update { it.copy(dotSize = dotSize) }

    // 抬手时持久化当前值
    fun onDragEnd() {
        val state = uiState.value
        viewModelScope.launch { repo.save(state.brightness, state.dotSize) }
    }
}
