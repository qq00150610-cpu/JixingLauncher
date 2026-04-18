package com.jixing.launcher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jixing_settings")

/**
 * 设置数据仓库 - 使用 DataStore 存储
 */
class SettingsRepository(private val context: Context) {

    // 主题设置
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[DARK_THEME_KEY] ?: true
        }

    // 音效设置
    val isSoundEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[SOUND_ENABLED_KEY] ?: true }

    // 导航语音设置
    val isNavVoiceEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[NAV_VOICE_ENABLED_KEY] ?: true }

    // 媒体音量
    val mediaVolume: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[MEDIA_VOLUME_KEY] ?: 50 }

    // 空调同步设置
    val isAcSyncEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[AC_SYNC_KEY] ?: true }

    // 温度单位 (true = Celsius, false = Fahrenheit)
    val isCelsiusUnit: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[TEMP_UNIT_KEY] ?: true }

    // 屏幕亮度
    val screenBrightness: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[BRIGHTNESS_KEY] ?: 80 }

    // 夜间模式
    val isNightMode: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[NIGHT_MODE_KEY] ?: false }

    // 悬浮球启用
    val isFloatingBallEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[FLOATING_BALL_KEY] ?: true }

    // 驾驶模式
    val isDrivingModeEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[DRIVING_MODE_KEY] ?: false }

    // 语音助手启用
    val isVoiceAssistantEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[VOICE_ASSISTANT_KEY] ?: true }

    // 保存设置
    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[DARK_THEME_KEY] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED_KEY] = enabled }
    }

    suspend fun setNavVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NAV_VOICE_ENABLED_KEY] = enabled }
    }

    suspend fun setMediaVolume(volume: Int) {
        context.dataStore.edit { it[MEDIA_VOLUME_KEY] = volume.coerceIn(0, 100) }
    }

    suspend fun setAcSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AC_SYNC_KEY] = enabled }
    }

    suspend fun setTempUnit(isCelsius: Boolean) {
        context.dataStore.edit { it[TEMP_UNIT_KEY] = isCelsius }
    }

    suspend fun setScreenBrightness(brightness: Int) {
        context.dataStore.edit { it[BRIGHTNESS_KEY] = brightness.coerceIn(0, 100) }
    }

    suspend fun setNightMode(enabled: Boolean) {
        context.dataStore.edit { it[NIGHT_MODE_KEY] = enabled }
    }

    suspend fun setFloatingBallEnabled(enabled: Boolean) {
        context.dataStore.edit { it[FLOATING_BALL_KEY] = enabled }
    }

    suspend fun setDrivingModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[DRIVING_MODE_KEY] = enabled }
    }

    suspend fun setVoiceAssistantEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VOICE_ASSISTANT_KEY] = enabled }
    }

    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        private val NAV_VOICE_ENABLED_KEY = booleanPreferencesKey("nav_voice_enabled")
        private val MEDIA_VOLUME_KEY = intPreferencesKey("media_volume")
        private val AC_SYNC_KEY = booleanPreferencesKey("ac_sync")
        private val TEMP_UNIT_KEY = booleanPreferencesKey("temp_unit_celsius")
        private val BRIGHTNESS_KEY = intPreferencesKey("screen_brightness")
        private val NIGHT_MODE_KEY = booleanPreferencesKey("night_mode")
        private val FLOATING_BALL_KEY = booleanPreferencesKey("floating_ball")
        private val DRIVING_MODE_KEY = booleanPreferencesKey("driving_mode")
        private val VOICE_ASSISTANT_KEY = booleanPreferencesKey("voice_assistant")
    }
}
