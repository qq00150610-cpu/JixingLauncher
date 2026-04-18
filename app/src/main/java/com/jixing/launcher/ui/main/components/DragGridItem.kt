package com.jixing.launcher.ui.main.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jixing.launcher.model.AppInfo
import com.jixing.launcher.model.GridItem
import com.jixing.launcher.ui.theme.JixingColors
import kotlin.math.roundToInt

/**
 * 可拖拽网格项组件 - 氢桌面风格
 */
@Composable
fun DragGridItem(
    app: AppInfo?,
    gridItem: GridItem?,
    isInEditMode: Boolean,
    isSelected: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDraggingThis by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            isDraggingThis -> 1.15f
            isDragging -> 0.95f
            else -> 1f
        },
        label = "drag_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isDraggingThis) 12.dp else if (isInEditMode) 2.dp else 0.dp,
        label = "drag_elevation"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .shadow(elevation, RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, JixingColors.PrimaryBlue, RoundedCornerShape(16.dp))
                } else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isDraggingThis) JixingColors.ElevatedDark
                else JixingColors.CardDark
            )
            .pointerInput(isInEditMode) {
                if (isInEditMode) {
                    detectTapGestures(
                        onTap = { if (isSelected) onClick() else onLongClick() },
                        onLongPress = { isDraggingThis = true; onDragStart() }
                    )
                } else {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { onLongClick() }
                    )
                }
            }
            .pointerInput(isInEditMode) {
                if (isInEditMode) {
                    detectDragGestures(
                        onDragStart = { isDraggingThis = true; onDragStart() },
                        onDragEnd = {
                            val finalOffset = Offset(offsetX, offsetY)
                            isDraggingThis = false
                            offsetX = 0f; offsetY = 0f
                            onDragEnd(finalOffset)
                        },
                        onDragCancel = {
                            isDraggingThis = false
                            offsetX = 0f; offsetY = 0f
                            onDrag()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            onDrag(Offset(offsetX, offsetY))
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JixingColors.SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                if (app?.icon != null) {
                    androidx.compose.foundation.Image(
                        bitmap = app.icon.asImageBitmap(),
                        contentDescription = app.appName,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(Icons.Default.Android, null, Modifier.size(32.dp), JixingColors.TextSecondaryDark)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(app?.appName ?: gridItem?.appName ?: "", 12.sp, JixingColors.TextPrimaryDark, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            if (isInEditMode && isSelected) {
                Box(Modifier.size(20.dp).offset(x = 28.dp, y = (-36).dp).clip(RoundedCornerShape(50)).background(JixingColors.Error).align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, "移除", Modifier.size(14.dp), Color.White)
                }
            }
        }
        if (isInEditMode) {
            Box(Modifier.fillMaxSize().graphicsLayer {})
        }
    }
}

/**
 * 文件夹网格项
 */
@Composable
fun FolderGridItem(
    folderName: String, itemCount: Int, isInEditMode: Boolean, isSelected: Boolean,
    onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier
) {
    Box(modifier = modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(JixingColors.CardDark)
        .then(if (isSelected) Modifier.border(2.dp, JixingColors.AccentAmber, RoundedCornerShape(16.dp)) else Modifier)
        .clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(JixingColors.AccentAmber.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Folder, null, Modifier.size(32.dp), JixingColors.AccentAmber)
            }
            Spacer(Modifier.height(6.dp))
            Text(folderName, 12.sp, JixingColors.TextPrimaryDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$itemCount 个应用", 10.sp, JixingColors.TextSecondaryDark)
        }
    }
}

/**
 * 空白网格项
 */
@Composable
fun EmptyGridItem(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(JixingColors.CardDark.copy(alpha = 0.3f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Add, "添加", Modifier.size(32.dp), JixingColors.TextTertiaryDark)
    }
}
