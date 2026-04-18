package com.jixing.launcher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jixing.launcher.data.repository.SettingsRepository
import com.jixing.launcher.model.SettingItem
import com.jixing.launcher.model.SettingType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置 ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow<List<SettingItem>>(emptyList())
    val settings: StateFlow<List<SettingItem>> = _settings.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _settings.value = listOf(
                SettingItem("dark_theme", "深色模式", "开启深色主题", com.jixing.launcher.R.drawable.ic_launcher, SettingType.SWITCH, true),
                SettingItem("brightness", "屏幕亮度", "调节屏幕亮度", com.jixing.launcher.R.drawable.ic_launcher, SettingType.SLIDER, 80),
                SettingItem("sound", "系统音效", "按键音和提示音", com.jixing.launcher.R.drawable.ic_launcher, SettingType.SWITCH, true),
                SettingItem("nav_voice", "导航语音", "播报导航指令", com.jixing.launcher.R.drawable.ic_launcher, SettingType.SWITCH, true),
                SettingItem("media_volume", "媒体音量", "调节媒体音量", com.jixing.launcher.R.drawable.ic_launcher, SettingType.SLIDER, 50),
                SettingItem("ac_sync", "温度同步", "左右区域联动", com.jixing.launcher.R.drawable.ic_launcher, SettingType.SWITCH, true),
                SettingItem("driving_mode", "驾驶模式", "简化界面", com.jixing.launcher.R.drawable.ic_launcher, SettingType.SWITCH, false),
                SettingItem("voice_assistant", "语音助手", "语音控制", com.jixing.launcher.R.drawable.ic_launcher, SettingType.SWITCH, true),
                SettingItem("floating_ball", "悬浮球", "快捷操作", com.jixing.launcher.R.drawable.ic_launcher, SettingType.SWITCH, true)
            )
        }
    }

    fun updateSetting(key: String, value: Any) {
        viewModelScope.launch {
            when (key) {
                "dark_theme" -> settingsRepository.setDarkTheme(value as Boolean)
                "brightness" -> settingsRepository.setScreenBrightness(value as Int)
                "sound" -> settingsRepository.setSoundEnabled(value as Boolean)
                "nav_voice" -> settingsRepository.setNavVoiceEnabled(value as Boolean)
                "media_volume" -> settingsRepository.setMediaVolume(value as Int)
                "ac_sync" -> settingsRepository.setAcSyncEnabled(value as Boolean)
                "driving_mode" -> settingsRepository.setDrivingModeEnabled(value as Boolean)
                "voice_assistant" -> settingsRepository.setVoiceAssistantEnabled(value as Boolean)
                "floating_ball" -> settingsRepository.setFloatingBallEnabled(value as Boolean)
            }
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            val newValue = !_isDarkTheme.value
            _isDarkTheme.value = newValue
            settingsRepository.setDarkTheme(newValue)
        }
    }
}
