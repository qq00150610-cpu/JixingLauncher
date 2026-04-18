package com.jixing.launcher.ui.factory

import androidx.lifecycle.ViewModel
import com.jixing.launcher.managers.VehicleStateManager
import com.jixing.launcher.model.VehicleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * 工厂模式 ViewModel
 */
@HiltViewModel
class FactoryViewModel @Inject constructor(
    private val vehicleStateManager: VehicleStateManager
) : ViewModel() {

    val vehicleState: StateFlow<VehicleState> = vehicleStateManager.vehicleState

    fun startMonitoring() {
        vehicleStateManager.startMonitoring()
    }

    fun stopMonitoring() {
        vehicleStateManager.stopMonitoring()
    }
}
