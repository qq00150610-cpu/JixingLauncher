package com.jixing.launcher.ui.navigation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import com.jixing.launcher.ui.theme.JixingColors
import dagger.hilt.android.AndroidEntryPoint

/**
 * 悬浮导航 Activity
 */
@AndroidEntryPoint
class FloatingNavActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            NavigationScreen(onClose = { finish() })
        }
    }

    private fun setupFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

@Composable
fun NavigationScreen(onClose: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    
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
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = JixingColors.TextPrimaryDark
                    )
                }
                Text(
                    text = "导航",
                    fontSize = 20.sp,
                    color = JixingColors.TextPrimaryDark
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索目的地", color = JixingColors.TextTertiaryDark) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = JixingColors.TextSecondaryDark)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = JixingColors.TextPrimaryDark,
                    unfocusedTextColor = JixingColors.TextPrimaryDark,
                    focusedBorderColor = JixingColors.PrimaryBlue,
                    unfocusedBorderColor = JixingColors.CardDark,
                    cursorColor = JixingColors.PrimaryBlue
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 快捷目的地
            Text(
                text = "快捷目的地",
                fontSize = 18.sp,
                color = JixingColors.TextPrimaryDark
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 目的地列表
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DestinationItem(
                    icon = Icons.Default.Home,
                    title = "回家",
                    subtitle = "设置为常用地址",
                    color = JixingColors.AccentAmber
                )
                DestinationItem(
                    icon = Icons.Default.Work,
                    title = "去公司",
                    subtitle = "设置为常用地址",
                    color = JixingColors.PrimaryBlue
                )
                DestinationItem(
                    icon = Icons.Default.LocalGasStation,
                    title = "加油站",
                    subtitle = "附近最近",
                    color = JixingColors.Success
                )
                DestinationItem(
                    icon = Icons.Default.LocalParking,
                    title = "停车场",
                    subtitle = "附近最近",
                    color = JixingColors.Info
                )
            }
        }
        
        // 底部导航栏
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = JixingColors.SurfaceDark,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavAction(
                    icon = Icons.Default.ZoomInMap,
                    label = "缩放",
                    onClick = {}
                )
                NavAction(
                    icon = Icons.Default.Layers,
                    label = "图层",
                    onClick = {}
                )
                NavAction(
                    icon = Icons.Default.Route,
                    label = "路线",
                    onClick = {}
                )
                NavAction(
                    icon = Icons.Default.MyLocation,
                    label = "定位",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun DestinationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, color = JixingColors.TextPrimaryDark)
                Text(subtitle, fontSize = 12.sp, color = JixingColors.TextSecondaryDark)
            }
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = "导航",
                    tint = JixingColors.PrimaryBlue
                )
            }
        }
    }
}

@Composable
fun NavAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(JixingColors.CardDark)
        ) {
            Icon(icon, contentDescription = label, tint = JixingColors.TextPrimaryDark)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = JixingColors.TextSecondaryDark)
    }
}
