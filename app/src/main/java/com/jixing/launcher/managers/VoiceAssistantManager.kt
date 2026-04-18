package com.jixing.launcher.managers

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.jixing.launcher.model.VoiceCommand
import com.jixing.launcher.model.VoiceCommandType

/**
 * 语音助手管理器
 */
class VoiceAssistantManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _lastCommand = MutableStateFlow<VoiceCommand?>(null)
    val lastCommand: StateFlow<VoiceCommand?> = _lastCommand.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    var onCommandRecognized: ((VoiceCommand) -> Unit)? = null

    init {
        checkAvailability()
    }

    private fun checkAvailability() {
        _isAvailable.value = SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (!_isAvailable.value) {
            return
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(createRecognitionListener())
            }

            val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun speak(text: String) {
        // 文字转语音功能
        // 实际实现需要 TextToSpeech
        _isSpeaking.value = true
        // 模拟语音播放完成
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _isSpeaking.value = false
        }, 2000)
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                processVoiceCommand(matches?.firstOrNull() ?: "")
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                // 处理部分结果
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun processVoiceCommand(command: String) {
        if (command.isEmpty()) return

        val commandType = analyzeCommandType(command)
        val voiceCommand = VoiceCommand(
            command = command,
            type = commandType,
            response = generateResponse(command, commandType),
            confidence = 0.95f
        )

        _lastCommand.value = voiceCommand
        onCommandRecognized?.invoke(voiceCommand)
    }

    private fun analyzeCommandType(command: String): VoiceCommandType {
        return when {
            command.contains("导航") || command.contains("去") || command.contains("路线") -> VoiceCommandType.NAVIGATION
            command.contains("音乐") || command.contains("播放") || command.contains("暂停") -> VoiceCommandType.MEDIA
            command.contains("空调") || command.contains("温度") || command.contains("冷") || command.contains("热") -> VoiceCommandType.HVAC
            command.contains("打开") || command.contains("启动") -> VoiceCommandType.APP
            else -> VoiceCommandType.UNKNOWN
        }
    }

    private fun generateResponse(command: String, type: VoiceCommandType): String {
        return when (type) {
            VoiceCommandType.NAVIGATION -> "好的，正在为您规划路线"
            VoiceCommandType.MEDIA -> "已为您${if (command.contains("暂停")) "暂停" else "播放"}"
            VoiceCommandType.HVAC -> "好的，正在调节空调"
            VoiceCommandType.APP -> "正在打开应用"
            VoiceCommandType.PHONE -> "好的，正在拨打电话"
            VoiceCommandType.SYSTEM -> "已为您调整系统设置"
            VoiceCommandType.UNKNOWN -> "抱歉，我没有理解您的意思"
        }
    }

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
