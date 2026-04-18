package com.jixing.launcher.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jixing.launcher.model.JixingColors
import com.jixing.launcher.model.SettingItem
import com.jixing.launcher.model.SettingType
import dagger.hilt.android.AndroidEntryPoint

/**
 * 设置 Activity
 */
@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            SettingsScreen(viewModel = viewModel, onBack = { finish() })
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
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    
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
            SettingsTopBar(onBack = onBack)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 显示设置
                item {
                    SettingsSection(title = "显示") {
                        SettingsItem(
                            icon = Icons.Default.DarkMode,
                            title = "深色模式",
                            subtitle = "开启深色主题减少眩光",
                            type = SettingType.SWITCH,
                            value = true,
                            onValueChange = { viewModel.updateSetting("dark_theme", it) }
                        )
                        SettingsItem(
                            icon = Icons.Default.Brightness6,
                            title = "屏幕亮度",
                            subtitle = "80%",
                            type = SettingType.SLIDER,
                            value = 80,
                            onValueChange = { viewModel.updateSetting("brightness", it) }
                        )
                    }
                }
                
                // 声音设置
                item {
                    SettingsSection(title = "声音") {
                        SettingsItem(
                            icon = Icons.Default.VolumeUp,
                            title = "系统音效",
                            subtitle = "按键音和提示音",
                            type = SettingType.SWITCH,
                            value = true,
                            onValueChange = { viewModel.updateSetting("sound", it) }
                        )
                        SettingsItem(
                            icon = Icons.Default.RecordVoiceOver,
                            title = "导航语音",
                            subtitle = "播报导航指令",
                            type = SettingType.SWITCH,
                            value = true,
                            onValueChange = { viewModel.updateSetting("nav_voice", it) }
                        )
                        SettingsItem(
                            icon = Icons.Default.VolumeDown,
                            title = "媒体音量",
                            subtitle = "50%",
                            type = SettingType.SLIDER,
                            value = 50,
                            onValueChange = { viewModel.updateSetting("media_volume", it) }
                        )
                    }
                }
                
                // 空调设置
                item {
                    SettingsSection(title = "空调") {
                        SettingsItem(
                            icon = Icons.Default.Sync,
                            title = "温度同步",
                            subtitle = "左右区域温度联动",
                            type = SettingType.SWITCH,
                            value = true,
                            onValueChange = { viewModel.updateSetting("ac_sync", it) }
                        )
                        SettingsItem(
                            icon = Icons.Default.Thermostat,
                            title = "温度单位",
                            subtitle = "摄氏度",
                            type = SettingType.SELECT,
                            value = "摄氏度",
                            onValueChange = {}
                        )
                    }
                }
                
                // 驾驶辅助
                item {
                    SettingsSection(title = "驾驶辅助") {
                        SettingsItem(
                            icon = Icons.Default.DirectionsCar,
                            title = "驾驶模式",
                            subtitle = "简化界面减少干扰",
                            type = SettingType.SWITCH,
                            value = false,
                            onValueChange = { viewModel.updateSetting("driving_mode", it) }
                        )
                        SettingsItem(
                            icon = Icons.Default.Mic,
                            title = "语音助手",
                            subtitle = "语音控制功能",
                            type = SettingType.SWITCH,
                            value = true,
                            onValueChange = { viewModel.updateSetting("voice_assistant", it) }
                        )
                        SettingsItem(
                            icon = Icons.Default.Circle,
                            title = "悬浮球",
                            subtitle = "快捷操作悬浮球",
                            type = SettingType.SWITCH,
                            value = true,
                            onValueChange = { viewModel.updateSetting("floating_ball", it) }
                        )
                    }
                }
                
                // 系统
                item {
                    SettingsSection(title = "系统") {
                        SettingsItem(
                            icon = Icons.Default.Android,
                            title = "开发者选项",
                            subtitle = "ADB调试、USB配置等",
                            type = SettingType.ACTION,
                            value = "",
                            onValueChange = { 
                                val intent = Intent(this@SettingsActivity, DeveloperOptionsActivity::class.java)
                                startActivity(intent)
                            }
                        )
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "关于",
                            subtitle = "版本 1.0.0",
                            type = SettingType.ACTION,
                            value = "",
                            onValueChange = { 
                                val intent = Intent(this@SettingsActivity, AboutDeviceActivity::class.java)
                                startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "返回", tint = JixingColors.TextPrimaryDark)
        }
        Text("设置", fontSize = 20.sp, color = JixingColors.TextPrimaryDark)
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun SettingsSection(
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
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    type: SettingType,
    value: Any,
    onValueChange: (Any) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = type == SettingType.ACTION) { onValueChange(Unit) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(JixingColors.SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = JixingColors.PrimaryBlue)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = JixingColors.TextPrimaryDark)
            Text(subtitle, fontSize = 12.sp, color = JixingColors.TextSecondaryDark)
        }
        
        when (type) {
            SettingType.SWITCH -> {
                Switch(
                    checked = value as Boolean,
                    onCheckedChange = { onValueChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JixingColors.PrimaryBlue,
                        checkedTrackColor = JixingColors.PrimaryBlue.copy(alpha = 0.5f)
                    )
                )
            }
            SettingType.SLIDER -> {
                LinearProgressIndicator(
                    progress = { (value as Int) / 100f },
                    modifier = Modifier.width(100.dp),
                    color = JixingColors.PrimaryBlue,
                    trackColor = JixingColors.SurfaceDark
                )
            }
            SettingType.SELECT, SettingType.ACTION -> {
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = JixingColors.TextSecondaryDark
                )
            }
            else -> {}
        }
    }
}
