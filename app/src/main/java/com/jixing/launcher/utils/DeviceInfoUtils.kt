package com.jixing.launcher.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.jixing.launcher.model.*
import java.io.RandomAccessFile

/**
 * 设备信息获取工具
 */
object DeviceInfoUtils {

    /**
     * 获取完整设备信息
     */
    fun getDeviceInfo(context: Context): DeviceInfo {
        return DeviceInfo(
            deviceModel = getDeviceModel(),
            androidVersion = getAndroidVersion(),
            windlinkVersion = getWindlinkVersion(),
            kernelVersion = getKernelVersion(),
            buildNumber = getBuildNumber(),
            serialNumber = getSerialNumber(),
            wifiMacAddress = getWifiMacAddress(context),
            bluetoothMacAddress = getBluetoothMacAddress(),
            storageTotal = getTotalStorage(),
            storageUsed = getUsedStorage(),
            storageAvailable = getAvailableStorage(),
            ramTotal = getTotalRam(),
            ramAvailable = getAvailableRam(),
            batteryLevel = getBatteryLevel(context),
            batteryStatus = getBatteryStatus(context)
        )
    }

    /**
     * 获取设备型号
     */
    fun getDeviceModel(): String = Build.MODEL

    /**
     * 获取设备品牌
     */
    fun getDeviceBrand(): String = Build.BRAND

    /**
     * 获取 Android 版本
     */
    fun getAndroidVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    /**
     * 获取 Windlink X 版本
     */
    fun getWindlinkVersion(): String {
        // 尝试从系统属性获取
        return try {
            val process = Runtime.getRuntime().exec("getprop ro.product.version.windlink")
            val result = process.inputStream.bufferedReader().readText().trim()
            if (result.isNotEmpty()) "Windlink X $result" else "Windlink X 未知版本"
        } catch (e: Exception) {
            // 根据 Android 版本推测
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> "Windlink X 2.0"
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> "Windlink X 1.0"
                else -> "Windlink X 未知"
            }
        }
    }

    /**
     * 获取内核版本
     */
    fun getKernelVersion(): String {
        return try {
            System.getProperty("os.version") ?: "未知"
        } catch (e: Exception) {
            "未知"
        }
    }

    /**
     * 获取构建号
     */
    fun getBuildNumber(): String = Build.DISPLAY

    /**
     * 获取序列号
     */
    fun getSerialNumber(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()
            } else {
                @Suppress("DEPRECATION")
                Build.SERIAL
            }
        } catch (e: Exception) {
            "未知"
        }
    }

    /**
     * 获取 WiFi MAC 地址
     */
    fun getWifiMacAddress(context: Context): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo: WifiInfo = wifiManager.connectionInfo
            wifiInfo.macAddress ?: "未连接"
        } catch (e: Exception) {
            "未知"
        }
    }

    /**
     * 获取蓝牙 MAC 地址
     */
    fun getBluetoothMacAddress(): String {
        return try {
            val btAddr = java.io.File("/sys/class/bluetooth/hci0/address")
            if (btAddr.exists()) {
                btAddr.readText().trim()
            } else {
                "未知"
            }
        } catch (e: Exception) {
            "未知"
        }
    }

    /**
     * 获取总存储空间
     */
    fun getTotalStorage(): Long {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            stat.blockSizeLong * stat.blockCountLong
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 获取已用存储空间
     */
    fun getUsedStorage(): Long {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            stat.blockSizeLong * (stat.blockCountLong - stat.availableBlocksLong)
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 获取可用存储空间
     */
    fun getAvailableStorage(): Long {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            stat.blockSizeLong * stat.availableBlocksLong
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 获取总内存
     */
    fun getTotalRam(context: Context): Long {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            memInfo.totalMem
        } catch (e: Exception) {
            // 备用方案：从 /proc/meminfo 读取
            try {
                val reader = RandomAccessFile("/proc/meminfo", "r")
                val totalLine = reader.readLine()
                reader.close()
                val total = totalLine.replace("\\D+".toRegex(), "").toLongOrNull() ?: 0L
                total * 1024 // KB to Bytes
            } catch (ex: Exception) {
                0L
            }
        }
    }

    /**
     * 获取可用内存
     */
    fun getAvailableRam(context: Context): Long {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            memInfo.availMem
        } catch (e: Exception) {
            // 备用方案：从 /proc/meminfo 读取
            try {
                val reader = RandomAccessFile("/proc/meminfo", "r")
                reader.readLine() // 跳过总内存行
                val freeLine = reader.readLine()
                reader.close()
                val free = freeLine.replace("\\D+".toRegex(), "").toLongOrNull() ?: 0L
                free * 1024 // KB to Bytes
            } catch (ex: Exception) {
                0L
            }
        }
    }

    /**
     * 获取电池电量
     */
    fun getBatteryLevel(context: Context): Int {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * 获取电池状态
     */
    fun getBatteryStatus(context: Context): BatteryStatus {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
                BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
                BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
                else -> BatteryStatus.UNKNOWN
            }
        } catch (e: Exception) {
            BatteryStatus.UNKNOWN
        }
    }

    /**
     * 格式化字节大小
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
            else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
        }
    }
}
