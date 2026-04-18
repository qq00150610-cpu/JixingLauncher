package com.jixing.launcher.ui.media

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jixing.launcher.model.JixingColors
import com.jixing.launcher.model.MediaInfo
import dagger.hilt.android.AndroidEntryPoint

/**
 * 媒体中心 Activity
 */
@AndroidEntryPoint
class MediaCenterActivity : ComponentActivity() {
    
    private val viewModel: MediaViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            MediaScreen(
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
            controller.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

@Composable
fun MediaScreen(
    viewModel: MediaViewModel,
    onBack: () -> Unit
) {
    val currentMedia by viewModel.currentMedia.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playlist by viewModel.playlist.collectAsState()
    
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
            MediaTopBar(onBack = onBack)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 当前播放
            MediaPlayer(currentMedia, isPlaying, viewModel)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 播放列表
            Text(
                text = "播放列表",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = JixingColors.TextPrimaryDark
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlist) { media ->
                    MediaItem(media, media == currentMedia, viewModel)
                }
            }
        }
    }
}

@Composable
fun MediaTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "返回", tint = JixingColors.TextPrimaryDark)
        }
        Text("音乐", fontSize = 20.sp, color = JixingColors.TextPrimaryDark)
        IconButton(onClick = {}) {
            Icon(Icons.Default.Search, "搜索", tint = JixingColors.TextPrimaryDark)
        }
    }
}

@Composable
fun MediaPlayer(
    media: MediaInfo?,
    isPlaying: Boolean,
    viewModel: MediaViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 专辑封面
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(JixingColors.PrimaryBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Album,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = JixingColors.PrimaryBlue
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 歌曲信息
            Text(
                text = media?.title ?: "未播放",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = JixingColors.TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = media?.artist ?: "",
                fontSize = 16.sp,
                color = JixingColors.TextSecondaryDark
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = { 0.4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = JixingColors.PrimaryBlue,
                trackColor = JixingColors.SurfaceDark
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1:30", fontSize = 12.sp, color = JixingColors.TextSecondaryDark)
                Text("4:20", fontSize = 12.sp, color = JixingColors.TextSecondaryDark)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 播放控制
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.playPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, "上一首", tint = JixingColors.TextPrimaryDark, modifier = Modifier.size(36.dp))
                }
                IconButton(
                    onClick = { viewModel.togglePlay() },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(JixingColors.PrimaryBlue)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "播放/暂停",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = { viewModel.playNext() }) {
                    Icon(Icons.Default.SkipNext, "下一首", tint = JixingColors.TextPrimaryDark, modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

@Composable
fun MediaItem(media: MediaInfo, isCurrent: Boolean, viewModel: MediaViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectMedia(media) },
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) JixingColors.PrimaryBlue.copy(alpha = 0.2f) else JixingColors.CardDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JixingColors.SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, null, tint = JixingColors.TextSecondaryDark)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(media.title, fontSize = 14.sp, color = JixingColors.TextPrimaryDark, fontWeight = FontWeight.Medium)
                Text(media.artist, fontSize = 12.sp, color = JixingColors.TextSecondaryDark)
            }
            Text(formatDuration(media.duration), fontSize = 12.sp, color = JixingColors.TextSecondaryDark)
        }
    }
}

private fun formatDuration(duration: Long): String {
    val minutes = duration / 1000 / 60
    val seconds = duration / 1000 % 60
    return "%d:%02d".format(minutes, seconds)
}
