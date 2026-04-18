package com.jixing.launcher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.jixing.launcher.utils.HardwareKeyHandler

/**
 * 硬件按键广播接收器 - Windlink X 车机适配
 */
class HardwareKeyReceiver : BroadcastReceiver() {

    var onVolumeUp: (() -> Unit)? = null
    var onVolumeDown: (() -> Unit)? = null
    var onHome: (() -> Unit)? = null
    var onBack: (() -> Unit)? = null
    var onVoice: (() -> Unit)? = null
    var onHvac: (() -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            keyEvent?.let {
                if (it.action == KeyEvent.ACTION_DOWN) {
                    handleKey(it.keyCode)
                }
            }
        }
    }

    private fun handleKey(keyCode: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> onVolumeUp?.invoke()
            KeyEvent.KEYCODE_VOLUME_DOWN -> onVolumeDown?.invoke()
            KeyEvent.KEYCODE_HOME -> onHome?.invoke()
            KeyEvent.KEYCODE_BACK -> onBack?.invoke()
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> onVoice?.invoke()
            KeyEvent.KEYCODE_MENU -> onHvac?.invoke()
        }
    }

    companion object {
        fun createListener(
            onVolumeUp: (() -> Unit)? = null,
            onVolumeDown: (() -> Unit)? = null,
            onHome: (() -> Unit)? = null,
            onBack: (() -> Unit)? = null,
            onVoice: (() -> Unit)? = null,
            onHvac: (() -> Unit)? = null
        ): HardwareKeyHandler.OnKeyEventListener {
            return object : HardwareKeyHandler.OnKeyEventListener {
                override fun onVolumeUp() = onVolumeUp?.invoke().let { true }
                override fun onVolumeDown() = onVolumeDown?.invoke().let { true }
                override fun onHome() = onHome?.invoke().let { true }
                override fun onBack() = onBack?.invoke().let { true }
                override fun onVoice() = onVoice?.invoke().let { true }
                override fun onHvac() = onHvac?.invoke().let { true }
                override fun onUnknown(keyCode: Int) = false
            }
        }
    }
}
