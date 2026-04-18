package com.jixing.launcher.ui.files

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.jixing.launcher.model.FileInfo
import com.jixing.launcher.model.JixingColors
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件管理器 Activity
 */
@AndroidEntryPoint
class FileManagerActivity : ComponentActivity() {
    private val viewModel: FileViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            FileManagerScreen(viewModel = viewModel, onBack = { finish() })
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
fun FileManagerScreen(viewModel: FileViewModel, onBack: () -> Unit) {
    val currentPath by viewModel.currentPath.collectAsState()
    val files by viewModel.files.collectAsState()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (viewModel.canGoBack()) {
                        viewModel.goBack()
                    } else {
                        onBack()
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = JixingColors.TextPrimaryDark)
                }
                Text("文件管理", fontSize = 20.sp, color = JixingColors.TextPrimaryDark)
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, "刷新", tint = JixingColors.TextPrimaryDark)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 路径导航
            Card(
                colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FolderOpen, null, tint = JixingColors.AccentAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentPath,
                        fontSize = 14.sp,
                        color = JixingColors.TextPrimaryDark
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JixingColors.PrimaryBlue)
                }
            } else if (files.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FolderOff,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = JixingColors.TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("此文件夹为空", fontSize = 16.sp, color = JixingColors.TextSecondaryDark)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files) { file ->
                        FileItem(file = file, onClick = { viewModel.openFile(file) })
                    }
                }
            }
        }
    }
}

@Composable
fun FileItem(file: FileInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, color) = when {
                file.isDirectory -> Icons.Default.Folder to JixingColors.AccentAmber
                file.mimeType.startsWith("image/") -> Icons.Default.Image to Color(0xFF4CAF50)
                file.mimeType.startsWith("video/") -> Icons.Default.VideoFile to Color(0xFFE91E63)
                file.mimeType.startsWith("audio/") -> Icons.Default.AudioFile to Color(0xFF9C27B0)
                file.mimeType.contains("pdf") -> Icons.Default.PictureAsPdf to Color(0xFFF44336)
                file.mimeType.contains("text") -> Icons.Default.Description to Color(0xFF2196F3)
                else -> Icons.Default.InsertDriveFile to JixingColors.TextSecondaryDark
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    color = JixingColors.TextPrimaryDark,
                    maxLines = 1
                )
                if (!file.isDirectory) {
                    Text(
                        text = formatFileSize(file.size),
                        fontSize = 12.sp,
                        color = JixingColors.TextSecondaryDark
                    )
                }
            }
            
            Text(
                text = formatDate(file.lastModified),
                fontSize = 10.sp,
                color = JixingColors.TextTertiaryDark
            )
            
            if (file.isDirectory) {
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = JixingColors.TextSecondaryDark
                )
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "%.1f KB".format(size / 1024.0)
        size < 1024 * 1024 * 1024 -> "%.1f MB".format(size / (1024.0 * 1024))
        else -> "%.1f GB".format(size / (1024.0 * 1024 * 1024))
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
