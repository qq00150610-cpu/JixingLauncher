package com.jixing.launcher.model

import android.graphics.drawable.Drawable

/**
 * 应用信息数据类
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean = false,
    val isEnabled: Boolean = true,
    val versionName: String = "",
    val versionCode: Long = 0,
    val installTime: Long = 0,
    val updateTime: Long = 0,
    val apkPath: String = ""
)

/**
 * 媒体信息数据类
 */
data class MediaInfo(
    val id: Long = 0,
    val title: String,
    val artist: String = "",
    val album: String = "",
    val duration: Long = 0,
    val albumArt: String? = null,
    val uri: String = "",
    val albumId: Long = 0
)

/**
 * 文件信息数据类
 */
data class FileInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Long = 0,
    val mimeType: String = ""
)

/**
 * 空调状态数据类
 */
data class AirConditionState(
    val isEnabled: Boolean = false,
    val temperature: Int = 24,
    val fanSpeed: Int = 2,
    val mode: AirConditionMode = AirConditionMode.AUTO,
    val acOn: Boolean = false,
    val heatOn: Boolean = false,
    val autoOn: Boolean = true,
    val syncOn: Boolean = false,
    val frontDefrost: Boolean = false,
    val rearDefrost: Boolean = false,
    val airQuality: Int = 0
)

enum class AirConditionMode {
    AUTO, COOL, HEAT, VENT, DEFROST
}

/**
 * 车辆状态数据类
 */
data class VehicleState(
    val speed: Float = 0f,
    val rpm: Float = 0f,
    val fuelLevel: Int = 0,
    val batteryVoltage: Float = 12.6f,
    val engineTemperature: Int = 90,
    val gear: GearPosition = GearPosition.PARK,
    val doorStatus: DoorStatus = DoorStatus(),
    val tirePressure: TirePressure = TirePressure(),
    val odometer: Long = 0,
    val drivingRange: Int = 0,
    val isDriving: Boolean = false
)

enum class GearPosition {
    PARK, REVERSE, NEUTRAL, DRIVE, SPORT, LOW
}

data class DoorStatus(
    val frontLeft: Boolean = false,
    val frontRight: Boolean = false,
    val rearLeft: Boolean = false,
    val rearRight: Boolean = false,
    val trunk: Boolean = false,
    val hood: Boolean = false
)

data class TirePressure(
    val frontLeft: Float = 2.3f,
    val frontRight: Float = 2.3f,
    val rearLeft: Float = 2.3f,
    val rearRight: Float = 2.3f
)

/**
 * 设置项数据类
 */
data class SettingItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val icon: Int = 0,
    val type: SettingType = SettingType.TEXT,
    val value: Any = "",
    val isEnabled: Boolean = true
)

enum class SettingType {
    TEXT, SWITCH, SLIDER, SELECT, ACTION
}

/**
 * 语音命令数据类
 */
data class VoiceCommand(
    val command: String,
    val type: VoiceCommandType,
    val response: String = "",
    val confidence: Float = 0f
)

enum class VoiceCommandType {
    NAVIGATION, MEDIA, PHONE, HVAC, APP, SYSTEM, UNKNOWN
}
