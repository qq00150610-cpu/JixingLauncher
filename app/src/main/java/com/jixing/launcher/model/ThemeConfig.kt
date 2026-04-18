package com.jixing.launcher.model

import androidx.compose.ui.graphics.Color

/**
 * UI 主题和颜色配置
 */
object JixingColors {
    // 主色调 - 科技蓝
    val PrimaryBlue = Color(0xFF2196F3)
    val PrimaryBlueDark = Color(0xFF1976D2)
    val PrimaryBlueLight = Color(0xFF64B5F6)
    
    // 强调色 - 琥珀色
    val AccentAmber = Color(0xFFFFB300)
    val AccentAmberDark = Color(0xFFFF8F00)
    val AccentAmberLight = Color(0xFFFFCA28)
    
    // 深色主题背景
    val BackgroundDark = Color(0xFF121212)
    val SurfaceDark = Color(0xFF1E1E1E)
    val CardDark = Color(0xFF2D2D2D)
    val ElevatedDark = Color(0xFF383838)
    
    // 浅色主题背景
    val BackgroundLight = Color(0xFFF5F5F5)
    val SurfaceLight = Color(0xFFFFFFFF)
    val CardLight = Color(0xFFFFFFFF)
    
    // 文字颜色
    val TextPrimaryDark = Color(0xFFFFFFFF)
    val TextSecondaryDark = Color(0xB3FFFFFF)
    val TextTertiaryDark = Color(0x80FFFFFF)
    val TextPrimaryLight = Color(0xFF212121)
    val TextSecondaryLight = Color(0xFF757575)
    
    // 状态颜色
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    val ErrorRed = Error  // 别名，用于错误提示
    val Info = Color(0xFF2196F3)
    
    // 功能颜色
    val MediaColor = Color(0xFFE91E63)
    val NavColor = Color(0xFF00BCD4)
    val PhoneColor = Color(0xFF4CAF50)
    val HvacColor = Color(0xFFFF5722)
}

/**
 * UI 尺寸配置
 */
object JixingDimensions {
    // 触摸目标最小尺寸（驾驶安全）
    const val MIN_TOUCH_TARGET = 64
    
    // 大按钮尺寸
    const val LARGE_BUTTON_SIZE = 96
    const val MEDIUM_BUTTON_SIZE = 72
    const val SMALL_BUTTON_SIZE = 56
    
    // 卡片尺寸
    const val CARD_CORNER_RADIUS = 16
    const val CARD_ELEVATION = 4
    const val CARD_PADDING = 16
    
    // 间距
    const val SPACING_SMALL = 8
    const val SPACING_MEDIUM = 16
    const val SPACING_LARGE = 24
    const val SPACING_XLARGE = 32
    
    // 字体大小
    const val TEXT_SIZE_TITLE = 28f
    const val TEXT_SIZE_SUBTITLE = 20f
    const val TEXT_SIZE_BODY = 16f
    const val TEXT_SIZE_CAPTION = 14f
    const val TEXT_SIZE_SMALL = 12f
}

/**
 * UI 常量
 */
object JixingConstants {
    const val ANIMATION_DURATION = 300
    const val FLICKERING_THRESHOLD = 1000L
    const val DOUBLE_TAP_INTERVAL = 300L
    const val LONG_PRESS_DURATION = 500L
    
    // 悬浮球配置
    const val FLOATING_BALL_SIZE = 64
    const val FLOATING_BALL_MARGIN = 32
    
    // 导航栏配置
    const val NAV_BAR_HEIGHT = 80
    const val QUICK_NAV_HEIGHT = 60
}
