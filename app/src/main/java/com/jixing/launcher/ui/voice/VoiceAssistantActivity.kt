package com.jixing.launcher.ui.voice

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jixing.launcher.managers.VoiceAssistantManager
import com.jixing.launcher.ui.theme.JixingColors
import com.jixing.launcher.model.VoiceCommand
import com.jixing.launcher.model.VoiceCommandType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 语音助手 Activity
 */
@AndroidEntryPoint
class VoiceAssistantActivity : ComponentActivity() {
    
    @Inject
    lateinit var voiceAssistantManager: VoiceAssistantManager
    
    private val viewModel: VoiceViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        
        setContent {
            VoiceScreen(
                viewModel = viewModel,
                voiceAssistantManager = voiceAssistantManager,
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
fun VoiceScreen(
    viewModel: VoiceViewModel,
    voiceAssistantManager: VoiceAssistantManager,
    onBack: () -> Unit
) {
    val isListening by voiceAssistantManager.isListening.collectAsState()
    val lastCommand by voiceAssistantManager.lastCommand.collectAsState()
    val conversation by viewModel.conversation.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JixingColors.BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                Text("语音助手", fontSize = 20.sp, color = JixingColors.TextPrimaryDark)
                IconButton(onClick = { viewModel.clearConversation() }) {
                    Icon(Icons.Default.Delete, "清除", tint = JixingColors.TextPrimaryDark)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 动画圆形
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isListening) 1.2f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = if (isListening) 0.8f else 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )
            
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(if (isListening) scale else 1f)
                    .clip(CircleShape)
                    .background(JixingColors.PrimaryBlue.copy(alpha = alpha)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (isListening) {
                            voiceAssistantManager.stopListening()
                        } else {
                            voiceAssistantManager.startListening()
                        }
                    },
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(if (isListening) JixingColors.PrimaryBlue else JixingColors.CardDark)
                ) {
                    Icon(
                        if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "语音",
                        modifier = Modifier.size(64.dp),
                        tint = if (isListening) Color.White else JixingColors.TextPrimaryDark
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isListening) "请说话..." else "点击开始语音",
                fontSize = 18.sp,
                color = JixingColors.TextSecondaryDark
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 快捷命令
            Text(
                text = "快捷命令",
                fontSize = 16.sp,
                color = JixingColors.TextSecondaryDark,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCommand(
                    icon = Icons.Default.Navigation,
                    text = "导航到...",
                    onClick = { voiceAssistantManager.startListening() }
                )
                QuickCommand(
                    icon = Icons.Default.MusicNote,
                    text = "播放音乐",
                    onClick = { viewModel.addCommand("播放音乐", VoiceCommandType.MEDIA) }
                )
                QuickCommand(
                    icon = Icons.Default.AcUnit,
                    text = "温度调低一点",
                    onClick = { viewModel.addCommand("温度调低一点", VoiceCommandType.HVAC) }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 最近的对话
            if (conversation.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "最近",
                            fontSize = 14.sp,
                            color = JixingColors.TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        conversation.takeLast(2).forEach { cmd ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when (cmd.type) {
                                        VoiceCommandType.NAVIGATION -> Icons.Default.Navigation
                                        VoiceCommandType.MEDIA -> Icons.Default.MusicNote
                                        VoiceCommandType.HVAC -> Icons.Default.AcUnit
                                        else -> Icons.Default.Mic
                                    },
                                    null,
                                    tint = JixingColors.PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cmd.command, fontSize = 14.sp, color = JixingColors.TextPrimaryDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickCommand(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = JixingColors.PrimaryBlue)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontSize = 16.sp, color = JixingColors.TextPrimaryDark)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = JixingColors.TextSecondaryDark
            )
        }
    }
}
