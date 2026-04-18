package com.jixing.launcher

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jixing.launcher.managers.VehicleStateManager
import com.jixing.launcher.services.FloatingBallService
import com.jixing.launcher.utils.PermissionUtils
import dagger.hilt.android.HiltAndroidApp

/**
 * 极行桌面 - Android Automotive OS 车载启动器
 * Application 入口类
 */
@HiltAndroidApp
class CarLauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i(TAG, "Application onCreate")
        
        initCrashHandler()
        initManagers()
        checkAndRequestPermissions()
    }

    private fun initCrashHandler() {
        // 初始化全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            // 在生产环境中应该保存错误日志并重启应用
            // 可以在这里添加日志上报逻辑
        }
    }

    private fun initManagers() {
        // 预初始化核心管理器
        instance?.let { context ->
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    // 初始化车辆状态管理器
                    VehicleStateManager.getInstance(context)
                    Log.i(TAG, "VehicleStateManager initialized")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize VehicleStateManager", e)
                }
            }, 500)
        }
    }

    /**
     * 检查并请求必要权限
     */
    private fun checkAndRequestPermissions() {
        instance?.let { context ->
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    // 检查悬浮窗权限并启动悬浮球服务
                    if (PermissionUtils.canDrawOverlays(context)) {
                        startFloatingBallService()
                    } else {
                        Log.w(TAG, "Overlay permission not granted yet")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking permissions", e)
                }
            }, 1000)
        }
    }

    /**
     * 启动悬浮球服务
     */
    private fun startFloatingBallService() {
        try {
            val intent = Intent(this, FloatingBallService::class.java)
            startForegroundService(intent)
            Log.i(TAG, "FloatingBallService started from Application")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start FloatingBallService", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            VehicleStateManager.getInstance().cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up VehicleStateManager", e)
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed")
    }

    companion object {
        private const val TAG = "CarLauncherApp"

        @Volatile
        private var instance: CarLauncherApplication? = null

        fun getInstance(): CarLauncherApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }

        fun getAppContext(): Context {
            return getInstance().applicationContext
        }

        /**
         * 检查是否为车载环境
         */
        fun isAutomotiveEnvironment(): Boolean {
            return try {
                VehicleStateManager.getInstance().isInAutomotiveEnvironment()
            } catch (e: Exception) {
                false
            }
        }
    }
}
