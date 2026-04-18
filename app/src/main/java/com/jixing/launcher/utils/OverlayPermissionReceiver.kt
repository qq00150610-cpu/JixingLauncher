package com.jixing.launcher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jixing.launcher.services.FloatingBallService

/**
 * 开机自启和权限接收器
 */
class OverlayPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // 开机完成后启动悬浮球服务
                val serviceIntent = Intent(context, FloatingBallService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
