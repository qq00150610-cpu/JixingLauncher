package com.jixing.launcher.utils

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process

/**
 * 全屏应用检测器 - Windlink X 系统适配
 */
object FullscreenAppDetector {

    private const val TAG = "FullscreenAppDetector"

    // 导航应用包名
    private val NAVIGATION_APPS = setOf(
        "com.autonavi.amapauto",           // 高德地图车机版
        "com.autonavi.amap",               // 高德地图
        "com.baidu.navi",                  // 百度地图
        "com.baidu.BaiduMap",             // 百度地图手机版
        "com.tencent.nav",                 // 腾讯地图
        "com.sogou.map.android.maps",     // 搜狗地图
        "com.google.android.apps.maps",    // Google Maps
        "com.waze"                        // Waze
    )

    // 音乐应用包名
    private val MUSIC_APPS = setOf(
        "com.kugou.android",               // 酷狗音乐
        "com.netease.cloudmusic",          // 网易云音乐
        "com.tencent.qqmusic",            // QQ音乐
        "com.xiami.westlake",             // 虾米音乐
        "com.kuwo.player",                // 酷我音乐
        "com.sina.weibo",                 // 微博
        "com.spotify.music",              // Spotify
        "com.apple.android.music"         // Apple Music
    )

    // 视频应用包名
    private val VIDEO_APPS = setOf(
        "com.youku.phone",                 // 优酷
        "com.iqiyi.video",                 // 爱奇艺
        "com.tencent.qqlive",              // 腾讯视频
        "com.baidu.video",                 // 百度视频
        "com.mediatek.mtkvideo.player",   // MTK 视频
        "com.mxtech.videoplayer",          // MX 播放器
        "com.google.android.videos",       // Google Play 电影
        "org.videolan.vlc"                // VLC
    )

    // 系统全屏应用
    private val SYSTEM_FULLSCREEN_APPS = setOf(
        "com.android.gallery3d",          // 图库
        "com.mediatek.camera",            // 相机
        "com.android.launcher",           // 启动器
        "com.jixing.launcher"            // 极行桌面
    )

    // 允许显示悬浮球的桌面应用
    private val LAUNCHER_APPS = setOf(
        "com.android.launcher",
        "com.android.launcher2",
        "com.android.launcher3",
        "com.jixing.launcher",
        "com.huawei.android.launcher",
        "com.miui.home",
        "com.coloros.slauncher",
        "com.oppo.launcher"
    )

    /**
     * 检测当前前台应用是否为全屏应用
     */
    fun isFullscreenApp(context: Context): Boolean {
        val currentPackage = getForegroundAppPackage(context)
        return isFullscreenPackage(currentPackage)
    }

    /**
     * 判断指定包名是否为全屏应用
     */
    fun isFullscreenPackage(packageName: String): Boolean {
        return packageName in NAVIGATION_APPS ||
               packageName in MUSIC_APPS ||
               packageName in VIDEO_APPS ||
               (packageName !in LAUNCHER_APPS && packageName !in SYSTEM_FULLSCREEN_APPS)
    }

    /**
     * 判断是否为导航应用
     */
    fun isNavigationApp(packageName: String): Boolean {
        return packageName in NAVIGATION_APPS
    }

    /**
     * 判断是否为音乐应用
     */
    fun isMusicApp(packageName: String): Boolean {
        return packageName in MUSIC_APPS
    }

    /**
     * 判断是否为视频应用
     */
    fun isVideoApp(packageName: String): Boolean {
        return packageName in VIDEO_APPS
    }

    /**
     * 判断是否为桌面应用
     */
    fun isLauncherApp(packageName: String): Boolean {
        return packageName in LAUNCHER_APPS || packageName == "com.jixing.launcher"
    }

    /**
     * 获取前台应用包名
     */
    fun getForegroundAppPackage(context: Context): String {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // Android 4.4 及以下
                @Suppress("DEPRECATION")
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                @Suppress("DEPRECATION")
                val tasks = am.getRunningTasks(1)
                tasks?.firstOrNull()?.topActivity?.packageName ?: ""
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                // Android 5.0 - 10
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val appProcesses = am.runningAppProcesses
                appProcesses?.firstOrNull {
                    it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                }?.processName ?: ""
            } else {
                // Android 11+ 使用 UsageStatsManager
                getForegroundPackageNew(context)
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Android 11+ 获取前台应用
     */
    private fun getForegroundPackageNew(context: Context): String {
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val time = System.currentTimeMillis()
            val usageStats = usm.queryUsageStats(
                android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 60,
                time
            )
            usageStats?.maxByOrNull { it.lastTimeUsed }?.packageName ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 检查是否有权限访问使用情况统计
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * 请求使用情况统计权限
     */
    fun requestUsageStatsPermission(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // 某些设备可能没有这个设置页面
        }
    }

    /**
     * 获取应用名称
     */
    fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    /**
     * 全屏应用分类
     */
    enum class AppCategory {
        NAVIGATION, MUSIC, VIDEO, OTHER
    }

    /**
     * 获取应用分类
     */
    fun getAppCategory(packageName: String): AppCategory {
        return when {
            packageName in NAVIGATION_APPS -> AppCategory.NAVIGATION
            packageName in MUSIC_APPS -> AppCategory.MUSIC
            packageName in VIDEO_APPS -> AppCategory.VIDEO
            else -> AppCategory.OTHER
        }
    }
}

private object Settings {
    object ACTION_USAGE_ACCESS_SETTINGS
}
