package com.jixing.launcher.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.roundToInt

/**
 * 屏幕尺寸和 DPI 计算工具
 */
object DisplayMetricsUtils {

    fun getDisplayMetrics(context: Context): DisplayMetrics {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getMetrics(metrics)
        return metrics
    }

    fun getRealDisplayMetrics(context: Context): DisplayMetrics {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        return metrics
    }

    fun dpToPx(context: Context, dp: Float): Int = (dp * context.resources.displayMetrics.density).roundToInt()
    fun pxToDp(context: Context, px: Int): Float = px / context.resources.displayMetrics.density
    fun spToPx(context: Context, sp: Float): Float = sp * context.resources.displayMetrics.scaledDensity
    fun pxToSp(context: Context, px: Float): Float = px / context.resources.displayMetrics.scaledDensity

    fun getScreenWidthPx(context: Context): Int = getDisplayMetrics(context).widthPixels
    fun getScreenHeightPx(context: Context): Int = getDisplayMetrics(context).heightPixels
    fun getScreenWidthDp(context: Context): Int = (getScreenWidthPx(context) / getDisplayMetrics(context).density).roundToInt()
    fun getScreenHeightDp(context: Context): Int = (getScreenHeightPx(context) / getDisplayMetrics(context).density).roundToInt()
    fun getDensity(context: Context): Float = context.resources.displayMetrics.density
    fun getDpi(context: Context): Int = context.resources.displayMetrics.densityDpi

    fun getScreenSizeInches(context: Context): Float {
        val metrics = getRealDisplayMetrics(context)
        val w = metrics.widthPixels.toFloat()
        val h = metrics.heightPixels.toFloat()
        val dpi = metrics.xdpi.coerceAtLeast(metrics.ydpi)
        return kotlin.math.sqrt(w * w + h * h) / dpi
    }

    fun calculateGridItemSize(context: Context, columns: Int, spacing: Int = 12): Int {
        val totalSpacing = spacing * (columns + 1)
        val availableWidth = getScreenWidthPx(context) - totalSpacing
        return (availableWidth / columns) - dpToPx(context, spacing.toFloat())
    }

    fun isLandscape(context: Context): Boolean = getScreenWidthPx(context) > getScreenHeightPx(context)

    enum class DpiCategory { LDPI, MDPI, HDPI, XHDPI, XXHDPI, XXXHDPI }
    
    fun getDpiCategory(context: Context): DpiCategory {
        return when (getDpi(context)) {
            DisplayMetrics.DENSITY_LOW -> DpiCategory.LDPI
            DisplayMetrics.DENSITY_MEDIUM -> DpiCategory.MDPI
            DisplayMetrics.DENSITY_HIGH -> DpiCategory.HDPI
            DisplayMetrics.DENSITY_XHIGH -> DpiCategory.XHDPI
            DisplayMetrics.DENSITY_XXHIGH -> DpiCategory.XXHDPI
            DisplayMetrics.DENSITY_XXXHIGH -> DpiCategory.XXXHDPI
            else -> DpiCategory.MDPI
        }
    }
}
