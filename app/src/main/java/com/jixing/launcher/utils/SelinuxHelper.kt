package com.jixing.launcher.utils

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * SELinux 状态助手 - Windlink X 系统适配
 */
object SelinuxHelper {

    private const val TAG = "SelinuxHelper"

    /**
     * SELinux 模式
     */
    enum class SelinuxMode {
        ENFORCING,   // 严格模式
        PERMISSIVE,  // 宽松模式
        DISABLED,    // 已禁用
        UNKNOWN      // 未知
    }

    /**
     * 获取当前 SELinux 模式
     */
    fun getSelinuxMode(): SelinuxMode {
        return try {
            val result = executeCommand("getenforce")
            when (result.trim().lowercase()) {
                "enforcing" -> SelinuxMode.ENFORCING
                "permissive" -> SelinuxMode.PERMISSIVE
                else -> SelinuxMode.UNKNOWN
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get SELinux mode", e)
            // 尝试通过 /sys/fs/selinux/enforce 检查
            try {
                val enforceFile = java.io.File("/sys/fs/selinux/enforce")
                if (enforceFile.exists()) {
                    val status = enforceFile.readText().trim()
                    if (status == "0") SelinuxMode.PERMISSIVE else SelinuxMode.ENFORCING
                } else {
                    SelinuxMode.UNKNOWN
                }
            } catch (ex: Exception) {
                SelinuxMode.UNKNOWN
            }
        }
    }

    /**
     * 获取 SELinux 模式（字符串）
     */
    fun getSelinuxModeString(): String {
        return when (getSelinuxMode()) {
            SelinuxMode.ENFORCING -> "Enforcing"
            SelinuxMode.PERMISSIVE -> "Permissive"
            SelinuxMode.DISABLED -> "Disabled"
            SelinuxMode.UNKNOWN -> "Unknown"
        }
    }

    /**
     * 是否为严格模式
     */
    fun isEnforcing(): Boolean = getSelinuxMode() == SelinuxMode.ENFORCING

    /**
     * 检查是否有 Root 权限
     */
    fun hasRootPermission(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c id")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 执行命令
     */
    private fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute command: $command", e)
            ""
        }
    }

    /**
     * 以 Root 权限执行命令
     */
    private fun executeCommandAsRoot(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c $command")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute root command: $command", e)
            false
        }
    }

    /**
     * 临时关闭 SELinux（需要 Root）
     * 注意：这是临时方案，重启后恢复
     */
    fun disableSelinuxTemporarily(): Boolean {
        return try {
            val mode = getSelinuxMode()
            if (mode == SelinuxMode.PERMISSIVE || mode == SelinuxMode.DISABLED) {
                true // 已经不需要操作
            } else {
                val success = executeCommandAsRoot("setenforce 0")
                if (success) {
                    Log.i(TAG, "SELinux set to Permissive mode")
                }
                success
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable SELinux", e)
            false
        }
    }

    /**
     * 恢复 SELinux 为严格模式
     */
    fun enableSelinux(): Boolean {
        return try {
            val success = executeCommandAsRoot("setenforce 1")
            if (success) {
                Log.i(TAG, "SELinux set to Enforcing mode")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable SELinux", e)
            false
        }
    }

    /**
     * 禁用 SELinux（需要 Root，持久化）
     */
    fun disableSelinuxPermanently(): Boolean {
        return try {
            executeCommandAsRoot("setenforce 0")
            // 尝试修改启动参数
            executeCommandAsRoot("mount -o remount,rw /system")
            executeCommandAsRoot("echo \"SELINUX=disabled\" > /system/etc/selinux/config")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable SELinux permanently", e)
            false
        }
    }
}
