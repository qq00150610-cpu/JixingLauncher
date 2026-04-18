package com.jixing.launcher.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.jixing.launcher.services.FloatingBallService

/**
 * 桌面服务管理器 - 悬浮球控制
 */
class LauncherServiceManager(private val context: Context) {

    private val TAG = "LauncherServiceManager"
    private var monitorHandler: Handler? = null
    private var monitorRunnable: Runnable? = null
    private var isMonitoring = false

    // 回调接口
    var onFullscreenAppChanged: ((Boolean, String) -> Unit)? = null

    /**
     * 检查悬浮窗权限
     */
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * 请求悬浮窗权限
     */
    fun requestOverlayPermission() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request overlay permission", e)
        }
    }

    /**
     * 启动悬浮球服务
     */
    fun startFloatingBallService() {
        if (!hasOverlayPermission()) {
            Log.w(TAG, "No overlay permission")
            return
        }

        try {
            val intent = Intent(context, FloatingBallService::class.java)
            context.startForegroundService(intent)
            Log.i(TAG, "Floating ball service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start floating ball service", e)
        }
    }

    /**
     * 停止悬浮球服务
     */
    fun stopFloatingBallService() {
        try {
            stopMonitoring()
            val intent = Intent(context, FloatingBallService::class.java)
            context.stopService(intent)
            Log.i(TAG, "Floating ball service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop floating ball service", e)
        }
    }

    /**
     * 显示悬浮球
     */
    fun showFloatingBall() {
        if (!hasOverlayPermission()) return
        try {
            val intent = Intent(context, FloatingBallService::class.java).apply {
                action = FloatingBallService.ACTION_SHOW
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show floating ball", e)
        }
    }

    /**
     * 隐藏悬浮球
     */
    fun hideFloatingBall() {
        try {
            val intent = Intent(context, FloatingBallService::class.java).apply {
                action = FloatingBallService.ACTION_HIDE
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide floating ball", e)
        }
    }

    /**
     * 切换悬浮球显示状态
     */
    fun toggleFloatingBall() {
        try {
            val intent = Intent(context, FloatingBallService::class.java).apply {
                action = FloatingBallService.ACTION_TOGGLE
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle floating ball", e)
        }
    }

    /**
     * 启动前台应用监控
     */
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true

        monitorHandler = Handler(Looper.getMainLooper())
        monitorRunnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return

                try {
                    val currentPackage = FullscreenAppDetector.getForegroundAppPackage(context)
                    val isFullscreen = FullscreenAppDetector.isFullscreenPackage(currentPackage)
                    val isLauncher = FullscreenAppDetector.isLauncherApp(currentPackage)

                    // 全屏应用时显示悬浮球，非桌面时
                    if (isFullscreen && !isLauncher) {
                        showFloatingBall()
                    } else {
                        hideFloatingBall()
                    }

                    onFullscreenAppChanged?.invoke(isFullscreen, currentPackage)
                } catch (e: Exception) {
                    Log.e(TAG, "Monitor error", e)
                }

                monitorHandler?.postDelayed(this, MONITOR_INTERVAL)
            }
        }

        monitorHandler?.post(monitorRunnable!!)
        Log.i(TAG, "Foreground app monitoring started")
    }

    /**
     * 停止前台应用监控
     */
    fun stopMonitoring() {
        isMonitoring = false
        monitorRunnable?.let { monitorHandler?.removeCallbacks(it) }
        monitorRunnable = null
        monitorHandler = null
        Log.i(TAG, "Foreground app monitoring stopped")
    }

    /**
     * 检查是否有权限访问任务栈
     */
    fun hasTaskPermission(): Boolean {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0+ 可以使用 getRunningTasks
                @Suppress("DEPRECATION")
                val tasks = am.getRunningTasks(1)
                !tasks.isNullOrEmpty()
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 返回桌面
     */
    fun goHome() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to go home", e)
        }
    }

    /**
     * 获取当前前台应用
     */
    fun getCurrentForegroundApp(): String {
        return FullscreenAppDetector.getForegroundAppPackage(context)
    }

    /**
     * 获取当前应用分类
     */
    fun getCurrentAppCategory(): FullscreenAppDetector.AppCategory {
        val packageName = getCurrentForegroundApp()
        return FullscreenAppDetector.getAppCategory(packageName)
    }

    /**
     * 是否为全屏应用
     */
    fun isCurrentAppFullscreen(): Boolean {
        val packageName = getCurrentForegroundApp()
        return FullscreenAppDetector.isFullscreenPackage(packageName)
    }

    companion object {
        private const val MONITOR_INTERVAL = 1000L // 1秒检测一次

        @Volatile
        private var instance: LauncherServiceManager? = null

        fun getInstance(context: Context): LauncherServiceManager {
            return instance ?: synchronized(this) {
                instance ?: LauncherServiceManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
