package com.jixing.launcher.ui.settings

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jixing.launcher.model.JixingColors
import com.jixing.launcher.ui.settings.viewmodel.AboutDeviceViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 关于设备 Activity
 */
@AndroidEntryPoint
class AboutDeviceActivity : ComponentActivity() {
    private val viewModel: AboutDeviceViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            AboutDeviceScreen(
                viewModel = viewModel,
                onBack = { finish() }
            )
        }
    }

    private fun setupFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
fun AboutDeviceScreen(viewModel: AboutDeviceViewModel, onBack: () -> Unit) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    val memoryInfo by viewModel.memoryInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JixingColors.BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // 顶部栏
            AboutDeviceTopBar(onBack = onBack)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = JixingColors.PrimaryBlue)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 应用信息卡片
                    item {
                        AppInfoCard()
                    }
                    
                    // 设备信息卡片
                    item {
                        DeviceInfoCard(deviceInfo = deviceInfo)
                    }
                    
                    // 存储信息卡片
                    item {
                        StorageInfoCard(storageInfo = storageInfo, viewModel = viewModel)
                    }
                    
                    // 内存信息卡片
                    item {
                        MemoryInfoCard(memoryInfo = memoryInfo, viewModel = viewModel)
                    }
                    
                    // 版权信息
                    item {
                        CopyrightCard()
                    }
                }
            }
        }
    }
}

@Composable
fun AboutDeviceTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Default.ArrowBack, 
                "返回", 
                tint = JixingColors.TextPrimaryDark
            )
        }
        Text(
            "关于设备", 
            fontSize = 20.sp, 
            color = JixingColors.TextPrimaryDark,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun AppInfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 应用图标
            Icon(
                Icons.Default.Android,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = JixingColors.PrimaryBlue
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "极行桌面",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = JixingColors.TextPrimaryDark
            )
            
            Text(
                "Jixing Launcher",
                fontSize = 14.sp,
                color = JixingColors.TextSecondaryDark
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 版本信息
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(label = "版本", value = "1.0.0")
                InfoChip(label = "构建", value = "2024.04.14")
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "专为东风风神 Windlink X 1.5 系统优化",
                fontSize = 12.sp,
                color = JixingColors.TextSecondaryDark
            )
        }
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Surface(
        color = JixingColors.SurfaceDark,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$label: ",
                fontSize = 12.sp,
                color = JixingColors.TextSecondaryDark
            )
            Text(
                value,
                fontSize = 12.sp,
                color = JixingColors.PrimaryBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DeviceInfoCard(deviceInfo: com.jixing.launcher.model.DeviceInfo?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "设备信息",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = JixingColors.TextPrimaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            deviceInfo?.let { info ->
                InfoRow("设备型号", info.deviceModel)
                InfoRow("Windlink 版本", info.windlinkVersion)
                InfoRow("Android 版本", info.androidVersion)
                InfoRow("内核版本", info.kernelVersion)
                InfoRow("构建编号", info.buildNumber)
                InfoRow("序列号", info.serialNumber)
            } ?: InfoRow("状态", "正在加载...")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = JixingColors.TextSecondaryDark
        )
        Text(
            value,
            fontSize = 14.sp,
            color = JixingColors.TextPrimaryDark
        )
    }
}

@Composable
fun StorageInfoCard(
    storageInfo: com.jixing.launcher.model.StorageInfo?,
    viewModel: AboutDeviceViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "存储空间",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = JixingColors.TextPrimaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            storageInfo?.let { info ->
                // 存储进度条
                val usagePercent = if (info.totalBytes > 0) {
                    (info.usedBytes.toFloat() / info.totalBytes * 100).toInt()
                } else 0
                
                LinearProgressIndicator(
                    progress = { usagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = JixingColors.PrimaryBlue,
                    trackColor = JixingColors.SurfaceDark
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoRow("总容量", viewModel.formatStorageSize(info.totalBytes))
                InfoRow("已使用", viewModel.formatStorageSize(info.usedBytes))
                InfoRow("可用空间", viewModel.formatStorageSize(info.availableBytes))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "已使用 $usagePercent%",
                    fontSize = 12.sp,
                    color = if (usagePercent > 90) JixingColors.ErrorRed else JixingColors.TextSecondaryDark
                )
            } ?: InfoRow("状态", "正在加载...")
        }
    }
}

@Composable
fun MemoryInfoCard(
    memoryInfo: com.jixing.launcher.model.MemoryInfo?,
    viewModel: AboutDeviceViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "运行内存",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = JixingColors.TextPrimaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            memoryInfo?.let { info ->
                val usagePercent = if (info.totalRam > 0) {
                    ((info.totalRam - info.availableRam).toFloat() / info.totalRam * 100).toInt()
                } else 0
                
                LinearProgressIndicator(
                    progress = { usagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = JixingColors.AccentGreen,
                    trackColor = JixingColors.SurfaceDark
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoRow("总内存", viewModel.formatStorageSize(info.totalRam))
                InfoRow("可用内存", viewModel.formatStorageSize(info.availableRam))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "当前使用 $usagePercent%",
                    fontSize = 12.sp,
                    color = if (usagePercent > 80) JixingColors.WarningOrange else JixingColors.TextSecondaryDark
                )
            } ?: InfoRow("状态", "正在加载...")
        }
    }
}

@Composable
fun CopyrightCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "© 2024 极行桌面",
                fontSize = 14.sp,
                color = JixingColors.TextSecondaryDark
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "Powered by Jetpack Compose",
                fontSize = 12.sp,
                color = JixingColors.TextSecondaryDark.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "隐私政策",
                    fontSize = 12.sp,
                    color = JixingColors.PrimaryBlue
                )
                Text(
                    "|",
                    fontSize = 12.sp,
                    color = JixingColors.TextSecondaryDark.copy(alpha = 0.5f)
                )
                Text(
                    "用户协议",
                    fontSize = 12.sp,
                    color = JixingColors.PrimaryBlue
                )
            }
        }
    }
}
