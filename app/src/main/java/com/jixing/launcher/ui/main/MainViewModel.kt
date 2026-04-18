package com.jixing.launcher.ui.main

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
 * 主桌面 ViewModel
 */
@HiltViewModel
class MainViewModel @Inject constructor(
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val filteredApps: StateFlow<List<AppInfo>> = _filteredApps.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                appRepository.loadAllApps()
                _allApps.value = appRepository.installedApps.let { flow ->
                    val apps = mutableListOf<AppInfo>()
                    flow.collect { apps.addAll(it) }
                    apps
                }
                _userApps.value = _allApps.value.filter { !it.isSystemApp }
                _systemApps.value = _allApps.value.filter { it.isSystemApp }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchApps(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isEmpty()) {
                _filteredApps.value = _allApps.value
            } else {
                _filteredApps.value = appRepository.searchApps(query)
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
