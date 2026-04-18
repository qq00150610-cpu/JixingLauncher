package com.jixing.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import com.jixing.launcher.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * 应用管理数据仓库
 */
class AppRepository(private val context: Context) {

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: Flow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _systemApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val systemApps: Flow<List<AppInfo>> = _systemApps.asStateFlow()

    private val _userApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val userApps: Flow<List<AppInfo>> = _userApps.asStateFlow()

    private var isLoaded = false

    suspend fun loadAllApps() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val allApps = packages.mapNotNull { appInfo ->
            try {
                val pkgInfo = getPackageInfo(appInfo.packageName)
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = getAppIcon(appInfo.packageName),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    isEnabled = pm.getApplicationEnabledSetting(appInfo.packageName) != 
                               PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    versionName = pkgInfo?.versionName ?: "",
                    installTime = pkgInfo?.firstInstallTime ?: 0,
                    updateTime = pkgInfo?.lastUpdateTime ?: 0,
                    apkPath = appInfo.sourceDir
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName }

        _installedApps.value = allApps
        _systemApps.value = allApps.filter { it.isSystemApp }
        _userApps.value = allApps.filter { !it.isSystemApp }
        isLoaded = true
    }

    private fun getAppIcon(packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    private fun getPackageInfo(packageName: String): android.content.pm.PackageInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun openAppSettings(packageName: String) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle exception
        }
    }

    suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.Default) {
        if (!isLoaded) loadAllApps()
        
        val lowerQuery = query.lowercase()
        _installedApps.value.filter { app ->
            app.appName.lowercase().contains(lowerQuery) ||
            app.packageName.lowercase().contains(lowerQuery)
        }
    }

    fun getAppInfo(packageName: String): AppInfo? {
        return _installedApps.value.find { it.packageName == packageName }
    }
}
