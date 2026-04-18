package com.jixing.launcher.ui.main.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jixing.launcher.model.AppInfo
import com.jixing.launcher.model.GridItem
import com.jixing.launcher.ui.theme.JixingColors

/**
 * 文件夹编辑弹窗 - 氢桌面风格
 */
@Composable
fun FolderEditDialog(
    isVisible: Boolean, mode: FolderDialogMode, folderName: String = "",
    folderApps: List<AppInfo> = emptyList(), allApps: List<AppInfo> = emptyList(),
    onDismiss: () -> Unit, onConfirm: (String, List<GridItem>) -> Unit, onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(folderName) }
    var selectedApps by remember { mutableStateOf<List<GridItem>>(emptyList()) }
    var showAppSelector by remember { mutableStateOf(false) }

    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = JixingColors.SurfaceDark)) {
                Column(Modifier.padding(24.dp)) {
                    Text(when (mode) { FolderDialogMode.CREATE -> "创建文件夹" ; FolderDialogMode.RENAME -> "重命名文件夹" ; FolderDialogMode.EDIT -> "编辑文件夹" },
                        20.sp, FontWeight.Bold, JixingColors.TextPrimaryDark)
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(value = name, onValueChange = { newValue -> name = newValue }, label = { Text("文件夹名称") },
                        singleLine = true, modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = JixingColors.TextPrimaryDark, unfocusedTextColor = JixingColors.TextPrimaryDark,
                            focusedBorderColor = JixingColors.PrimaryBlue, unfocusedBorderColor = JixingColors.CardDark,
                            focusedLabelColor = JixingColors.PrimaryBlue, unfocusedLabelColor = JixingColors.TextSecondaryDark,
                            cursorColor = JixingColors.PrimaryBlue),
                        shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(16.dp))
                    if (mode != FolderDialogMode.RENAME) {
                        Text("包含的应用", 14.sp, JixingColors.TextSecondaryDark)
                        Spacer(Modifier.height(8.dp))
                        LazyVerticalGrid(columns = GridCells.Fixed(5), modifier.heightIn(max = 120.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(folderApps) { app ->
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(JixingColors.CardDark),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Android, app.appName, Modifier.size(24.dp), JixingColors.PrimaryBlue)
                                }
                            }
                            item {
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(JixingColors.CardDark.copy(alpha = 0.5f)).clickable { showAppSelector = true },
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, "添加", Modifier.size(24.dp), JixingColors.TextSecondaryDark)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                        if (mode == FolderDialogMode.EDIT) {
                            TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = JixingColors.Error)) {
                                Icon(Icons.Default.Delete, null); Spacer(Modifier.width(4.dp)); Text("删除")
                            }
                            Spacer(Modifier.weight(1f))
                        }
                        TextButton(onClick = onDismiss) { Text("取消", JixingColors.TextSecondaryDark) }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { onConfirm(name, selectedApps) }, enabled = name.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = JixingColors.PrimaryBlue), shape = RoundedCornerShape(12.dp)) { Text("确定") }
                    }
                }
            }
        }
    }
    if (showAppSelector) {
        AppSelectorDialog(allApps = allApps, selectedApps = folderApps, onDismiss = { showAppSelector = false }, onConfirm = { apps ->
            selectedApps = apps; showAppSelector = false
        })
    }
}

/**
 * 应用选择弹窗
 */
@Composable
private fun AppSelectorDialog(
    allApps: List<AppInfo>, 
    selectedApps: List<AppInfo>, 
    onDismiss: () -> Unit, 
    onConfirm: (List<AppInfo>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(selectedApps.toSet()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = JixingColors.SurfaceDark)) {
            Column(Modifier.padding(24.dp)) {
                Text("选择应用", 18.sp, FontWeight.Bold, JixingColors.TextPrimaryDark)
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier.heightIn(max = 300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allApps) { app ->
                        val isSelected = app.packageName in selected.map { it.packageName }
                        Box(Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) JixingColors.PrimaryBlue.copy(alpha = 0.2f) else JixingColors.CardDark)
                            .clickable { selected = if (isSelected) selected.filter { it.packageName != app.packageName }.toSet() else selected + app },
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Android, app.appName, Modifier.size(28.dp),
                                if (isSelected) JixingColors.PrimaryBlue else JixingColors.TextSecondaryDark)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消", JixingColors.TextSecondaryDark) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirm(selected.toList()) },
                        colors = ButtonDefaults.buttonColors(containerColor = JixingColors.PrimaryBlue), shape = RoundedCornerShape(12.dp)) { Text("确定 (${selected.size})") }
                }
            }
        }
    }
}

enum class FolderDialogMode { CREATE, RENAME, EDIT }
