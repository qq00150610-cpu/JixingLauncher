package com.jixing.launcher.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build

/**
 * 屏幕方向工具类
 */
object OrientationUtils {

    /**
     * 判断当前是否为横屏
     */
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * 判断当前是否为竖屏
     */
    fun isPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * 获取屏幕方向
     */
    fun getOrientation(context: Context): Int {
        return context.resources.configuration.orientation
    }

    /**
     * 判断是否为平板（大屏幕设备）
     */
    fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        return screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    /**
     * 获取屏幕宽度（dp）
     */
    fun getScreenWidthDp(context: Context): Int {
        return context.resources.configuration.screenWidthDp
    }

    /**
     * 获取屏幕高度（dp）
     */
    fun getScreenHeightDp(context: Context): Int {
        return context.resources.configuration.screenHeightDp
    }

    /**
     * 获取网格列数（根据屏幕方向）
     */
    fun getGridColumns(context: Context): Int {
        return if (isLandscape(context)) {
            6 // 横屏 6 列
        } else {
            4 // 竖屏 4 列
        }
    }

    /**
     * 获取每页应用数量
     */
    fun getAppsPerPage(context: Context): Int {
        return getGridColumns(context) * 4 // 4 行
    }

    /**
     * 获取图标大小
     */
    fun getIconSize(context: Context): Int {
        return if (isLandscape(context)) 56 else 48
    }

    /**
     * 获取 Dock 栏图标数量
     */
    fun getDockItemCount(context: Context): Int {
        return if (isLandscape(context)) 6 else 5
    }

    /**
     * 判断是否为车载屏幕（宽屏）
     */
    fun isWideScreen(context: Context): Boolean {
        val widthDp = getScreenWidthDp(context)
        val heightDp = getScreenHeightDp(context)
        return widthDp >= 800 || (widthDp > heightDp && widthDp >= 600)
    }

    /**
     * 获取边距（根据屏幕方向）
     */
    fun getMargin(context: Context): Int {
        return if (isLandscape(context)) 24 else 16
    }

    /**
     * 获取卡片圆角大小
     */
    fun getCardCornerRadius(context: Context): Int {
        return if (isLandscape(context)) 20 else 16
    }

    /**
     * 获取触摸目标最小尺寸
     */
    fun getTouchTargetMin(context: Context): Int {
        return if (isLandscape(context)) 72 else 64
    }

    /**
     * 屏幕方向改变监听器接口
     */
    interface OnOrientationChangeListener {
        fun onOrientationChanged(isLandscape: Boolean)
    }
}
