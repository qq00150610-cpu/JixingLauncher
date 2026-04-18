package com.jixing.launcher.utils

import android.view.KeyEvent

/**
 * 硬件按键处理工具 - 适配东风风神 Windlink X 车机
 */
object HardwareKeyHandler {

    interface OnKeyEventListener {
        fun onVolumeUp(): Boolean
        fun onVolumeDown(): Boolean
        fun onHome(): Boolean
        fun onBack(): Boolean
        fun onVoice(): Boolean
        fun onHvac(): Boolean
        fun onUnknown(keyCode: Int): Boolean
    }

    fun handleKeyEvent(keyCode: Int, listener: OnKeyEventListener): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> listener.onVolumeUp()
            KeyEvent.KEYCODE_VOLUME_DOWN -> listener.onVolumeDown()
            KeyEvent.KEYCODE_HOME -> listener.onHome()
            KeyEvent.KEYCODE_BACK -> listener.onBack()
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> listener.onVoice()
            KeyEvent.KEYCODE_MENU -> listener.onHvac()
            else -> listener.onUnknown(keyCode)
        }
    }

    fun isVolumeKey(keyCode: Int) = keyCode in listOf(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN)

    fun isNavigationKey(keyCode: Int) = keyCode in listOf(
        KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_APP_SWITCH
    )

    fun getKeyName(keyCode: Int): String = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP -> "音量+"
        KeyEvent.KEYCODE_VOLUME_DOWN -> "音量-"
        KeyEvent.KEYCODE_HOME -> "Home"
        KeyEvent.KEYCODE_BACK -> "返回"
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "语音"
        KeyEvent.KEYCODE_MENU -> "菜单/空调"
        KeyEvent.KEYCODE_DPAD_UP -> "上"
        KeyEvent.KEYCODE_DPAD_DOWN -> "下"
        KeyEvent.KEYCODE_DPAD_LEFT -> "左"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "右"
        KeyEvent.KEYCODE_DPAD_CENTER -> "确认"
        else -> "未知($keyCode)"
    }
}
