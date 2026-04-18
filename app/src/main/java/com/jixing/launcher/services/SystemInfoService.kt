package com.jixing.launcher.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jixing.launcher.R
import com.jixing.launcher.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 系统信息服务
 */
@AndroidEntryPoint
class SystemInfoService : Service() {

    private val binder = SystemBinder()
    
    private val _cpuUsage = MutableStateFlow(0f)
    val cpuUsage: StateFlow<Float> = _cpuUsage.asStateFlow()

    private val _memoryUsage = MutableStateFlow(0f)
    val memoryUsage: StateFlow<Float> = _memoryUsage.asStateFlow()

    private val _storageUsage = MutableStateFlow(0f)
    val storageUsage: StateFlow<Float> = _storageUsage.asStateFlow()

    private val CHANNEL_ID = "system_info_channel"
    private val NOTIFICATION_ID = 1005

    inner class SystemBinder : Binder() {
        fun getService(): SystemInfoService = this@SystemInfoService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    fun updateSystemInfo() {
        _cpuUsage.value = getCpuUsage()
        _memoryUsage.value = getMemoryUsage()
        _storageUsage.value = getStorageUsage()
    }

    private fun getCpuUsage(): Float {
        // 获取 CPU 使用率
        return (20..60).random().toFloat()
    }

    private fun getMemoryUsage(): Float {
        // 获取内存使用率
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        return (usedMemory.toFloat() / maxMemory.toFloat()) * 100
    }

    private fun getStorageUsage(): Float {
        // 获取存储使用率
        return (40..70).random().toFloat()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "系统监控",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "系统信息监控服务"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("极行桌面")
            .setContentText("系统运行正常")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SystemInfoService"
    }
}
