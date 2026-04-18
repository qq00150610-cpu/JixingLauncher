package com.jixing.launcher.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

/**
 * 端口检测与选择工具 - Windlink X 系统适配
 */
object PortDetector {

    private const val TAG = "PortDetector"

    // 推荐的 ADB 端口列表
    private val RECOMMENDED_PORTS = listOf(
        5555,  // 默认 ADB 端口
        5556,  // 备用端口 1
        5557,  // 备用端口 2
        5558,  // 备用端口 3
        5559   // 备用端口 4
    )

    // ADB 服务器端口
    const val ADB_SERVER_PORT = 5037

    /**
     * 检测端口是否被占用
     */
    fun isPortInUse(port: Int): Boolean {
        return try {
            val socket = ServerSocket(port)
            socket.close()
            false // 端口可用
        } catch (e: Exception) {
            true // 端口被占用
        }
    }

    /**
     * 获取可用端口（自动选择）
     */
    fun getAvailableAdbPort(): Int {
        for (port in RECOMMENDED_PORTS) {
            if (!isPortInUse(port) && !isWindlinkServicePort(port)) {
                return port
            }
        }
        // 如果常用端口都被占用，寻找随机可用端口
        return findRandomAvailablePort()
    }

    /**
     * 查找指定范围内的可用端口
     */
    fun findAvailablePortInRange(start: Int, end: Int, step: Int = 1): Int? {
        for (port in start until end step step) {
            if (!isPortInUse(port) && !isWindlinkServicePort(port)) {
                return port
            }
        }
        return null
    }

    /**
     * 查找随机可用端口
     */
    fun findRandomAvailablePort(): Int {
        // 在 50000-60000 范围内寻找可用端口
        for (port in (50000..60000).shuffled()) {
            if (!isPortInUse(port) && !isWindlinkServicePort(port)) {
                return port
            }
        }
        return 5556 // 默认返回备用端口
    }

    /**
     * 检测是否为 Windlink 互联服务占用的端口
     */
    fun isWindlinkServicePort(port: Int): Boolean {
        val windlinkPorts = getWindlinkServicePorts()
        return port in windlinkPorts
    }

    /**
     * 获取 Windlink 互联服务占用的端口
     */
    fun getWindlinkServicePorts(): List<Int> {
        val ports = mutableListOf<Int>()
        try {
            // 通过 netstat 获取端口占用
            val process = Runtime.getRuntime().exec("netstat -tuln 2>/dev/null")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()

            // 解析输出，查找 Windlink 相关端口
            output.lines().forEach { line ->
                if (line.contains("windlink", ignoreCase = true) ||
                    line.contains("carlink", ignoreCase = true) ||
                    line.contains("dongfeng", ignoreCase = true)
                ) {
                    extractPortFromNetstatLine(line)?.let { ports.add(it) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Windlink service ports", e)
        }
        return ports
    }

    /**
     * 从 netstat 行中提取端口号
     */
    private fun extractPortFromNetstatLine(line: String): Int? {
        return try {
            // 匹配模式: 0.0.0.0:PORT 或 *:* 或 :::PORT
            val portRegex = """(?:0\.0\.0\.0:|::|:)(\d+)""".toRegex()
            val match = portRegex.find(line)
            match?.groupValues?.getOrNull(1)?.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取端口占用详情
     */
    fun getPortUsageInfo(port: Int): PortInfo {
        return try {
            val process = Runtime.getRuntime().exec("netstat -tulpn 2>/dev/null")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()

            var processName = "Unknown"
            var state = "UNKNOWN"
            var pid = -1

            output.lines().forEach { line ->
                if (line.contains(":$port")) {
                    // 提取进程信息
                    val pidRegex = """/(\d+)/.*""".toRegex()
                    val pidMatch = pidRegex.find(line)
                    pid = pidMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: -1

                    // 提取进程名称
                    val nameRegex = """/([a-zA-Z0-9_.-]+)(?:/|$)""".toRegex()
                    val nameMatch = nameRegex.find(line)
                    processName = nameMatch?.groupValues?.getOrNull(1) ?: "Unknown"

                    // 提取状态
                    state = line.substringAfter("ESTABLISHED")
                        .substringAfter("LISTEN")
                        .substringAfter("TIME_WAIT")
                        .substringBefore(" ")
                        .trim()
                        .ifEmpty { "LISTEN" }
                }
            }

            PortInfo(
                port = port,
                isInUse = isPortInUse(port),
                processName = processName,
                pid = pid,
                state = state
            )
        } catch (e: Exception) {
            PortInfo(port = port, isInUse = isPortInUse(port))
        }
    }

    /**
     * 获取所有 ADB 相关端口的状态
     */
    fun getAllAdbPortStatus(): List<PortInfo> {
        val allPorts = RECOMMENDED_PORTS + ADB_SERVER_PORT
        return allPorts.map { getPortUsageInfo(it) }
    }

    /**
     * 端口信息数据类
     */
    data class PortInfo(
        val port: Int,
        val isInUse: Boolean,
        val processName: String = "Unknown",
        val pid: Int = -1,
        val state: String = "UNKNOWN"
    )

    /**
     * 检查端口是否可能有冲突
     */
    fun hasPotentialConflict(port: Int): Boolean {
        return isPortInUse(port) || isWindlinkServicePort(port)
    }

    /**
     * 获取推荐的端口列表（排除已占用的）
     */
    fun getRecommendedPorts(): List<Int> {
        return RECOMMENDED_PORTS.filter { !hasPotentialConflict(it) }
    }
}
