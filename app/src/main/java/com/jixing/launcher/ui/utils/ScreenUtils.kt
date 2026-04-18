package com.jixing.launcher.ui.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * 屏幕适配工具类
 * 提供屏幕信息获取、网格列数计算、响应式尺寸获取等功能
 */
object ScreenUtils {
    
    /**
     * 获取屏幕宽度（像素）
     */
    fun getScreenWidthPx(context: Context): Int {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }
    
    /**
     * 获取屏幕高度（像素）
     */
    fun getScreenHeightPx(context: Context): Int {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
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
     * 获取最小屏幕宽度（dp）- 用于资源限定符匹配
     */
    fun getSmallestWidthDp(context: Context): Int {
        return context.resources.configuration.smallestScreenWidthDp
    }
    
    /**
     * dp转px
     */
    fun dpToPx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).roundToInt()
    }
    
    /**
     * px转dp
     */
    fun pxToDp(context: Context, px: Int): Float {
        return px / context.resources.displayMetrics.density
    }
    
    /**
     * 获取屏幕密度
     */
    fun getDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }
    
    /**
     * 判断是否为横屏
     */
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
    
    /**
     * 判断是否为平板（最小宽度 >= 600dp）
     */
    fun isTablet(context: Context): Boolean {
        return getSmallestWidthDp(context) >= 600
    }
    
    /**
     * 判断是否为车载大屏（最小宽度 >= 720dp）
     */
    fun isLargeScreen(context: Context): Boolean {
        return getSmallestWidthDp(context) >= 720
    }
    
    /**
     * 根据屏幕宽度计算网格列数
     * @param context 上下文
     * @param minItemWidthDp 每个网格项最小宽度(dp)
     * @param spacingDp 网格间距(dp)
     * @return 推荐网格列数
     */
    fun calculateGridColumns(context: Context, minItemWidthDp: Int = 100, spacingDp: Int = 12): Int {
        val screenWidthDp = getScreenWidthDp(context)
        val totalSpacing = spacingDp * (calculateMaxColumns(screenWidthDp, minItemWidthDp, spacingDp) + 1)
        val availableWidth = screenWidthDp - totalSpacing
        return (availableWidth / (minItemWidthDp + spacingDp)).coerceAtLeast(3)
    }
    
    /**
     * 计算最大可能的列数
     */
    private fun calculateMaxColumns(screenWidthDp: Int, minItemWidthDp: Int, spacingDp: Int): Int {
        var columns = 1
        while ((columns * minItemWidthDp + (columns + 1) * spacingDp) <= screenWidthDp) {
            columns++
        }
        return columns
    }
    
    /**
     * 获取推荐的网格列数
     * 根据不同屏幕尺寸返回合适的列数
     */
    fun getRecommendedGridColumns(context: Context): Int {
        val screenWidthDp = getScreenWidthDp(context)
        return when {
            screenWidthDp >= 840 -> 8  // 超大屏/电视
            screenWidthDp >= 720 -> 7  // 大屏车载/平板横屏
            screenWidthDp >= 600 -> 6  // 平板竖屏
            screenWidthDp >= 480 -> 5  // 大屏手机横屏
            screenWidthDp >= 400 -> 4  // 普通手机横屏
            screenWidthDp >= 360 -> 4  // 普通手机竖屏
            else -> 3                  // 小屏手机
        }
    }
    
    /**
     * 获取横屏推荐的网格列数
     */
    fun getRecommendedGridColumnsLandscape(context: Context): Int {
        val screenWidthDp = getScreenWidthDp(context)
        return when {
            screenWidthDp >= 960 -> 10  // 超大屏
            screenWidthDp >= 840 -> 9   // 大屏
            screenWidthDp >= 720 -> 8   // 车载大屏
            screenWidthDp >= 600 -> 7   // 平板横屏
            else -> 6                    // 普通横屏
        }
    }
    
    /**
     * 获取推荐的Dock栏图标数量
     */
    fun getRecommendedDockItemCount(context: Context): Int {
        val screenWidthDp = getScreenWidthDp(context)
        return when {
            screenWidthDp >= 720 -> 7
            screenWidthDp >= 600 -> 6
            screenWidthDp >= 480 -> 5
            else -> 4
        }
    }
    
    /**
     * 根据屏幕尺寸获取基础字体大小
     */
    fun getBaseTextSize(context: Context): Dp {
        val screenWidthDp = getScreenWidthDp(context)
        return when {
            screenWidthDp >= 720 -> 18.dp
            screenWidthDp >= 600 -> 16.dp
            screenWidthDp >= 480 -> 14.dp
            else -> 12.dp
        }
    }
    
    /**
     * 根据屏幕尺寸获取大字体大小
     */
    fun getLargeTextSize(context: Context): Dp {
        val screenWidthDp = getScreenWidthDp(context)
        return when {
            screenWidthDp >= 720 -> 36.dp
            screenWidthDp >= 600 -> 32.dp
            screenWidthDp >= 480 -> 28.dp
            else -> 24.dp
        }
    }
    
    /**
     * 获取屏幕尺寸分类
     */
    enum class ScreenCategory {
        SMALL_PHONE,      // 小屏手机 (<360dp)
        NORMAL_PHONE,      // 普通手机 (360-480dp)
        LARGE_PHONE,       // 大屏手机/横屏 (480-600dp)
        TABLET,           // 平板 (600-720dp)
        LARGE_TABLET,     // 大屏车载 (>=720dp)
        TV                // 电视/超大屏 (>=840dp)
    }
    
    fun getScreenCategory(context: Context): ScreenCategory {
        val screenWidthDp = getScreenWidthDp(context)
        return when {
            screenWidthDp >= 840 -> ScreenCategory.TV
            screenWidthDp >= 720 -> ScreenCategory.LARGE_TABLET
            screenWidthDp >= 600 -> ScreenCategory.TABLET
            screenWidthDp >= 480 -> ScreenCategory.LARGE_PHONE
            screenWidthDp >= 360 -> ScreenCategory.NORMAL_PHONE
            else -> ScreenCategory.SMALL_PHONE
        }
    }
}

/**
 * Compose中使用屏幕信息
 */
@Composable
fun rememberScreenInfo(): ScreenInfo {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    return remember(configuration) {
        ScreenInfo(
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp,
            smallestWidthDp = configuration.smallestScreenWidthDp,
            isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
            screenCategory = ScreenUtils.getScreenCategory(context)
        )
    }
}

/**
 * 屏幕信息数据类
 */
data class ScreenInfo(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val smallestWidthDp: Int,
    val isLandscape: Boolean,
    val screenCategory: ScreenUtils.ScreenCategory
) {
    val gridColumns: Int
        get() = if (isLandscape) {
            ScreenUtils.getRecommendedGridColumnsLandscape(
                LocalContext.current
            )
        } else {
            ScreenUtils.getRecommendedGridColumns(
                LocalContext.current
            )
        }
    
    val dockItemCount: Int
        get() = ScreenUtils.getRecommendedDockItemCount(
            LocalContext.current
        )
}
