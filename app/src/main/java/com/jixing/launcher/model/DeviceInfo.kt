package com.jixing.launcher.model

/**
 * 设备信息数据模型
 */
data class DeviceInfo(
    val deviceModel: String,
    val androidVersion: String,
    val windlinkVersion: String,
    val kernelVersion: String,
    val buildNumber: String,
    val serialNumber: String,
    val wifiMacAddress: String,
    val bluetoothMacAddress: String,
    val storageTotal: Long,
    val storageUsed: Long,
    val storageAvailable: Long,
    val ramTotal: Long,
    val ramAvailable: Long,
    val batteryLevel: Int,
    val batteryStatus: BatteryStatus
)

/**
 * 电池状态
 */
enum class BatteryStatus {
    CHARGING, DISCHARGING, FULL, NOT_CHARGING, UNKNOWN
}

/**
 * 存储信息
 */
data class StorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long
) {
    val usedPercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes) * 100 else 0f
}

/**
 * 内存信息
 */
data class MemoryInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long = totalBytes - availableBytes
) {
    // 兼容属性别名
    val totalRam: Long get() = totalBytes
    val availableRam: Long get() = availableBytes
    val usedRam: Long get() = usedBytes

    val usedPercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes) * 100 else 0f
}
