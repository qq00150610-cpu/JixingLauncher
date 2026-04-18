package com.jixing.launcher.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jixing.launcher.R
import com.jixing.launcher.managers.VehicleStateManager
import com.jixing.launcher.model.*
import com.jixing.launcher.ui.utils.rememberScreenInfo
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主桌面 Activity - Jetpack Compose 实现（多分辨率适配版）
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var vehicleStateManager: VehicleStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 全屏沉浸式
        setupFullScreen()
        
        vehicleStateManager = VehicleStateManager.getInstance(this)
        vehicleStateManager.startMonitoring()

        setContent {
            JixingTheme {
                MainScreen(
                    viewModel = viewModel,
                    vehicleState = vehicleStateManager.vehicleState.collectAsState(),
                    onNavigateTo = { activityClass -> navigateTo(activityClass) }
                )
            }
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

    private fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadApps()
    }

    override fun onDestroy() {
        super.onDestroy()
        vehicleStateManager.stopMonitoring()
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    vehicleState: androidx.compose.runtime.State<VehicleState>,
    onNavigateTo: (Class<*>) -> Unit
) {
    val screenInfo = rememberScreenInfo()
    
    // 响应式间距
    val spacingSmall = dimensionResource(R.dimen.spacing_small)
    val spacingMedium = dimensionResource(R.dimen.spacing_medium)
    val spacingLarge = dimensionResource(R.dimen.spacing_large)
    
    // 响应式字体大小
    val textSizeTitle = dimensionResource(R.dimen.text_size_title)
    val textSizeSubtitle = dimensionResource(R.dimen.text_size_subtitle)
    val textSizeBody = dimensionResource(R.dimen.text_size_body)
    val textSizeCaption = dimensionResource(R.dimen.text_size_caption)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JixingColors.BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部状态栏
            TopStatusBar(
                state = vehicleState.value,
                textSizeLarge = textSizeTitle,
                textSizeBody = textSizeBody
            )
            
            // 主内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacingMedium),
                verticalArrangement = Arrangement.spacedBy(spacingMedium)
            ) {
                // 快捷入口
                QuickAccessSection(
                    onNavigateTo = onNavigateTo,
                    screenInfo = screenInfo
                )
                
                // 应用网格
                AppGridSection(
                    viewModel = viewModel,
                    screenInfo = screenInfo
                )
            }
        }
        
        // 底部导航栏
        BottomNavigationBar(
            onNavigateTo = onNavigateTo,
            screenInfo = screenInfo,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun TopStatusBar(
    state: VehicleState,
    textSizeLarge: androidx.compose.ui.unit.TextUnit,
    textSizeBody: androidx.compose.ui.unit.TextUnit
) {
    val padding = dimensionResource(R.dimen.spacing_medium)
    val iconSize = dimensionResource(R.dimen.icon_size_medium)
    val iconSpacing = dimensionResource(R.dimen.spacing_medium)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 速度显示
            Column {
                Text(
                    text = "${state.speed.toInt()}",
                    fontSize = textSizeLarge,
                    fontWeight = FontWeight.Bold,
                    color = JixingColors.TextPrimaryDark
                )
                Text(
                    text = "km/h",
                    fontSize = textSizeBody,
                    color = JixingColors.TextSecondaryDark
                )
            }
            
            // 车辆状态
            Row(
                horizontalArrangement = Arrangement.spacedBy(iconSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 油耗/电量
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = JixingColors.AccentAmber,
                        modifier = Modifier.size(iconSize)
                    )
                    Text(
                        text = "${state.fuelLevel}%",
                        fontSize = textSizeBody,
                        fontWeight = FontWeight.Bold,
                        color = JixingColors.TextPrimaryDark
                    )
                }
                
                // 时间
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = JixingColors.PrimaryBlue,
                        modifier = Modifier.size(iconSize)
                    )
                    Text(
                        text = "12:30",
                        fontSize = textSizeBody,
                        fontWeight = FontWeight.Bold,
                        color = JixingColors.TextPrimaryDark
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAccessSection(
    onNavigateTo: (Class<*>) -> Unit,
    screenInfo: com.jixing.launcher.ui.utils.ScreenInfo
) {
    val spacing = dimensionResource(R.dimen.spacing_small)
    val cardSize = dimensionResource(R.dimen.quick_card_size)
    val iconSize = dimensionResource(R.dimen.quick_icon_size)
    val cardPadding = dimensionResource(R.dimen.card_padding)
    val textSize = dimensionResource(R.dimen.text_size_caption)
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            QuickAccessCard(
                icon = Icons.Default.Navigation,
                title = "导航",
                color = JixingColors.NavColor,
                onClick = { onNavigateTo(com.jixing.launcher.ui.navigation.FloatingNavActivity::class.java) },
                cardSize = cardSize,
                iconSize = iconSize,
                padding = cardPadding,
                textSize = textSize
            )
        }
        item {
            QuickAccessCard(
                icon = Icons.Default.MusicNote,
                title = "音乐",
                color = JixingColors.MediaColor,
                onClick = { onNavigateTo(com.jixing.launcher.ui.media.MediaCenterActivity::class.java) },
                cardSize = cardSize,
                iconSize = iconSize,
                padding = cardPadding,
                textSize = textSize
            )
        }
        item {
            QuickAccessCard(
                icon = Icons.Default.AcUnit,
                title = "空调",
                color = JixingColors.HvacColor,
                onClick = { onNavigateTo(com.jixing.launcher.ui.hvac.HvacControlActivity::class.java) },
                cardSize = cardSize,
                iconSize = iconSize,
                padding = cardPadding,
                textSize = textSize
            )
        }
        item {
            QuickAccessCard(
                icon = Icons.Default.Settings,
                title = "设置",
                color = JixingColors.PrimaryBlue,
                onClick = { onNavigateTo(com.jixing.launcher.ui.settings.SettingsActivity::class.java) },
                cardSize = cardSize,
                iconSize = iconSize,
                padding = cardPadding,
                textSize = textSize
            )
        }
        item {
            QuickAccessCard(
                icon = Icons.Default.Mic,
                title = "语音",
                color = JixingColors.AccentAmber,
                onClick = { onNavigateTo(com.jixing.launcher.ui.voice.VoiceAssistantActivity::class.java) },
                cardSize = cardSize,
                iconSize = iconSize,
                padding = cardPadding,
                textSize = textSize
            )
        }
    }
}

@Composable
fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit,
    cardSize: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    padding: androidx.compose.ui.unit.Dp,
    textSize: androidx.compose.ui.unit.TextUnit
) {
    val cornerRadius = dimensionResource(R.dimen.card_corner_radius)
    
    Card(
        modifier = Modifier
            .size(cardSize)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(iconSize * 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = textSize,
                color = JixingColors.TextPrimaryDark,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AppGridSection(
    viewModel: MainViewModel,
    screenInfo: com.jixing.launcher.ui.utils.ScreenInfo
) {
    val apps by viewModel.userApps.collectAsState()
    
    // 动态获取网格列数
    val gridColumns = integerResource(R.integer.grid_columns)
    val spacing = dimensionResource(R.dimen.spacing_small)
    val textSize = dimensionResource(R.dimen.text_size_subtitle)
    val paddingBottom = dimensionResource(R.dimen.spacing_small)
    val appsPerPage = integerResource(R.integer.apps_per_page)
    
    Column {
        Text(
            text = "应用",
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            color = JixingColors.TextPrimaryDark,
            modifier = Modifier.padding(bottom = paddingBottom)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            items(apps.take(appsPerPage)) { app ->
                AppGridItem(
                    app = app,
                    onClick = { viewModel.launchApp(app.packageName) }
                )
            }
        }
    }
}

@Composable
fun AppGridItem(app: AppInfo, onClick: () -> Unit) {
    val cornerRadius = dimensionResource(R.dimen.card_corner_radius)
    val padding = dimensionResource(R.dimen.app_item_padding)
    val iconSize = dimensionResource(R.dimen.app_icon_size)
    val textSize = dimensionResource(R.dimen.text_size_small)
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = JixingColors.CardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            app.icon?.let { icon ->
                val bitmap = remember(icon) {
                    Bitmap.createBitmap(icon.intrinsicWidth.coerceAtLeast(1), icon.intrinsicHeight.coerceAtLeast(1), Bitmap.Config.ARGB_8888).also {
                        icon.setBounds(0, 0, it.width, it.height)
                        icon.draw(Canvas(it))
                    }
                }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(iconSize)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.appName,
                fontSize = textSize,
                color = JixingColors.TextPrimaryDark,
                maxLines = 1
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    onNavigateTo: (Class<*>) -> Unit,
    screenInfo: com.jixing.launcher.ui.utils.ScreenInfo,
    modifier: Modifier = Modifier
) {
    val navBarHeight = dimensionResource(R.dimen.nav_bar_height)
    val cornerRadius = dimensionResource(R.dimen.card_corner_radius)
    val horizontalPadding = dimensionResource(R.dimen.spacing_medium)
    val verticalPadding = dimensionResource(R.dimen.spacing_small)
    val iconSize = dimensionResource(R.dimen.icon_size_large)
    val itemPadding = dimensionResource(R.dimen.spacing_small)
    val textSize = dimensionResource(R.dimen.text_size_small)
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = JixingColors.SurfaceDark,
        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(navBarHeight)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                label = "首页",
                selected = true,
                onClick = {},
                iconSize = iconSize,
                padding = itemPadding,
                textSize = textSize
            )
            NavBarItem(
                icon = Icons.Default.Apps,
                label = "应用",
                onClick = { onNavigateTo(com.jixing.launcher.ui.apps.AppManagerActivity::class.java) },
                iconSize = iconSize,
                padding = itemPadding,
                textSize = textSize
            )
            NavBarItem(
                icon = Icons.Default.Folder,
                label = "文件",
                onClick = { onNavigateTo(com.jixing.launcher.ui.files.FileManagerActivity::class.java) },
                iconSize = iconSize,
                padding = itemPadding,
                textSize = textSize
            )
            NavBarItem(
                icon = Icons.Default.Build,
                label = "工厂",
                onClick = { onNavigateTo(com.jixing.launcher.ui.factory.FactoryModeActivity::class.java) },
                iconSize = iconSize,
                padding = itemPadding,
                textSize = textSize
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    iconSize: androidx.compose.ui.unit.Dp,
    padding: androidx.compose.ui.unit.Dp,
    textSize: androidx.compose.ui.unit.TextUnit
) {
    val itemColor = if (selected) JixingColors.PrimaryBlue else JixingColors.TextSecondaryDark
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = itemColor,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = textSize,
            color = itemColor
        )
    }
}

@Composable
fun JixingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = JixingColors.PrimaryBlue,
            secondary = JixingColors.AccentAmber,
            background = JixingColors.BackgroundDark,
            surface = JixingColors.SurfaceDark,
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = JixingColors.TextPrimaryDark,
            onSurface = JixingColors.TextPrimaryDark
        ),
        content = content
    )
}
