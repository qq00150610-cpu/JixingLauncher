package com.jixing.launcher.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 格式化工具类
 */
object FormatUtils {

    /**
     * 格式化时间戳
     */
    fun formatTimestamp(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * 格式化文件大小
     */
    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "%.1f KB".format(size / 1024.0)
            size < 1024 * 1024 * 1024 -> "%.1f MB".format(size / (1024.0 * 1024))
            else -> "%.2f GB".format(size / (1024.0 * 1024 * 1024))
        }
    }

    /**
     * 格式化时长
     */
    fun formatDuration(durationMs: Long): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return when {
            hours > 0 -> "%d:%02d:%02d".format(hours, minutes % 60, seconds % 60)
            else -> "%d:%02d".format(minutes, seconds % 60)
        }
    }

    /**
     * 格式化速度
     */
    fun formatSpeed(speed: Float): String {
        return "%.1f km/h".format(speed)
    }

    /**
     * 格式化温度
     */
    fun formatTemperature(temp: Int, isCelsius: Boolean = true): String {
        return if (isCelsius) {
            "$temp°C"
        } else {
            "${temp * 9 / 5 + 32}°F"
        }
    }

    /**
     * 格式化百分比
     */
    fun formatPercentage(value: Float): String {
        return "%.0f%%".format(value)
    }

    /**
     * 格式化距离
     */
    fun formatDistance(distanceMeters: Float): String {
        return when {
            distanceMeters < 1000 -> "%.0f 米".format(distanceMeters)
            else -> "%.1f 公里".format(distanceMeters / 1000)
        }
    }

    /**
     * 格式化电压
     */
    fun formatVoltage(voltage: Float): String {
        return "%.1f V".format(voltage)
    }

    /**
     * 格式化转速
     */
    fun formatRpm(rpm: Float): String {
        return "%.0f RPM".format(rpm)
    }
}
