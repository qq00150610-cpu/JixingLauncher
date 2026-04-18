package com.jixing.launcher.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * 东风风神 Windlink X 系统适配器
 * 支持 Windlink X 1.0 (Android 8.x) - Windlink X 2.0 (Android 10.x)
 */
object WindlinkXAdapter {

    private const val STATUS_BAR_HEIGHT_DP = 48
    private const val NAV_BAR_HEIGHT_DP = 48

    /**
     * 检测是否运行在 Windlink X 系统
     */
    fun isWindlinkXSystem(context: Context): Boolean {
        val brand = Build.BRAND.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL
        return brand.contains("dongfeng") || manufacturer.contains("dongfeng") ||
               model.contains("Windlink", ignoreCase = true) || model.contains("AEOLUS", ignoreCase = true)
    }

    /**
     * 获取 Windlink X 版本: 1 = X 1.0, 2 = X 2.0
     */
    fun getWindlinkXVersion(): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> 2
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> 1
            else -> 0
        }
    }

    /**
     * 获取实际屏幕宽度
     */
    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        return metrics.widthPixels
    }

    /**
     * 获取实际屏幕高度
     */
    fun getScreenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        return metrics.heightPixels
    }

    /**
     * 获取屏幕 DPI
     */
    fun getScreenDpi(context: Context): Int {
        return context.resources.displayMetrics.densityDpi
    }

    /**
     * 状态栏高度（dp）
     */
    fun getStatusBarHeightDp() = STATUS_BAR_HEIGHT_DP

    /**
     * 导航栏高度（dp）
     */
    fun getNavBarHeightDp() = NAV_BAR_HEIGHT_DP

    /**
     * 获取系统字体缩放因子
     */
    fun getFontScale(context: Context) = context.resources.configuration.fontScale

    /**
     * 是否为大屏设备（12.3英寸及以上）
     */
    fun isLargeScreen(context: Context): Boolean {
        val metrics = context.resources.displayMetrics
        return (metrics.widthPixels / metrics.density) >= 800
    }

    /**
     * 获取推荐的网格列数
     */
    fun getRecommendedGridColumns(context: Context): Int {
        val width = getScreenWidth(context)
        return when {
            width >= 1920 -> 6
            width >= 1280 -> 5
            else -> 4
        }
    }

    /**
     * 获取推荐的图标大小
     */
    fun getRecommendedIconSize(context: Context): Int {
        return when {
            isLargeScreen(context) -> 64
            getScreenDpi(context) >= 320 -> 56
            else -> 48
        }
    }

    /**
     * 获取推荐的触摸目标尺寸
     */
    fun getRecommendedTouchTargetSize(context: Context): Int {
        return if (isLargeScreen(context)) 72 else 64
    }

    /**
     * 安全区域
     */
    data class SafeArea(val top: Int, val bottom: Int, val left: Int, val right: Int)
    
    fun getSafeArea(context: Context) = SafeArea(
        getStatusBarHeightDp(),
        getNavBarHeightDp(),
        16,
        16
    )
}
