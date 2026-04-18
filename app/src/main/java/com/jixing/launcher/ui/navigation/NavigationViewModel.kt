package com.jixing.launcher.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 导航 ViewModel
 */
@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    private val _currentDestination = MutableStateFlow<String?>(null)
    val currentDestination: StateFlow<String?> = _currentDestination.asStateFlow()

    private val _eta = MutableStateFlow<String?>(null)
    val eta: StateFlow<String?> = _eta.asStateFlow()

    private val _distance = MutableStateFlow<String?>(null)
    val distance: StateFlow<String?> = _distance.asStateFlow()

    fun startNavigation(destination: String) {
        _currentDestination.value = destination
        _isNavigating.value = true
        _eta.value = "25 分钟"
        _distance.value = "12.5 公里"
    }

    fun stopNavigation() {
        _isNavigating.value = false
        _currentDestination.value = null
        _eta.value = null
        _distance.value = null
    }
}
