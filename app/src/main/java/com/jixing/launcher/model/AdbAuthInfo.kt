package com.jixing.launcher.model

/**
 * ADB 授权信息数据模型
 */
data class AdbAuthInfo(
    val computerName: String,
    val keyFingerprint: String,
    val authorizedTime: Long,
    val isCurrentComputer: Boolean = false
)

/**
 * ADB 状态
 */
data class AdbStatus(
    val isEnabled: Boolean,
    val isWirelessEnabled: Boolean,
    val wirelessPort: Int,
    val pairedCode: String?,
    val connectedComputers: List<AdbAuthInfo>,
    val isAuthorized: Boolean
) {
    companion object {
        val DISABLED = AdbStatus(
            isEnabled = false,
            isWirelessEnabled = false,
            wirelessPort = 5555,
            pairedCode = null,
            connectedComputers = emptyList(),
            isAuthorized = false
        )
    }
}

/**
 * ADB 连接状态
 */
enum class AdbConnectionStatus {
    DISCONNECTED,     // 未连接
    CONNECTING,       // 连接中
    CONNECTED,        // 已连接
    ENABLED_WIRED,    // 有线已启用
    ENABLED_WIRELESS, // 无线已启用
    PAIRING           // 配对中
}
