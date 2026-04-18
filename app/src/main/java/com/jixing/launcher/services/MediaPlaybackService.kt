package com.jixing.launcher.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jixing.launcher.R
import com.jixing.launcher.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * 媒体播放服务
 */
@AndroidEntryPoint
class MediaPlaybackService : Service(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private val binder = MediaBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private var currentUri: String? = null
    private var isPrepared = false
    
    private var onPlaybackStateChanged: ((Boolean) -> Unit)? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null

    private val CHANNEL_ID = "media_playback_channel"
    private val NOTIFICATION_ID = 1002

    inner class MediaBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener(this@MediaPlaybackService)
            setOnCompletionListener(this@MediaPlaybackService)
            setOnErrorListener(this@MediaPlaybackService)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> resume()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
        }
        startForeground(NOTIFICATION_ID, createNotification())
        return START_NOT_STICKY
    }

    fun play(uri: String) {
        currentUri = uri
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(uri)
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing media", e)
            onErrorListener?.invoke(e.message ?: "播放失败")
        }
    }

    fun resume() {
        if (isPrepared) {
            requestAudioFocus()
            mediaPlayer?.start()
            onPlaybackStateChanged?.invoke(true)
            updateNotification()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        onPlaybackStateChanged?.invoke(false)
        updateNotification()
    }

    fun stop() {
        mediaPlayer?.stop()
        isPrepared = false
        abandonAudioFocus()
        onPlaybackStateChanged?.invoke(false)
    }

    fun playNext() {
        // 播放下一首
        onCompletionListener?.invoke()
    }

    fun playPrevious() {
        // 播放上一首
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun setOnPlaybackStateChangedListener(listener: (Boolean) -> Unit) {
        onPlaybackStateChanged = listener
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    fun setOnErrorListener(listener: (String) -> Unit) {
        onErrorListener = listener
    }

    override fun onPrepared(mp: MediaPlayer) {
        isPrepared = true
        requestAudioFocus()
        mp.start()
        onPlaybackStateChanged?.invoke(true)
        updateNotification()
    }

    override fun onCompletion(mp: MediaPlayer) {
        isPrepared = false
        onCompletionListener?.invoke()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
        onErrorListener?.invoke("播放错误: $what")
        return true
    }

    private fun requestAudioFocus(): Boolean {
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> pause()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                    AudioManager.AUDIOFOCUS_GAIN -> resume()
                }
            }
            .build()

        return audioManager?.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "媒体播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "媒体播放控制"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("极行桌面")
            .setContentText("正在播放")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        abandonAudioFocus()
    }

    companion object {
        private const val TAG = "MediaPlaybackService"
        const val ACTION_PLAY = "com.jixing.launcher.action.PLAY"
        const val ACTION_PAUSE = "com.jixing.launcher.action.PAUSE"
        const val ACTION_STOP = "com.jixing.launcher.action.STOP"
        const val ACTION_NEXT = "com.jixing.launcher.action.NEXT"
        const val ACTION_PREVIOUS = "com.jixing.launcher.action.PREVIOUS"
    }
}
