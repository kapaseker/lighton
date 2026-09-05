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

// brightness = -1f 表示未设置（跟随系统）；dotSize = 0f 表示未设置（用最小尺寸）；
// red = -1f 表示未设置颜色（用默认前景色），green/blue 忽略；分量取值 0f..1f
data class Settings(
    val brightness: Float,
    val dotSize: Float,
    val red: Float = -1f,
    val green: Float = -1f,
    val blue: Float = -1f,
)

interface SettingsStore {
    val settings: Flow<Settings>
    suspend fun save(settings: Settings)
}

class SettingsStorage(context: Context) : SettingsStore {
    private val dataStore = context.settingsDataStore

    override val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            brightness = prefs[KEY_BRIGHTNESS] ?: -1f,
            dotSize = prefs[KEY_DOT_SIZE] ?: 0f,
            red = prefs[KEY_RED] ?: -1f,
            green = prefs[KEY_GREEN] ?: -1f,
            blue = prefs[KEY_BLUE] ?: -1f,
        )
    }

    override suspend fun save(settings: Settings) {
        dataStore.edit { prefs ->
            prefs[KEY_BRIGHTNESS] = settings.brightness
            prefs[KEY_DOT_SIZE] = settings.dotSize
            prefs[KEY_RED] = settings.red
            prefs[KEY_GREEN] = settings.green
            prefs[KEY_BLUE] = settings.blue
        }
    }

    private companion object {
        val KEY_BRIGHTNESS = floatPreferencesKey("brightness")
        val KEY_DOT_SIZE = floatPreferencesKey("dot_size")
        val KEY_RED = floatPreferencesKey("color_r")
        val KEY_GREEN = floatPreferencesKey("color_g")
        val KEY_BLUE = floatPreferencesKey("color_b")
    }
}
