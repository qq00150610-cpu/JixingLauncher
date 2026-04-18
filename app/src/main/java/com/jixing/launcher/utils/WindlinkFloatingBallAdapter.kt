package com.jixing.launcher.utils

import android.content.Context
import android.util.Log

/**
 * Windlink X 悬浮球适配器
 */
object WindlinkFloatingBallAdapter {

    private const val TAG = "WindlinkFBAdapter"

    /**
     * Windlink X 版本
     */
    enum class WindlinkVersion {
        WINDLINK_1_0,    // Android 8.x
        WINDLINK_1_5,    // Android 9.x
        WINDLINK_2_0,    // Android 10.x
        UNKNOWN
    }

    /**
     * 获取 Windlink X 版本
     */
    fun getWindlinkVersion(): WindlinkVersion {
        return try {
            when {
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q -> WindlinkVersion.WINDLINK_2_0
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P -> WindlinkVersion.WINDLINK_1_5
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O -> WindlinkVersion.WINDLINK_1_0
                else -> WindlinkVersion.UNKNOWN
            }
        } catch (e: Exception) {
            WindlinkVersion.UNKNOWN
        }
    }

    /**
     * 检测是否为 Windlink X 系统
     */
    fun isWindlinkXSystem(): Boolean {
        return try {
            val brand = android.os.Build.BRAND.lowercase()
            val manufacturer = android.os.Build.MANUFACTURER.lowercase()
            val model = android.os.Build.MODEL

            brand.contains("dongfeng") ||
            manufacturer.contains("dongfeng") ||
            model.contains("Windlink", ignoreCase = true) ||
            model.contains("AEOLUS", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取推荐的悬浮球窗口类型
     */
    fun getRecommendedWindowType(): Int {
        return if (isWindlinkXSystem()) {
            when (getWindlinkVersion()) {
                WindlinkVersion.WINDLINK_1_0 -> {
                    // Windlink 1.0 使用传统窗口类型
                    android.view.WindowManager.LayoutParams.TYPE_PHONE
                }
                WindlinkVersion.WINDLINK_1_5, WindlinkVersion.WINDLINK_2_0 -> {
                    // Windlink 1.5/2.0 使用 APPLICATION_OVERLAY
                    android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                }
                else -> android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            }
        } else {
            android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
    }

    /**
     * 获取悬浮球窗口标志
     */
    fun getWindowFlags(): Int {
        var flags = android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        if (isWindlinkXSystem()) {
            // Windlink X 可能需要额外标志
            when (getWindlinkVersion()) {
                WindlinkVersion.WINDLINK_1_0 -> {
                    // 可能需要禁用某些标志
                }
                WindlinkVersion.WINDLINK_1_5, WindlinkVersion.WINDLINK_2_0 -> {
                    flags = flags or android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                }
                else -> {}
            }
        }

        return flags
    }

    /**
     * 窗口参数调整
     */
    fun adjustWindowParams(params: android.view.WindowManager.LayoutParams) {
        if (isWindlinkXSystem()) {
            when (getWindlinkVersion()) {
                WindlinkVersion.WINDLINK_1_0 -> {
                    // Windlink 1.0 可能需要特殊处理
                    // 调整触摸模式
                }
                WindlinkVersion.WINDLINK_1_5, WindlinkVersion.WINDLINK_2_0 -> {
                    // Windlink 1.5/2.0 标准处理
                }
                else -> {}
            }
        }
    }

    /**
     * 获取悬浮球尺寸
     */
    fun getFloatingBallSize(): Int {
        return if (isWindlinkXSystem()) {
            when (getWindlinkVersion()) {
                WindlinkVersion.WINDLINK_1_0 -> 64  // 稍小
                WindlinkVersion.WINDLINK_1_5, WindlinkVersion.WINDLINK_2_0 -> 60
                else -> 60
            }
        } else {
            60
        }
    }

    /**
     * 获取边缘边距
     */
    fun getEdgeMargin(): Int {
        return if (isWindlinkXSystem()) {
            when (getWindlinkVersion()) {
                WindlinkVersion.WINDLINK_1_0 -> 24  // 更大边距
                WindlinkVersion.WINDLINK_1_5, WindlinkVersion.WINDLINK_2_0 -> 16
                else -> 16
            }
        } else {
            16
        }
    }

    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeightDp(): Int {
        return if (isWindlinkXSystem()) {
            // Windlink X 状态栏高度
            48
        } else {
            24
        }
    }

    /**
     * 获取导航栏高度
     */
    fun getNavBarHeightDp(): Int {
        return if (isWindlinkXSystem()) {
            // Windlink X 导航栏高度
            48
        } else {
            48
        }
    }

    /**
     * 是否需要特殊权限处理
     */
    fun needsSpecialPermissionHandling(): Boolean {
        return isWindlinkXSystem() && getWindlinkVersion() == WindlinkVersion.WINDLINK_1_0
    }

    /**
     * 获取悬浮球透明度
     */
    fun getFloatingBallAlpha(): Float {
        return if (isWindlinkXSystem()) {
            when (getWindlinkVersion()) {
                WindlinkVersion.WINDLINK_1_0 -> 0.9f
                WindlinkVersion.WINDLINK_1_5, WindlinkVersion.WINDLINK_2_0 -> 1.0f
                else -> 1.0f
            }
        } else {
            1.0f
        }
    }

    /**
     * 初始化 Windlink X 特定设置
     */
    fun initialize(context: Context) {
        if (isWindlinkXSystem()) {
            Log.i(TAG, "Initializing for Windlink X ${getWindlinkVersion()}")
            
            when (getWindlinkVersion()) {
                WindlinkVersion.WINDLINK_1_0 -> {
                    // Windlink 1.0 可能需要禁用 SELinux
                    if (SelinuxHelper.isEnforcing()) {
                        Log.w(TAG, "SELinux is Enforcing on Windlink 1.0")
                    }
                }
                WindlinkVersion.WINDLINK_1_5, WindlinkVersion.WINDLINK_2_0 -> {
                    // Windlink 1.5/2.0 标准处理
                }
                else -> {}
            }
        }
    }
}
