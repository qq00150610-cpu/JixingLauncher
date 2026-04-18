package com.jixing.launcher.ui.voice

import androidx.lifecycle.ViewModel
import com.jixing.launcher.model.VoiceCommand
import com.jixing.launcher.model.VoiceCommandType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 语音助手 ViewModel
 */
@HiltViewModel
class VoiceViewModel @Inject constructor() : ViewModel() {

    private val _conversation = MutableStateFlow<List<VoiceCommand>>(emptyList())
    val conversation: StateFlow<List<VoiceCommand>> = _conversation.asStateFlow()

    fun addCommand(command: String, type: VoiceCommandType) {
        val voiceCommand = VoiceCommand(
            command = command,
            type = type,
            response = generateResponse(type),
            confidence = 1.0f
        )
        _conversation.value = _conversation.value + voiceCommand
    }

    fun clearConversation() {
        _conversation.value = emptyList()
    }

    private fun generateResponse(type: VoiceCommandType): String {
        return when (type) {
            VoiceCommandType.NAVIGATION -> "好的，正在为您规划路线"
            VoiceCommandType.MEDIA -> "正在为您播放音乐"
            VoiceCommandType.HVAC -> "好的，正在调节温度"
            VoiceCommandType.APP -> "正在打开应用"
            VoiceCommandType.PHONE -> "好的，正在拨打电话"
            VoiceCommandType.SYSTEM -> "已为您调整系统设置"
            VoiceCommandType.UNKNOWN -> "抱歉，我没有理解您的意思"
        }
    }
}
