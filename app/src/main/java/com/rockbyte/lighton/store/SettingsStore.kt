package com.rockbyte.lighton.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// 仅测试用于清空数据
internal val Context.settingsDataStoreForTest: DataStore<Preferences> get() = settingsDataStore

// brightness = -1f 表示未设置（跟随系统）；dotSize = 0f 表示未设置（用最小尺寸）
data class Settings(val brightness: Float, val dotSize: Float)

interface SettingsStore {
    val settings: Flow<Settings>
    suspend fun save(brightness: Float, dotSize: Float)
}

class SettingsStorage(context: Context) : SettingsStore {
    private val dataStore = context.settingsDataStore

    override val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            brightness = prefs[KEY_BRIGHTNESS] ?: -1f,
            dotSize = prefs[KEY_DOT_SIZE] ?: 0f,
        )
    }

    override suspend fun save(brightness: Float, dotSize: Float) {
        dataStore.edit { prefs ->
            prefs[KEY_BRIGHTNESS] = brightness
            prefs[KEY_DOT_SIZE] = dotSize
        }
    }

    private companion object {
        val KEY_BRIGHTNESS = floatPreferencesKey("brightness")
        val KEY_DOT_SIZE = floatPreferencesKey("dot_size")
    }
}
