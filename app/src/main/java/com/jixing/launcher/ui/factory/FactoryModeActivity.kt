package com.jixing.launcher.ui.factory

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jixing.launcher.managers.VehicleStateManager
import com.jixing.launcher.ui.theme.JixingColors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 工厂模式 Activity
 */
@AndroidEntryPoint
class FactoryModeActivity : ComponentActivity() {
    
    @Inject
    lateinit var vehicleStateManager: VehicleStateManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            FactoryModeScreen(
                vehicleStateManager = vehicleStateManager,
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
fun FactoryModeScreen(
    vehicleStateManager: VehicleStateManager,
    onBack: () -> Unit
) {
    val vehicleState by vehicleStateManager.vehicleState.collectAsState()
    
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = JixingColors.TextPrimaryDark)
                }
                Text("工厂模式", fontSize = 20.sp, color = JixingColors.TextPrimaryDark)
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 警告提示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = com.jixing.launcher.ui.theme.JixingColors.Error.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = com.jixing.launcher.ui.theme.JixingColors.Error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "警告：此模式仅供专业人员使用，误操作可能导致系统故障",
                        fontSize = 12.sp,
                        color = com.jixing.launcher.ui.theme.JixingColors.Error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 车辆状态
                item {
                    FactorySection(title = "车辆状态") {
                        FactoryItem(
                            icon = Icons.Default.Speed,
                            title = "车速",
                            value = "${vehicleState.speed.toInt()} km/h"
                        )
                        FactoryItem(
                            icon = Icons.Default.Build,
                            title = "引擎转速",
                            value = "${vehicleState.rpm.toInt()} RPM"
                        )
                        FactoryItem(
                            icon = Icons.Default.LocalGasStation,
                            title = "燃油量",
                            value = "${vehicleState.fuelLevel}%"
                        )
                        FactoryItem(
                            icon = Icons.Default.BatteryStd,
                            title = "电池电压",
                            value = "${vehicleState.batteryVoltage} V"
                        )
                        FactoryItem(
                            icon = Icons.Default.Thermostat,
                            title = "引擎温度",
                            value = "${vehicleState.engineTemperature}°C"
                        )
                    }
                }
                
                // 系统信息
                item {
                    FactorySection(title = "系统信息") {
                        FactoryItem(
                            icon = Icons.Default.Android,
                            title = "Android 版本",
                            value = android.os.Build.VERSION.RELEASE
                        )
                        FactoryItem(
                            icon = Icons.Default.PhoneAndroid,
                            title = "型号",
                            value = android.os.Build.MODEL
                        )
                        FactoryItem(
                            icon = Icons.Default.Memory,
                            title = "内核版本",
                            value = android.os.Build.VERSION.SDK_INT.toString()
                        )
                    }
                }
                
                // 传感器测试
                item {
                    FactorySection(title = "传感器测试") {
                        FactoryItem(
                            icon = Icons.Default.TouchApp,
                            title = "触摸屏",
                            value = "正常",
                            onClick = {}
                        )
                        FactoryItem(
                            icon = Icons.Default.VolumeUp,
                            title = "扬声器",
                            value = "正常",
                            onClick = {}
                        )
                        FactoryItem(
                            icon = Icons.Default.Mic,
                            title = "麦克风",
                            value = "正常",
                            onClick = {}
                        )
                        FactoryItem(
                            icon = Icons.Default.GpsFixed,
                            title = "GPS",
                            value = "正常",
                            onClick = {}
                        )
                    }
                }
                
                // 显示屏测试
                item {
                    FactorySection(title = "显示屏测试") {
                        FactoryItem(
                            icon = Icons.Default.BrightnessHigh,
                            title = "亮度测试",
                            value = "点击测试",
                            onClick = {}
                        )
                        FactoryItem(
                            icon = Icons.Default.ColorLens,
                            title = "颜色测试",
                            value = "点击测试",
                            onClick = {}
                        )
                    }
                }
                
                // 恢复出厂设置
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { /* 恢复出厂设置 */ },
                        colors = CardDefaults.cardColors(containerColor = com.jixing.launcher.ui.theme.JixingColors.Error.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = com.jixing.launcher.ui.theme.JixingColors.Error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "恢复出厂设置",
                                fontSize = 16.sp,
                                color = com.jixing.launcher.ui.theme.JixingColors.Error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FactorySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            color = JixingColors.TextSecondaryDark,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                content = content
            )
        }
    }
}

@Composable
fun FactoryItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = JixingColors.PrimaryBlue)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = JixingColors.TextPrimaryDark,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = JixingColors.TextSecondaryDark
        )
    }
}
