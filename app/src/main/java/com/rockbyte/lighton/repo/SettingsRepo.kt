package com.rockbyte.lighton.repo

import com.rockbyte.lighton.store.Settings
import com.rockbyte.lighton.store.SettingsStore
import kotlinx.coroutines.flow.Flow

interface SettingsRepo {
    val settings: Flow<Settings>

    // 原语签名：让上层（ViewModel）无需依赖 store 层的 Settings 类型；-1f 为未设置哨兵
    suspend fun save(
        brightness: Float,
        dotSize: Float,
        red: Float = -1f,
        green: Float = -1f,
        blue: Float = -1f,
    )
}

class SettingsRepository(private val store: SettingsStore) : SettingsRepo {
    override val settings: Flow<Settings> = store.settings

    override suspend fun save(
        brightness: Float,
        dotSize: Float,
        red: Float,
        green: Float,
        blue: Float,
    ) = store.save(Settings(brightness, dotSize, red, green, blue))
}
