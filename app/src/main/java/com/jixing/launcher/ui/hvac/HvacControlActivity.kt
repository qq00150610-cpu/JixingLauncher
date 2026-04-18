package com.jixing.launcher.ui.hvac

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jixing.launcher.managers.AirConditionManager
import com.jixing.launcher.model.AirConditionMode
import com.jixing.launcher.model.AirConditionState
import com.jixing.launcher.ui.theme.JixingColors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 空调控制 Activity
 */
@AndroidEntryPoint
class HvacControlActivity : ComponentActivity() {
    
    @Inject
    lateinit var airConditionManager: AirConditionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            HvacScreen(
                airConditionManager = airConditionManager,
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
fun HvacScreen(
    airConditionManager: AirConditionManager,
    onBack: () -> Unit
) {
    val state by airConditionManager.airConditionState.collectAsState()
    val leftTemp by airConditionManager.leftTemperature.collectAsState()
    val rightTemp by airConditionManager.rightTemperature.collectAsState()
    
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
                Text("空调控制", fontSize = 20.sp, color = JixingColors.TextPrimaryDark)
                IconButton(onClick = { airConditionManager.reset() }) {
                    Icon(Icons.Default.Refresh, "重置", tint = JixingColors.TextPrimaryDark)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 电源开关 - 使用自定义开关按钮
            PowerToggleButton(
                checked = state.isEnabled,
                onCheckedChange = { airConditionManager.togglePower() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 温度控制区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TemperatureControl(
                    label = "左侧",
                    temperature = leftTemp,
                    onIncrease = { airConditionManager.setLeftTemperature(leftTemp + 1) },
                    onDecrease = { airConditionManager.setLeftTemperature(leftTemp - 1) }
                )
                
                // 同步按钮
                IconButton(
                    onClick = { airConditionManager.toggleSync() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.syncOn) JixingColors.PrimaryBlue.copy(alpha = 0.2f)
                            else JixingColors.CardDark
                        )
                ) {
                    Icon(
                        Icons.Default.Sync,
                        "同步",
                        tint = if (state.syncOn) JixingColors.PrimaryBlue else JixingColors.TextSecondaryDark
                    )
                }
                
                TemperatureControl(
                    label = "右侧",
                    temperature = rightTemp,
                    onIncrease = { airConditionManager.setRightTemperature(rightTemp + 1) },
                    onDecrease = { airConditionManager.setRightTemperature(rightTemp - 1) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 模式选择
            Text("模式", fontSize = 16.sp, color = JixingColors.TextSecondaryDark)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeButton(
                    icon = Icons.Default.Air,
                    label = "自动",
                    selected = state.mode == AirConditionMode.AUTO,
                    onClick = { airConditionManager.setMode(AirConditionMode.AUTO) },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    icon = Icons.Default.AcUnit,
                    label = "制冷",
                    selected = state.mode == AirConditionMode.COOL,
                    onClick = { airConditionManager.setMode(AirConditionMode.COOL) },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    icon = Icons.Default.Whatshot,
                    label = "制热",
                    selected = state.mode == AirConditionMode.HEAT,
                    onClick = { airConditionManager.setMode(AirConditionMode.HEAT) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeButton(
                    icon = Icons.Default.Loop,
                    label = "通风",
                    selected = state.mode == AirConditionMode.VENT,
                    onClick = { airConditionManager.setMode(AirConditionMode.VENT) },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    icon = Icons.Default.Dehaze,
                    label = "除雾",
                    selected = state.mode == AirConditionMode.DEFROST,
                    onClick = { airConditionManager.setMode(AirConditionMode.DEFROST) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 风速控制
            Text("风速", fontSize = 16.sp, color = JixingColors.TextSecondaryDark)
            Spacer(modifier = Modifier.height(12.dp))
            
            FanSpeedControl(
                fanSpeed = state.fanSpeed,
                onIncrease = { airConditionManager.increaseFanSpeed() },
                onDecrease = { airConditionManager.decreaseFanSpeed() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 快捷功能
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Circle,
                    label = "前除霜",
                    isActive = state.frontDefrost,
                    onClick = { airConditionManager.toggleFrontDefrost() },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Circle,
                    label = "后除霜",
                    isActive = state.rearDefrost,
                    onClick = { airConditionManager.toggleRearDefrost() },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.AcUnit,
                    label = "AC",
                    isActive = state.acOn,
                    onClick = { airConditionManager.toggleAC() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TemperatureControl(
    label: String,
    temperature: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 14.sp, color = JixingColors.TextSecondaryDark)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${temperature}°",
            fontSize = 48.sp,
            color = JixingColors.TextPrimaryDark
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(JixingColors.CardDark)
            ) {
                Icon(Icons.Default.Remove, "降低", tint = JixingColors.TextPrimaryDark)
            }
            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(JixingColors.CardDark)
            ) {
                Icon(Icons.Default.Add, "升高", tint = JixingColors.TextPrimaryDark)
            }
        }
    }
}

@Composable
fun ModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) JixingColors.PrimaryBlue else JixingColors.CardDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (selected) Color.White else JixingColors.TextSecondaryDark
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (selected) Color.White else JixingColors.TextSecondaryDark
            )
        }
    }
}

@Composable
fun FanSpeedControl(
    fanSpeed: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrease,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(JixingColors.CardDark)
        ) {
            Icon(Icons.Default.Remove, "减小风速", tint = JixingColors.TextPrimaryDark)
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(7) { index ->
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index < fanSpeed) JixingColors.PrimaryBlue
                            else JixingColors.SurfaceDark
                        )
                )
            }
        }
        
        Text(
            text = "$fanSpeed",
            fontSize = 24.sp,
            color = JixingColors.TextPrimaryDark,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        IconButton(
            onClick = onIncrease,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(JixingColors.CardDark)
        ) {
            Icon(Icons.Default.Add, "增大风速", tint = JixingColors.TextPrimaryDark)
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(64.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) JixingColors.AccentAmber.copy(alpha = 0.2f) else JixingColors.CardDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isActive) JixingColors.AccentAmber else JixingColors.TextSecondaryDark,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (isActive) JixingColors.AccentAmber else JixingColors.TextSecondaryDark
            )
        }
    }
}

/**
 * 电源开关按钮 - 自定义组件替代 CenterAlignedToggleButton
 */
@Composable
fun PowerToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = if (checked) JixingColors.PrimaryBlue else JixingColors.CardDark
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                if (checked) Icons.Default.PowerSettingsNew else Icons.Default.Power,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (checked) "关闭空调" else "开启空调",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}
