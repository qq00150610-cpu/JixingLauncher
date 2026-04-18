package com.jixing.launcher.ui.apps

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jixing.launcher.model.AppInfo
import com.jixing.launcher.ui.theme.JixingColors
import dagger.hilt.android.AndroidEntryPoint

/**
 * 应用管理器 Activity
 */
@AndroidEntryPoint
class AppManagerActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            AppManagerScreen(viewModel = viewModel, onBack = { finish() })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("全部", "用户", "系统")
    
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
                Text("应用管理", fontSize = 20.sp, color = JixingColors.TextPrimaryDark)
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchApps(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索应用", color = JixingColors.TextTertiaryDark) },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = JixingColors.TextSecondaryDark)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.loadApps()
                        }) {
                            Icon(Icons.Default.Clear, "清除", tint = JixingColors.TextSecondaryDark)
                        }
                    }
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tab
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = JixingColors.SurfaceDark,
                contentColor = JixingColors.PrimaryBlue
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 应用列表
            val apps by when (selectedTab) {
                0 -> viewModel.allApps.collectAsState()
                1 -> viewModel.userApps.collectAsState()
                else -> viewModel.systemApps.collectAsState()
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(apps) { app ->
                    AppGridItem(
                        app = app,
                        onClick = { viewModel.launchApp(app.packageName) },
                        onLongClick = { viewModel.openAppSettings(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppGridItem(app: AppInfo, onClick: () -> Unit, onLongClick: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            app.icon?.let { icon ->
                androidx.compose.foundation.Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(40.dp)
                )
            } ?: Icon(
                Icons.Default.Android,
                app.appName,
                modifier = Modifier.size(40.dp),
                tint = JixingColors.TextSecondaryDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.appName,
                fontSize = 12.sp,
                color = JixingColors.TextPrimaryDark,
                maxLines = 1
            )
            if (app.isSystemApp) {
                Text(
                    text = "系统",
                    fontSize = 10.sp,
                    color = JixingColors.AccentAmber
                )
            }
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(app.appName) },
            text = { Text("包名: ${app.packageName}") },
            confirmButton = {
                TextButton(onClick = {
                    onLongClick()
                    showDialog = false
                }) {
                    Text("应用信息")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
