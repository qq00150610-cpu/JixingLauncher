package com.jixing.launcher.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jixing.launcher.model.AdbStatus
import com.jixing.launcher.model.AdbConnectionStatus
import com.jixing.launcher.model.JixingColors
import com.jixing.launcher.model.UsbConfig
import com.jixing.launcher.ui.settings.viewmodel.DeveloperOptionsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 开发者选项 Activity
 */
@AndroidEntryPoint
class DeveloperOptionsActivity : ComponentActivity() {
    private val viewModel: DeveloperOptionsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            DeveloperOptionsScreen(
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
fun DeveloperOptionsScreen(viewModel: DeveloperOptionsViewModel, onBack: () -> Unit) {
    val adbStatus by viewModel.adbStatus.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val usbState by viewModel.usbState.collectAsState()
    val authorizedComputers by viewModel.authorizedComputers.collectAsState()
    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val context = LocalContext.current
    
    // 显示Toast消息
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            // 清除消息
        }
    }
    
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
            DeveloperOptionsTopBar(onBack = onBack)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ADB 状态概览卡片
                item {
                    AdbStatusOverviewCard(
                        adbStatus = adbStatus,
                        connectionStatus = connectionStatus,
                        ipAddress = "192.168.1.100", // 实际获取
                        port = 5555
                    )
                }
                
                // ADB 控制卡片
                item {
                    AdbControlCard(
                        adbStatus = adbStatus,
                        onToggle = { viewModel.toggleAdb() },
                        onEnableWireless = { viewModel.enableWirelessAdb() },
                        isLoading = isLoading
                    )
                }
                
                // USB 配置卡片
                item {
                    UsbConfigCard(
                        usbState = usbState,
                        onUsbModeChange = { viewModel.setUsbMode(it) }
                    )
                }
                
                // 授权设备卡片
                item {
                    AuthorizedDevicesCard(
                        authorizedComputers = authorizedComputers,
                        onRevoke = { viewModel.revokeAuth(it) },
                        onRevokeAll = { viewModel.revokeAllAuth() }
                    )
                }
                
                // 系统调试卡片
                item {
                    SystemDebugCard(
                        onOpenUsageAccess = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        onOpenOverlay = {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                        },
                        onOpenDevSettings = {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DeveloperOptionsTopBar(onBack: () -> Unit) {
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
            "开发者选项", 
            fontSize = 20.sp, 
            color = JixingColors.TextPrimaryDark,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = { /* 刷新状态 */ }) {
            Icon(
                Icons.Default.Refresh,
                "刷新",
                tint = JixingColors.PrimaryBlue
            )
        }
    }
}

@Composable
fun AdbStatusOverviewCard(
    adbStatus: AdbStatus,
    connectionStatus: AdbConnectionStatus,
    ipAddress: String,
    port: Int
) {
    val statusColor = if (adbStatus.isEnabled) JixingColors.AccentGreen else JixingColors.TextSecondaryDark
    
    val statusText = if (adbStatus.isEnabled) "已启用" else "已禁用"
    
    Card(
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DeveloperMode,
                        contentDescription = null,
                        tint = JixingColors.PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "ADB 调试状态",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JixingColors.TextPrimaryDark
                        )
                        Text(
                            statusText,
                            fontSize = 14.sp,
                            color = statusColor
                        )
                    }
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
            }
            
            if (adbStatus.isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = JixingColors.SurfaceDark)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(
                        icon = Icons.Default.Wifi,
                        label = "IP地址",
                        value = ipAddress
                    )
                    InfoItem(
                        icon = Icons.Default.SettingsEthernet,
                        label = "端口",
                        value = port.toString()
                    )
                    InfoItem(
                        icon = Icons.Default.Cable,
                        label = "连接",
                        value = when (connectionStatus) {
                            AdbConnectionStatus.CONNECTED -> "已连接"
                            AdbConnectionStatus.CONNECTING -> "连接中"
                            AdbConnectionStatus.DISCONNECTED -> "未连接"
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 连接命令
                Surface(
                    color = JixingColors.SurfaceDark,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "adb connect $ipAddress:$port",
                            fontSize = 13.sp,
                            color = JixingColors.AccentGreen,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "复制",
                            tint = JixingColors.TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = JixingColors.TextSecondaryDark, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = JixingColors.TextPrimaryDark)
        Text(label, fontSize = 11.sp, color = JixingColors.TextSecondaryDark)
    }
}

@Composable
fun AdbControlCard(
    adbStatus: AdbStatus,
    onToggle: () -> Unit,
    onEnableWireless: () -> Unit,
    isLoading: Boolean
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
                "ADB 控制",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = JixingColors.TextPrimaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // ADB 开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(JixingColors.SurfaceDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Usb,
                            contentDescription = null,
                            tint = JixingColors.PrimaryBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("USB 调试", fontSize = 14.sp, color = JixingColors.TextPrimaryDark)
                        Text("通过 USB 连接调试设备", fontSize = 12.sp, color = JixingColors.TextSecondaryDark)
                    }
                }
                Switch(
                    checked = adbStatus == AdbStatus.ENABLED,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JixingColors.PrimaryBlue,
                        checkedTrackColor = JixingColors.PrimaryBlue.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 无线调试按钮
            Button(
                onClick = onEnableWireless,
                enabled = !isLoading && adbStatus == AdbStatus.ENABLED,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JixingColors.PrimaryBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Wifi, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开启无线调试")
            }
            
            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = JixingColors.PrimaryBlue
                )
            }
        }
    }
}

@Composable
fun UsbConfigCard(
    usbState: com.jixing.launcher.model.UsbState,
    onUsbModeChange: (UsbConfig) -> Unit
) {
    val currentMode = usbState.config
    
    Card(
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "USB 配置",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = JixingColors.TextPrimaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UsbModeChip(
                    label = "充电",
                    selected = currentMode == UsbConfig.CHARGING,
                    onClick = { onUsbModeChange(UsbConfig.CHARGING) },
                    modifier = Modifier.weight(1f)
                )
                UsbModeChip(
                    label = "MTP",
                    selected = currentMode == UsbConfig.MTP,
                    onClick = { onUsbModeChange(UsbConfig.MTP) },
                    modifier = Modifier.weight(1f)
                )
                UsbModeChip(
                    label = "PTP",
                    selected = currentMode == UsbConfig.PTP,
                    onClick = { onUsbModeChange(UsbConfig.PTP) },
                    modifier = Modifier.weight(1f)
                )
                UsbModeChip(
                    label = "ADB",
                    selected = currentMode == UsbConfig.ADB,
                    onClick = { onUsbModeChange(UsbConfig.ADB) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun UsbModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (selected) JixingColors.PrimaryBlue else JixingColors.SurfaceDark,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) JixingColors.TextPrimaryDark else JixingColors.TextSecondaryDark,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun AuthorizedDevicesCard(
    authorizedComputers: List<com.jixing.launcher.model.AdbAuthInfo>,
    onRevoke: (String) -> Unit,
    onRevokeAll: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "已授权的计算机",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = JixingColors.TextPrimaryDark
                )
                if (authorizedComputers.isNotEmpty()) {
                    TextButton(onClick = onRevokeAll) {
                        Text("全部撤销", fontSize = 12.sp, color = JixingColors.ErrorRed)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (authorizedComputers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无已授权的计算机",
                        fontSize = 14.sp,
                        color = JixingColors.TextSecondaryDark
                    )
                }
            } else {
                authorizedComputers.forEach { computer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Computer,
                                contentDescription = null,
                                tint = JixingColors.TextSecondaryDark
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    computer.computerName,
                                    fontSize = 14.sp,
                                    color = JixingColors.TextPrimaryDark
                                )
                                Text(
                                    computer.ipAddress,
                                    fontSize = 12.sp,
                                    color = JixingColors.TextSecondaryDark
                                )
                            }
                        }
                        IconButton(onClick = { onRevoke(computer.computerName) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "撤销授权",
                                tint = JixingColors.ErrorRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemDebugCard(
    onOpenUsageAccess: () -> Unit,
    onOpenOverlay: () -> Unit,
    onOpenDevSettings: () -> Unit
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
                "系统调试",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = JixingColors.TextPrimaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            DebugItem(
                icon = Icons.Default.Visibility,
                title = "使用情况访问权限",
                subtitle = "检测前台应用需要此权限",
                onClick = onOpenUsageAccess
            )
            
            DebugItem(
                icon = Icons.Default.Layers,
                title = "悬浮窗权限",
                subtitle = "悬浮球功能需要此权限",
                onClick = onOpenOverlay
            )
            
            DebugItem(
                icon = Icons.Default.Settings,
                title = "开发者设置",
                subtitle = "打开系统开发者选项",
                onClick = onOpenDevSettings
            )
        }
    }
}

@Composable
fun DebugItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(JixingColors.SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = JixingColors.PrimaryBlue)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = JixingColors.TextPrimaryDark)
            Text(subtitle, fontSize = 12.sp, color = JixingColors.TextSecondaryDark)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = JixingColors.TextSecondaryDark
        )
    }
}
