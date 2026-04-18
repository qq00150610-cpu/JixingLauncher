package com.jixing.launcher.utils

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import com.jixing.launcher.model.AdbAuthInfo
import com.jixing.launcher.model.AdbStatus
import com.jixing.launcher.model.AdbConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * ADB 状态管理器
 */
class AdbManager(private val context: Context) {

    private val _adbStatus = MutableStateFlow(AdbStatus.DISABLED)
    val adbStatus: StateFlow<AdbStatus> = _adbStatus.asStateFlow()

    /**
     * 检查 ADB 是否启用
     */
    fun isAdbEnabled(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 启用 ADB
     */
    fun enableAdb(): Boolean {
        return try {
            Settings.Global.putInt(context.contentResolver, Settings.Global.ADB_ENABLED, 1)
            updateStatus()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 禁用 ADB
     */
    fun disableAdb(): Boolean {
        return try {
            Settings.Global.putInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0)
            updateStatus()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 启用无线调试
     */
    fun enableWirelessDebugging(): Boolean {
        if (!isAdbEnabled()) return false
        return try {
            // 传统方式设置 TCP 端口
            Runtime.getRuntime().exec("setprop service.adb.tcp.port 5555")
            Runtime.getRuntime().exec("stop adbd")
            Runtime.getRuntime().exec("start adbd")
            updateStatus()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取无线调试端口
     */
    fun getWirelessPort(): Int {
        return try {
            val process = Runtime.getRuntime().exec("getprop service.adb.tcp.port")
            val port = process.inputStream.bufferedReader().readText().trim()
            port.toIntOrNull() ?: 5555
        } catch (e: Exception) {
            5555
        }
    }

    /**
     * 检查是否启用无线调试
     */
    fun isWirelessDebuggingEnabled(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getprop service.adb.tcp.port")
            val port = process.inputStream.bufferedReader().readText().trim()
            port.isNotEmpty() && port != "-1"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取已授权的计算机列表
     */
    fun getAuthorizedComputers(): List<AdbAuthInfo> {
        val authDir = File("/data/misc/adb/")
        if (!authDir.exists()) return emptyList()

        return authDir.listFiles()?.filter { it.name.endsWith(".adb_key") || it.name == "adb_keys" }
            ?.mapIndexed { index, file ->
                AdbAuthInfo(
                    computerName = "计算机 ${index + 1}",
                    keyFingerprint = file.name,
                    authorizedTime = file.lastModified(),
                    isCurrentComputer = false
                )
            } ?: emptyList()
    }

    /**
     * 撤销授权
     */
    fun revokeAuthorization(keyFingerprint: String): Boolean {
        return try {
            val keyFile = File("/data/misc/adb/$keyFingerprint")
            if (keyFile.exists()) {
                keyFile.delete()
                updateStatus()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 清除所有授权
     */
    fun clearAllAuthorizations(): Boolean {
        return try {
            val authDir = File("/data/misc/adb/")
            authDir.listFiles()?.forEach { it.delete() }
            updateStatus()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取连接状态
     */
    fun getConnectionStatus(): AdbConnectionStatus {
        return when {
            !isAdbEnabled() -> AdbConnectionStatus.DISCONNECTED
            isWirelessDebuggingEnabled() -> AdbConnectionStatus.ENABLED_WIRELESS
            getAuthorizedComputers().isNotEmpty() -> AdbConnectionStatus.CONNECTED
            else -> AdbConnectionStatus.ENABLED_WIRED
        }
    }

    /**
     * 更新状态
     */
    fun updateStatus() {
        _adbStatus.value = AdbStatus(
            isEnabled = isAdbEnabled(),
            isWirelessEnabled = isWirelessDebuggingEnabled(),
            wirelessPort = getWirelessPort(),
            pairedCode = null,
            connectedComputers = getAuthorizedComputers(),
            isAuthorized = getAuthorizedComputers().isNotEmpty()
        )
    }

    companion object {
        @Volatile
        private var instance: AdbManager? = null

        fun getInstance(context: Context): AdbManager {
            return instance ?: synchronized(this) {
                instance ?: AdbManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
