package com.jixing.launcher.ui.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jixing.launcher.model.AdbAuthInfo
import com.jixing.launcher.model.AdbStatus
import com.jixing.launcher.model.AdbConnectionStatus
import com.jixing.launcher.model.UsbConfig
import com.jixing.launcher.model.UsbState
import com.jixing.launcher.utils.AdbManager
import com.jixing.launcher.utils.UsbConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 开发者选项 ViewModel
 */
@HiltViewModel
class DeveloperOptionsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val adbManager = AdbManager.getInstance(application)
    private val usbManager = UsbConfigManager.getInstance(application)

    private val _adbStatus = MutableStateFlow(AdbStatus.DISABLED)
    val adbStatus: StateFlow<AdbStatus> = _adbStatus.asStateFlow()

    private val _connectionStatus = MutableStateFlow(AdbConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<AdbConnectionStatus> = _connectionStatus.asStateFlow()

    private val _usbState = MutableStateFlow(UsbState(UsbConfig.CHARGING, false, false, false))
    val usbState: StateFlow<UsbState> = _usbState.asStateFlow()

    private val _authorizedComputers = MutableStateFlow<List<AdbAuthInfo>>(emptyList())
    val authorizedComputers: StateFlow<List<AdbAuthInfo>> = _authorizedComputers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                adbManager.updateStatus()
                _adbStatus.value = adbManager.adbStatus.value
                _connectionStatus.value = adbManager.getConnectionStatus()
                _authorizedComputers.value = adbManager.getAuthorizedComputers()
                _usbState.value = usbManager.getCurrentUsbState()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAdb() {
        viewModelScope.launch {
            try {
                if (_adbStatus.value.isEnabled) {
                    if (adbManager.disableAdb()) {
                        _message.value = "ADB 已禁用"
                    } else {
                        _message.value = "ADB 禁用失败"
                    }
                } else {
                    if (adbManager.enableAdb()) {
                        _message.value = "ADB 已启用"
                    } else {
                        _message.value = "ADB 启用失败"
                    }
                }
                refreshStatus()
            } catch (e: Exception) {
                _message.value = "操作失败: ${e.message}"
            }
        }
    }

    fun enableAdb() {
        viewModelScope.launch {
            try {
                if (adbManager.enableAdb()) {
                    _message.value = "ADB 已启用"
                    refreshStatus()
                } else {
                    _message.value = "ADB 启用失败，需要 Root 权限"
                }
            } catch (e: Exception) {
                _message.value = "启用失败: ${e.message}"
            }
        }
    }

    fun disableAdb() {
        viewModelScope.launch {
            try {
                if (adbManager.disableAdb()) {
                    _message.value = "ADB 已禁用"
                    refreshStatus()
                } else {
                    _message.value = "ADB 禁用失败，需要 Root 权限"
                }
            } catch (e: Exception) {
                _message.value = "禁用失败: ${e.message}"
            }
        }
    }

    fun enableWirelessDebugging() {
        viewModelScope.launch {
            try {
                if (adbManager.enableWirelessDebugging()) {
                    _message.value = "无线调试已启用，端口: ${adbManager.getWirelessPort()}"
                    refreshStatus()
                } else {
                    _message.value = "无线调试启用失败"
                }
            } catch (e: Exception) {
                _message.value = "启用失败: ${e.message}"
            }
        }
    }

    fun revokeAuthorization(keyFingerprint: String) {
        viewModelScope.launch {
            try {
                if (adbManager.revokeAuthorization(keyFingerprint)) {
                    _message.value = "已撤销授权"
                    refreshStatus()
                } else {
                    _message.value = "撤销授权失败"
                }
            } catch (e: Exception) {
                _message.value = "撤销失败: ${e.message}"
            }
        }
    }

    fun clearAllAuthorizations() {
        viewModelScope.launch {
            try {
                if (adbManager.clearAllAuthorizations()) {
                    _message.value = "已清除所有授权"
                    refreshStatus()
                } else {
                    _message.value = "清除授权失败"
                }
            } catch (e: Exception) {
                _message.value = "清除失败: ${e.message}"
            }
        }
    }

    fun setUsbConfig(config: UsbConfig) {
        viewModelScope.launch {
            try {
                if (usbManager.setUsbConfig(config)) {
                    _message.value = "USB 配置已更改为: ${config.displayName}"
                    refreshStatus()
                } else {
                    _message.value = "USB 配置更改失败"
                }
            } catch (e: Exception) {
                _message.value = "设置失败: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
