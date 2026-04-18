package com.jixing.launcher.utils

import android.content.Context
import android.hardware.usb.UsbManager
import com.jixing.launcher.model.UsbConfig
import com.jixing.launcher.model.UsbState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * USB 配置管理器
 */
class UsbConfigManager(private val context: Context) {

    private val _usbState = MutableStateFlow(getCurrentUsbState())
    val usbState: StateFlow<UsbState> = _usbState.asStateFlow()

    /**
     * 获取当前 USB 状态
     */
    fun getCurrentUsbState(): UsbState {
        return try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
            val devices = usbManager?.deviceList?.values?.toList() ?: emptyList()
            
            UsbState(
                currentConfig = getCurrentUsbConfig(),
                isConnected = devices.isNotEmpty(),
                isPowerConnected = isPowerConnected(),
                isDataConnected = devices.isNotEmpty()
            )
        } catch (e: Exception) {
            UsbState(UsbConfig.CHARGING, false, false, false)
        }
    }

    /**
     * 获取当前 USB 配置
     */
    fun getCurrentUsbConfig(): UsbConfig {
        return try {
            val config = getSystemProperty("sys.usb.config", "none")
            UsbConfig.fromValue(config)
        } catch (e: Exception) {
            UsbConfig.CHARGING
        }
    }

    /**
     * 检查是否连接电源
     */
    fun isPowerConnected(): Boolean {
        return try {
            val power = getSystemProperty("sys.power.connect", "0")
            power == "1"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 设置 USB 配置
     */
    fun setUsbConfig(config: UsbConfig): Boolean {
        return try {
            setSystemProperty("sys.usb.config", config.value)
            _usbState.value = getCurrentUsbState()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 通过反射获取系统属性
     */
    private fun getSystemProperty(key: String, default: String): String {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val get = systemProperties.getMethod("get", String::class.java, String::class.java)
            get.invoke(null, key, default) as String
        } catch (e: Exception) {
            default
        }
    }

    /**
     * 通过反射设置系统属性
     */
    private fun setSystemProperty(key: String, value: String) {
        try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val set = systemProperties.getMethod("set", String::class.java, String::class.java)
            set.invoke(null, key, value)
        } catch (e: Exception) {
            // 静默处理
        }
    }

    /**
     * 设置为充电模式
     */
    fun setChargingMode(): Boolean = setUsbConfig(UsbConfig.CHARGING)

    /**
     * 设置为 MTP 模式
     */
    fun setMtpMode(): Boolean = setUsbConfig(UsbConfig.MTP)

    /**
     * 设置为 PTP 模式
     */
    fun setPtpMode(): Boolean = setUsbConfig(UsbConfig.PTP)

    /**
     * 设置为 MIDI 模式
     */
    fun setMidiMode(): Boolean = setUsbConfig(UsbConfig.MIDI)

    /**
     * 更新状态
     */
    fun updateState() {
        _usbState.value = getCurrentUsbState()
    }
}
