package com.jixing.launcher.ui.floating

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 悬浮球 ViewModel
 */
@HiltViewModel
class FloatingBallViewModel @Inject constructor() : ViewModel() {

    private val _isVisible = MutableStateFlow(true)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    private val _position = MutableStateFlow(Pair(100, 300))
    val position: StateFlow<Pair<Int, Int>> = _position.asStateFlow()

    fun show() {
        _isVisible.value = true
    }

    fun hide() {
        _isVisible.value = false
    }

    fun updatePosition(x: Int, y: Int) {
        _position.value = Pair(x, y)
    }

    fun snapToEdge(screenWidth: Int, ballWidth: Int) {
        val currentX = _position.value.first
        val threshold = screenWidth / 2
        
        val newX = if (currentX < threshold) {
            32 // 左边距
        } else {
            screenWidth - ballWidth - 32 // 右边距
        }
        
        _position.value = Pair(newX, _position.value.second)
    }
}
