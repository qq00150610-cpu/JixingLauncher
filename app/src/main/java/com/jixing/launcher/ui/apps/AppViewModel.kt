package com.jixing.launcher.ui.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jixing.launcher.data.repository.AppRepository
import com.jixing.launcher.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用管理器 ViewModel
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application,
    private val appRepository: AppRepository
) : AndroidViewModel(application) {

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _userApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val userApps: StateFlow<List<AppInfo>> = _userApps.asStateFlow()

    private val _systemApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val systemApps: StateFlow<List<AppInfo>> = _systemApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                appRepository.loadAllApps()
                appRepository.installedApps.collect { apps ->
                    _allApps.value = apps
                    _userApps.value = apps.filter { !it.isSystemApp }
                    _systemApps.value = apps.filter { it.isSystemApp }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchApps(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadApps()
            } else {
                val results = appRepository.searchApps(query)
                _allApps.value = results
                _userApps.value = results.filter { !it.isSystemApp }
                _systemApps.value = results.filter { it.isSystemApp }
            }
        }
    }

    fun launchApp(packageName: String): Boolean {
        return appRepository.launchApp(packageName)
    }

    fun openAppSettings(packageName: String) {
        appRepository.openAppSettings(packageName)
    }
}
