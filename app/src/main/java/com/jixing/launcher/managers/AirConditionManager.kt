package com.jixing.launcher.managers

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.jixing.launcher.model.AirConditionState
import com.jixing.launcher.model.AirConditionMode

/**
 * 空调控制管理器
 */
class AirConditionManager(context: Context) {

    private val _airConditionState = MutableStateFlow(AirConditionState())
    val airConditionState: StateFlow<AirConditionState> = _airConditionState.asStateFlow()

    private val _leftTemperature = MutableStateFlow(24)
    val leftTemperature: StateFlow<Int> = _leftTemperature.asStateFlow()

    private val _rightTemperature = MutableStateFlow(24)
    val rightTemperature: StateFlow<Int> = _rightTemperature.asStateFlow()

    fun togglePower() {
        _airConditionState.value = _airConditionState.value.copy(
            isEnabled = !_airConditionState.value.isEnabled
        )
    }

    fun setTemperature(temp: Int) {
        val clampedTemp = temp.coerceIn(16, 30)
        _airConditionState.value = _airConditionState.value.copy(temperature = clampedTemp)
        _leftTemperature.value = clampedTemp
        _rightTemperature.value = clampedTemp
    }

    fun setLeftTemperature(temp: Int) {
        _leftTemperature.value = temp.coerceIn(16, 30)
        if (_airConditionState.value.syncOn) {
            _rightTemperature.value = temp
        }
    }

    fun setRightTemperature(temp: Int) {
        _rightTemperature.value = temp.coerceIn(16, 30)
        if (_airConditionState.value.syncOn) {
            _leftTemperature.value = temp
        }
    }

    fun setFanSpeed(speed: Int) {
        _airConditionState.value = _airConditionState.value.copy(
            fanSpeed = speed.coerceIn(0, 7)
        )
    }

    fun increaseFanSpeed() {
        val current = _airConditionState.value.fanSpeed
        if (current < 7) {
            setFanSpeed(current + 1)
        }
    }

    fun decreaseFanSpeed() {
        val current = _airConditionState.value.fanSpeed
        if (current > 0) {
            setFanSpeed(current - 1)
        }
    }

    fun setMode(mode: AirConditionMode) {
        val state = _airConditionState.value
        _airConditionState.value = state.copy(
            mode = mode,
            acOn = mode == AirConditionMode.COOL,
            heatOn = mode == AirConditionMode.HEAT,
            autoOn = mode == AirConditionMode.AUTO
        )
    }

    fun toggleAC() {
        _airConditionState.value = _airConditionState.value.copy(
            acOn = !_airConditionState.value.acOn
        )
    }

    fun toggleHeat() {
        _airConditionState.value = _airConditionState.value.copy(
            heatOn = !_airConditionState.value.heatOn
        )
    }

    fun toggleSync() {
        _airConditionState.value = _airConditionState.value.copy(
            syncOn = !_airConditionState.value.syncOn
        )
        if (_airConditionState.value.syncOn) {
            _rightTemperature.value = _leftTemperature.value
        }
    }

    fun toggleFrontDefrost() {
        _airConditionState.value = _airConditionState.value.copy(
            frontDefrost = !_airConditionState.value.frontDefrost
        )
    }

    fun toggleRearDefrost() {
        _airConditionState.value = _airConditionState.value.copy(
            rearDefrost = !_airConditionState.value.rearDefrost
        )
    }

    fun setAutoMode(enabled: Boolean) {
        _airConditionState.value = _airConditionState.value.copy(
            autoOn = enabled,
            mode = if (enabled) AirConditionMode.AUTO else _airConditionState.value.mode
        )
    }

    fun reset() {
        _airConditionState.value = AirConditionState()
        _leftTemperature.value = 24
        _rightTemperature.value = 24
    }
}
