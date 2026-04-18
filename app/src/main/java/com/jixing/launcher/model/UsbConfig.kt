package com.jixing.launcher.model

/**
 * USB 配置枚举
 */
enum class UsbConfig(val value: String, val displayName: String) {
    NONE("none", "无"),
    CHARGING("charging", "仅充电"),
    MTP("mtp", "文件传输 (MTP)"),
    PTP("ptp", "照片传输 (PTP)"),
    MIDI("midi", "MIDI"),
    RNDIS("rndis", "网络共享"),
    AUDIO_SOURCE("audio_source", "USB 音频"),
    NCM("ncm", "网络共享 (NCM)"),
    ADB("adb", "ADB 调试");

    companion object {
        fun fromValue(value: String): UsbConfig {
            return entries.find { it.value == value } ?: CHARGING
        }
    }
}

/**
 * 当前 USB 配置状态
 */
data class UsbState(
    val currentConfig: UsbConfig,
    val isConnected: Boolean,
    val isPowerConnected: Boolean,
    val isDataConnected: Boolean
)
