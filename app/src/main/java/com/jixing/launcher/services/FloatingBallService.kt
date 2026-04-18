package com.jixing.launcher.services

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.core.app.NotificationCompat
import com.jixing.launcher.R
import com.jixing.launcher.ui.main.MainActivity
import com.jixing.launcher.utils.FullscreenAppDetector
import com.jixing.launcher.utils.WindlinkFloatingBallAdapter

/**
 * 全局悬浮球服务 - 返回桌面功能
 * 在全屏应用时显示悬浮球，点击返回桌面
 */
class FloatingBallService : Service() {

    private val TAG = "FloatingBallService"

    // Window Manager
    private lateinit var windowManager: WindowManager
    private var floatingBallView: View? = null
    private var params: WindowManager.LayoutParams? = null

    // 状态
    private var isVisible = false
    private var isExpanded = false
    private var isDragging = false

    // 位置
    private var initialX = 0
    private var initialY = 0
    private var lastX = 0
    private var lastY = 0

    // 监控
    private var monitorHandler: Handler? = null
    private var monitorRunnable: Runnable? = null
    private var isMonitoring = false

    // 尺寸配置
    private val ballSizeDp: Int
        get() = WindlinkFloatingBallAdapter.getFloatingBallSize()

    private val edgeMarginDp: Int
        get() = WindlinkFloatingBallAdapter.getEdgeMargin()

    // 通知频道 ID
    private val CHANNEL_ID = "floating_ball_channel"
    private val NOTIFICATION_ID = 1001

    companion object {
        const val ACTION_SHOW = "com.jixing.launcher.action.SHOW_FLOATING_BALL"
        const val ACTION_HIDE = "com.jixing.launcher.action.HIDE_FLOATING_BALL"
        const val ACTION_TOGGLE = "com.jixing.launcher.action.TOGGLE_FLOATING_BALL"

        private const val MONITOR_INTERVAL = 1000L // 1秒检测一次
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "FloatingBallService created")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 检查悬浮窗权限
        if (!checkOverlayPermission()) {
            Log.w(TAG, "Overlay permission not granted, service will not create floating ball")
            // 不立即停止，等待权限授予
        } else {
            // 创建悬浮球
            createFloatingBall()
            
            // 初始化 Windlink X 适配
            WindlinkFloatingBallAdapter.initialize(this)
        }

        // 启动前台通知
        startForeground(NOTIFICATION_ID, createNotification())
    }

    /**
     * 检查悬浮窗权限
     */
    private fun checkOverlayPermission(): Boolean {
        return android.provider.Settings.canDrawOverlays(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showFloatingBall()
            ACTION_HIDE -> hideFloatingBall()
            ACTION_TOGGLE -> toggleFloatingBall()
            else -> {
                // 默认启动监控
                if (!isMonitoring) {
                    startMonitoring()
                }
            }
        }
        return START_STICKY
    }

    /**
     * 创建悬浮球视图
     */
    private fun createFloatingBall() {
        if (floatingBallView != null) return

        // 再次检查权限
        if (!checkOverlayPermission()) {
            Log.w(TAG, "Cannot create floating ball: no overlay permission")
            return
        }

        try {
            // 创建悬浮球布局
            floatingBallView = LayoutInflater.from(this).inflate(R.layout.floating_ball_layout, null)

            // 设置窗口参数
            val windowType = WindlinkFloatingBallAdapter.getRecommendedWindowType()
            val flags = WindlinkFloatingBallAdapter.getWindowFlags()

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                flags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                // 初始位置：屏幕右侧中部
                x = getScreenWidth() - dpToPx(ballSizeDp + edgeMarginDp)
                y = getScreenHeight() / 2 - dpToPx(ballSizeDp / 2)
                width = dpToPx(ballSizeDp)
                height = dpToPx(ballSizeDp)
            }

            // 应用 Windlink 特定调整
            WindlinkFloatingBallAdapter.adjustWindowParams(params!!)

            // 设置触摸监听
            setupTouchListener()

            // 设置点击监听
            floatingBallView?.setOnClickListener {
                if (!isDragging) {
                    onBallClicked()
                }
            }

            // 添加到窗口
            windowManager.addView(floatingBallView, params)

            Log.i(TAG, "Floating ball created")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception creating floating ball - permission may have been revoked", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create floating ball", e)
        }
    }

    /**
     * 设置触摸监听
     */
    private fun setupTouchListener() {
        floatingBallView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX.toInt() - lastX
                    val dy = event.rawY.toInt() - lastY

                    // 判断是否为拖拽
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isDragging = true
                    }

                    params?.x = initialX + dx
                    params?.y = initialY + dy
                    windowManager.updateViewLayout(floatingBallView, params)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 贴边吸附
                    snapToEdge()
                    isDragging = false
                    floatingBallView?.performClick()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * 贴边吸附动画
     */
    private fun snapToEdge() {
        val screenWidth = getScreenWidth()
        val currentX = params?.x ?: 0
        val screenCenter = screenWidth / 2

        val targetX = if (currentX < screenCenter) {
            dpToPx(edgeMarginDp) // 左边缘
        } else {
            screenWidth - dpToPx(ballSizeDp + edgeMarginDp) // 右边缘
        }

        animateToPosition(targetX, params?.y ?: 0)
    }

    /**
     * 动画移动到指定位置
     */
    private fun animateToPosition(targetX: Int, targetY: Int) {
        val startX = params?.x ?: 0
        val startY = params?.y ?: 0

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                params?.x = (startX + (targetX - startX) * fraction).toInt()
                params?.y = (startY + (targetY - startY) * fraction).toInt()
                windowManager.updateViewLayout(floatingBallView, params)
            }
            start()
        }
    }

    /**
     * 点击悬浮球
     */
    private fun onBallClicked() {
        goHome()
    }

    /**
     * 返回桌面
     */
    private fun goHome() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            Log.i(TAG, "Going home")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to go home", e)
        }
    }

    /**
     * 显示悬浮球
     */
    private fun showFloatingBall() {
        floatingBallView?.visibility = View.VISIBLE
        isVisible = true
        Log.i(TAG, "Floating ball shown")
    }

    /**
     * 隐藏悬浮球
     */
    private fun hideFloatingBall() {
        floatingBallView?.visibility = View.GONE
        isVisible = false
        Log.i(TAG, "Floating ball hidden")
    }

    /**
     * 切换显示状态
     */
    private fun toggleFloatingBall() {
        if (isVisible) {
            hideFloatingBall()
        } else {
            showFloatingBall()
        }
    }

    /**
     * 启动前台应用监控
     */
    private fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true

        monitorHandler = Handler(Looper.getMainLooper())
        monitorRunnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return

                try {
                    val currentPackage = FullscreenAppDetector.getForegroundAppPackage(this@FloatingBallService)
                    val isFullscreen = FullscreenAppDetector.isFullscreenPackage(currentPackage)
                    val isLauncher = FullscreenAppDetector.isLauncherApp(currentPackage)

                    // 全屏应用时显示悬浮球，桌面时隐藏
                    if (isFullscreen && !isLauncher) {
                        if (!isVisible) showFloatingBall()
                    } else {
                        if (isVisible) hideFloatingBall()
                    }
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
     * 停止监控
     */
    private fun stopMonitoring() {
        isMonitoring = false
        monitorRunnable?.let { monitorHandler?.removeCallbacks(it) }
        monitorRunnable = null
        monitorHandler = null
    }

    /**
     * 创建前台通知
     */
    private fun createNotification(): Notification {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("极行桌面")
            .setContentText("悬浮球服务运行中")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    /**
     * 创建通知频道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "悬浮球服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "悬浮球返回桌面服务"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 工具方法
     */
    private fun getScreenWidth(): Int = resources.displayMetrics.widthPixels
    private fun getScreenHeight(): Int = resources.displayMetrics.heightPixels
    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        floatingBallView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove floating ball", e)
            }
        }
        floatingBallView = null
        Log.i(TAG, "FloatingBallService destroyed")
    }
}
