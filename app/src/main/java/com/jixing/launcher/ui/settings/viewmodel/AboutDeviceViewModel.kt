package com.jixing.launcher.ui.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jixing.launcher.model.DeviceInfo
import com.jixing.launcher.model.StorageInfo
import com.jixing.launcher.model.MemoryInfo
import com.jixing.launcher.utils.DeviceInfoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 关于设备 ViewModel
 */
@HiltViewModel
class AboutDeviceViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    private val _storageInfo = MutableStateFlow<StorageInfo?>(null)
    val storageInfo: StateFlow<StorageInfo?> = _storageInfo.asStateFlow()

    private val _memoryInfo = MutableStateFlow<MemoryInfo?>(null)
    val memoryInfo: StateFlow<MemoryInfo?> = _memoryInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDeviceInfo()
    }

    fun loadDeviceInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val context = getApplication<Application>()
                _deviceInfo.value = DeviceInfoUtils.getDeviceInfo(context)
                
                val total = DeviceInfoUtils.getTotalStorage()
                val used = DeviceInfoUtils.getUsedStorage()
                val available = DeviceInfoUtils.getAvailableStorage()
                _storageInfo.value = StorageInfo(total, used, available)
                
                val ramTotal = DeviceInfoUtils.getTotalRam(context)
                val ramAvailable = DeviceInfoUtils.getAvailableRam(context)
                _memoryInfo.value = MemoryInfo(ramTotal, ramAvailable)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshInfo() {
        loadDeviceInfo()
    }

    fun formatStorageSize(bytes: Long): String = DeviceInfoUtils.formatFileSize(bytes)
}
