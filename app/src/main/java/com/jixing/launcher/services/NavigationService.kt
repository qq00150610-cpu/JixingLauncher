package com.jixing.launcher.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jixing.launcher.R
import com.jixing.launcher.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 导航服务
 */
@AndroidEntryPoint
class NavigationService : Service() {

    private val binder = NavigationBinder()
    
    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    private val _currentDestination = MutableStateFlow<String?>(null)
    val currentDestination: StateFlow<String?> = _currentDestination.asStateFlow()

    private val _eta = MutableStateFlow<String?>(null)
    val eta: StateFlow<String?> = _eta.asStateFlow()

    private val _distance = MutableStateFlow<String?>(null)
    val distance: StateFlow<String?> = _distance.asStateFlow()

    private val CHANNEL_ID = "navigation_channel"
    private val NOTIFICATION_ID = 1003

    inner class NavigationBinder : Binder() {
        fun getService(): NavigationService = this@NavigationService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    fun startNavigation(destination: String) {
        _currentDestination.value = destination
        _isNavigating.value = true
        _eta.value = calculateETA()
        _distance.value = calculateDistance()
        updateNotification()
    }

    fun stopNavigation() {
        _isNavigating.value = false
        _currentDestination.value = null
        _eta.value = null
        _distance.value = null
        updateNotification()
    }

    private fun calculateETA(): String {
        // 模拟计算 ETA
        val minutes = (15..45).random()
        return "$minutes 分钟"
    }

    private fun calculateDistance(): String {
        // 模拟计算距离
        val km = (5..30).random()
        return "$km 公里"
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "导航服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "导航指引服务"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("极行桌面")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (_isNavigating.value) {
            builder.setContentText("正在导航至: ${_currentDestination.value}")
        } else {
            builder.setContentText("导航服务运行中")
        }

        return builder.build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        stopNavigation()
    }

    companion object {
        private const val TAG = "NavigationService"
    }
}
