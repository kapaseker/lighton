package com.rockbyte.lighton.repo

import com.rockbyte.lighton.store.Settings
import com.rockbyte.lighton.store.SettingsStore
import kotlinx.coroutines.flow.Flow

interface SettingsRepo {
    val settings: Flow<Settings>
    suspend fun save(brightness: Float, dotSize: Float, hue: Float)
}

class SettingsRepository(private val store: SettingsStore) : SettingsRepo {
    override val settings: Flow<Settings> = store.settings
    override suspend fun save(brightness: Float, dotSize: Float, hue: Float) =
        store.save(brightness, dotSize, hue)
}
