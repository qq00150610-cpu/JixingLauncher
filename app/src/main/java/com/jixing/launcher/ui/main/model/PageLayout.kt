package com.jixing.launcher.model

/**
 * 页面布局数据模型 - 氢桌面风格
 */
data class PageLayout(
    val pageIndex: Int,
    val items: List<GridItem> = emptyList()
) {
    companion object {
        const val MAX_ITEMS_PER_PAGE = 24 // 4x6 或 4x4 根据方向
    }
}

/**
 * 网格项数据模型
 */
data class GridItem(
    val id: String,
    val packageName: String,
    val appName: String,
    val position: Int, // 在页面中的位置 0-23
    val folderId: String? = null, // 如果在文件夹中
    val isFolder: Boolean = false
)

/**
 * 文件夹数据模型
 */
data class AppFolder(
    val id: String,
    val name: String,
    val items: MutableList<GridItem> = mutableListOf(),
    val position: Int // 在页面中的位置
)

/**
 * 主页布局数据
 */
data class HomeLayout(
    val pages: List<PageLayout>,
    val folders: List<AppFolder>,
    val currentPage: Int = 0
) {
    val totalPages: Int get() = pages.size
    
    fun getAllApps(): List<GridItem> {
        return pages.flatMap { it.items }
    }
    
    fun getAppsOnPage(pageIndex: Int): List<GridItem> {
        return pages.getOrNull(pageIndex)?.items ?: emptyList()
    }
}

/**
 * 拖拽状态
 */
data class DragState(
    val isDragging: Boolean = false,
    val draggedItem: GridItem? = null,
    val targetPosition: Int = -1,
    val sourcePage: Int = -1,
    val targetPage: Int = -1
)

/**
 * 编辑模式状态
 */
data class EditModeState(
    val isEditMode: Boolean = false,
    val selectedItems: Set<String> = emptySet(), // 选中的应用 ID
    val isSelectingFolder: Boolean = false // 是否正在选择要放入文件夹的应用
)
