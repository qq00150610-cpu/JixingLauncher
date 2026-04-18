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
 * 语音助手服务
 */
@AndroidEntryPoint
class VoiceAssistantService : Service() {

    private val binder = VoiceBinder()
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _lastCommand = MutableStateFlow<String?>(null)
    val lastCommand: StateFlow<String?> = _lastCommand.asStateFlow()

    private val CHANNEL_ID = "voice_assistant_channel"
    private val NOTIFICATION_ID = 1004

    inner class VoiceBinder : Binder() {
        fun getService(): VoiceAssistantService = this@VoiceAssistantService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    fun startListening() {
        _isListening.value = true
        updateNotification()
    }

    fun stopListening() {
        _isListening.value = false
        updateNotification()
    }

    fun speak(text: String) {
        _isSpeaking.value = true
        // TTS 实现
        updateNotification()
    }

    fun stopSpeaking() {
        _isSpeaking.value = false
        updateNotification()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "语音助手",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "语音助手服务"
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

        when {
            _isListening.value -> builder.setContentText("正在聆听...")
            _isSpeaking.value -> builder.setContentText("正在说话...")
            else -> builder.setContentText("语音助手就绪")
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
        stopListening()
        stopSpeaking()
    }

    companion object {
        private const val TAG = "VoiceAssistantService"
    }
}
