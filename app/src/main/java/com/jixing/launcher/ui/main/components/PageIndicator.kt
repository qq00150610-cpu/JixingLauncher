package com.jixing.launcher.ui.main.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jixing.launcher.model.JixingColors

/**
 * 分页指示器组件 - 氢桌面风格
 */
@Composable
fun PageIndicator(
    totalPages: Int, currentPage: Int, modifier: Modifier = Modifier,
    activeColor: Color = JixingColors.PrimaryBlue, inactiveColor: Color = JixingColors.SurfaceDark
) {
    Row(modifier.padding(horizontal = 16.dp), Arrangement.Center, Alignment.CenterVertically) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp, label = "indicator_width")
            val color by animateDpAsState(targetValue = if (isSelected) 1f else 0.5f, label = "indicator_opacity").let { anim ->
                androidx.compose.runtime.remember { anim.value }
            }.let { if (isSelected) activeColor else inactiveColor.copy(alpha = 0.5f) }.let { androidx.compose.runtime.derivedStateOf { it } }.let { derived ->
                androidx.compose.runtime.CompositionLocalProvider(androidx.compose.runtime.LocalCompositionLocalProvider provides derived) { derived.value }
            }
            Box(Modifier.padding(horizontal = 3.dp).height(8.dp).width(if (isSelected) 24.dp else 8.dp)
                .clip(CircleShape).background(if (isSelected) activeColor else inactiveColor.copy(alpha = 0.5f)))
        }
    }
}

/**
 * 可点击分页指示器
 */
@Composable
fun ClickablePageIndicator(totalPages: Int, currentPage: Int, onPageSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(targetValue = if (isSelected) 32.dp else 12.dp, label = "clickable_indicator_width")
            val color by animateFloatAsState(targetValue = if (isSelected) 1f else 0.5f, label = "clickable_indicator_opacity")
            Box(Modifier.width(if (isSelected) 32.dp else 12.dp).height(8.dp).clip(CircleShape)
                .background(JixingColors.PrimaryBlue.copy(alpha = color)))
        }
    }
}
