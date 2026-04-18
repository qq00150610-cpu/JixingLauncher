package com.jixing.launcher.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 无线 ADB 管理器 - Windlink X 系统适配
 */
class WirelessAdbManager(private val context: Context) {

    private val _connectionInfo = MutableStateFlow<WirelessAdbConnectionInfo?>(null)
    val connectionInfo: StateFlow<WirelessAdbConnectionInfo?> = _connectionInfo.asStateFlow()

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _selinuxMode = MutableStateFlow(SelinuxHelper.SelinuxMode.UNKNOWN)
    val selinuxMode: StateFlow<SelinuxHelper.SelinuxMode> = _selinuxMode.asStateFlow()

    private var currentPort = DEFAULT_PORT

    init {
        refreshStatus()
    }

    /**
     * 刷新状态
     */
    fun refreshStatus() {
        _selinuxMode.value = SelinuxHelper.getSelinuxMode()
        _isEnabled.value = checkWirelessAdbEnabled()
    }

    /**
     * 开启无线 ADB
     */
    fun enableWirelessAdb(): WirelessAdbResult {
        // 1. 检测 SELinux 状态
        val selinuxMode = SelinuxHelper.getSelinuxMode()
        var selinuxAdjusted = false

        if (selinuxMode == SelinuxHelper.SelinuxMode.ENFORCING) {
            // 尝试关闭 SELinux
            if (!SelinuxHelper.disableSelinuxTemporarily()) {
                return WirelessAdbResult.SelinuxBlocked(
                    "SELinux 处于严格模式，无法执行。需要 Root 权限。"
                )
            }
            selinuxAdjusted = true
        }

        // 2. 检测可用端口
        val availablePort = PortDetector.getAvailableAdbPort()
        if (availablePort != currentPort && availablePort != DEFAULT_PORT) {
            currentPort = availablePort
            Log.i(TAG, "Using alternative port: $currentPort")
        }

        // 3. 执行开启命令
        return try {
            // 设置 ADB 端口
            executeShellCommand("setprop service.adb.tcp.port $currentPort")

            // 重启 adbd 服务
            executeShellCommand("stop adbd")
            executeShellCommand("start adbd")

            // 获取本机 IP
            val ip = getLocalIpAddress()

            val info = WirelessAdbConnectionInfo(
                ipAddress = ip,
                port = currentPort,
                connectCommand = "adb connect $ip:$currentPort",
                selinuxAdjusted = selinuxAdjusted,
                isEnabled = true
            )

            _connectionInfo.value = info
            _isEnabled.value = true

            Log.i(TAG, "Wireless ADB enabled: $ip:$currentPort")
            WirelessAdbResult.Success(info)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable wireless ADB", e)
            WirelessAdbResult.Failed(e.message ?: "未知错误")
        }
    }

    /**
     * 关闭无线 ADB
     */
    fun disableWirelessAdb(): Boolean {
        return try {
            // 恢复为 USB 模式
            executeShellCommand("setprop service.adb.tcp.port -1")

            // 重启 adbd 服务
            executeShellCommand("stop adbd")
            executeShellCommand("start adbd")

            _connectionInfo.value = null
            _isEnabled.value = false

            Log.i(TAG, "Wireless ADB disabled")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable wireless ADB", e)
            false
        }
    }

    /**
     * 检查无线 ADB 是否启用
     */
    fun checkWirelessAdbEnabled(): Boolean {
        return try {
            val port = executeShellCommand("getprop service.adb.tcp.port")
            val portValue = port.trim()
            portValue.isNotEmpty() && portValue != "-1" && portValue != "0"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取当前端口
     */
    fun getCurrentPort(): Int {
        return try {
            val port = executeShellCommand("getprop service.adb.tcp.port")
            port.trim().toIntOrNull() ?: DEFAULT_PORT
        } catch (e: Exception) {
            DEFAULT_PORT
        }
    }

    /**
     * 设置端口
     */
    fun setPort(port: Int): Boolean {
        return try {
            executeShellCommand("setprop service.adb.tcp.port $port")
            currentPort = port
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取本地 IP 地址
     */
    fun getLocalIpAddress(): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }

    /**
     * 获取 ADB 连接状态信息
     */
    fun getAdbStatus(): AdbStatusInfo {
        return AdbStatusInfo(
            isAdbEnabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED, 0
            ) == 1,
            isWirelessEnabled = checkWirelessAdbEnabled(),
            currentPort = getCurrentPort(),
            selinuxMode = SelinuxHelper.getSelinuxModeString(),
            hasRootPermission = SelinuxHelper.hasRootPermission(),
            localIpAddress = if (checkWirelessAdbEnabled()) getLocalIpAddress() else null
        )
    }

    /**
     * 执行 Shell 命令
     */
    private fun executeShellCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readText()
            reader.close()
            process.waitFor()
            result
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 无线 ADB 结果
     */
    sealed class WirelessAdbResult {
        data class Success(val info: WirelessAdbConnectionInfo) : WirelessAdbResult()
        data class Failed(val error: String) : WirelessAdbResult()
        data class SelinuxBlocked(val message: String) : WirelessAdbResult()
    }

    /**
     * 无线 ADB 连接信息
     */
    data class WirelessAdbConnectionInfo(
        val ipAddress: String,
        val port: Int,
        val connectCommand: String,
        val selinuxAdjusted: Boolean,
        val isEnabled: Boolean
    )

    /**
     * ADB 状态信息
     */
    data class AdbStatusInfo(
        val isAdbEnabled: Boolean,
        val isWirelessEnabled: Boolean,
        val currentPort: Int,
        val selinuxMode: String,
        val hasRootPermission: Boolean,
        val localIpAddress: String?
    )

    companion object {
        private const val TAG = "WirelessAdbManager"
        private const val DEFAULT_PORT = 5555

        @Volatile
        private var instance: WirelessAdbManager? = null

        fun getInstance(context: Context): WirelessAdbManager {
            return instance ?: synchronized(this) {
                instance ?: WirelessAdbManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
