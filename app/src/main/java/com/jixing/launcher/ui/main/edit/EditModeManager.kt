package com.jixing.launcher.ui.main.edit

import com.jixing.launcher.model.AppFolder
import com.jixing.launcher.model.DragState
import com.jixing.launcher.model.EditModeState
import com.jixing.launcher.model.GridItem
import com.jixing.launcher.model.HomeLayout
import com.jixing.launcher.model.PageLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * 编辑模式管理器 - 氢桌面风格
 */
class EditModeManager {

    private val _editModeState = MutableStateFlow(EditModeState())
    val editModeState: StateFlow<EditModeState> = _editModeState.asStateFlow()

    private val _dragState = MutableStateFlow(DragState())
    val dragState: StateFlow<DragState> = _dragState.asStateFlow()

    private val _homeLayout = MutableStateFlow<HomeLayout?>(null)
    val homeLayout: StateFlow<HomeLayout?> = _homeLayout.asStateFlow()

    private val _folders = MutableStateFlow<List<AppFolder>>(emptyList())
    val folders: StateFlow<List<AppFolder>> = _folders.asStateFlow()

    /**
     * 进入编辑模式
     */
    fun enterEditMode() {
        _editModeState.update { it.copy(isEditMode = true) }
    }

    /**
     * 退出编辑模式
     */
    fun exitEditMode() {
        _editModeState.update { 
            it.copy(
                isEditMode = false,
                selectedItems = emptySet(),
                isSelectingFolder = false
            )
        }
        _dragState.update { DragState() }
    }

    /**
     * 切换编辑模式
     */
    fun toggleEditMode() {
        if (_editModeState.value.isEditMode) {
            exitEditMode()
        } else {
            enterEditMode()
        }
    }

    /**
     * 选中/取消选中应用
     */
    fun toggleItemSelection(itemId: String) {
        _editModeState.update { state ->
            val newSelected = if (itemId in state.selectedItems) {
                state.selectedItems - itemId
            } else {
                state.selectedItems + itemId
            }
            state.copy(selectedItems = newSelected)
        }
    }

    /**
     * 清除选择
     */
    fun clearSelection() {
        _editModeState.update { it.copy(selectedItems = emptySet()) }
    }

    /**
     * 开始拖拽
     */
    fun startDrag(item: GridItem, sourcePage: Int) {
        _dragState.update {
            DragState(
                isDragging = true,
                draggedItem = item,
                sourcePage = sourcePage
            )
        }
    }

    /**
     * 更新拖拽目标位置
     */
    fun updateDragTarget(position: Int, targetPage: Int) {
        _dragState.update {
            it.copy(
                targetPosition = position,
                targetPage = targetPage
            )
        }
    }

    /**
     * 结束拖拽
     */
    fun endDrag(): PositionSwap? {
        val state = _dragState.value
        if (!state.isDragging || state.draggedItem == null) {
            _dragState.update { DragState() }
            return null
        }

        val swap = PositionSwap(
            fromPosition = com.jixing.launcher.ui.main.model.GridItemPosition(
                pageIndex = state.sourcePage,
                row = state.draggedItem.position / 4,
                column = state.draggedItem.position % 4
            ),
            toPosition = com.jixing.launcher.ui.main.model.GridItemPosition(
                pageIndex = state.targetPage,
                row = state.targetPosition / 4,
                column = state.targetPosition % 4
            )
        )

        _dragState.update { DragState() }
        return swap
    }

    /**
     * 取消拖拽
     */
    fun cancelDrag() {
        _dragState.update { DragState() }
    }

    /**
     * 创建文件夹
     */
    fun createFolder(name: String, items: List<GridItem>): AppFolder {
        val folder = AppFolder(
            id = UUID.randomUUID().toString(),
            name = name,
            items = items.toMutableList(),
            position = items.firstOrNull()?.position ?: 0
        )
        _folders.update { it + folder }
        return folder
    }

    /**
     * 重命名文件夹
     */
    fun renameFolder(folderId: String, newName: String) {
        _folders.update { folders ->
            folders.map { folder ->
                if (folder.id == folderId) folder.copy(name = newName) else folder
            }
        }
    }

    /**
     * 删除文件夹
     */
    fun deleteFolder(folderId: String): List<GridItem> {
        val folder = _folders.value.find { it.id == folderId }
        _folders.update { it.filter { f -> f.id != folderId } }
        return folder?.items ?: emptyList()
    }

    /**
     * 向文件夹添加应用
     */
    fun addToFolder(folderId: String, item: GridItem) {
        _folders.update { folders ->
            folders.map { folder ->
                if (folder.id == folderId) {
                    folder.copy(items = folder.items + item)
                } else {
                    folder
                }
            }
        }
    }

    /**
     * 从文件夹移除应用
     */
    fun removeFromFolder(folderId: String, itemId: String) {
        _folders.update { folders ->
            folders.mapNotNull { folder ->
                if (folder.id == folderId) {
                    val newItems = folder.items.filter { it.id != itemId }
                    if (newItems.isEmpty()) null else folder.copy(items = newItems)
                } else {
                    folder
                }
            }
        }
    }

    /**
     * 设置主页布局
     */
    fun setHomeLayout(layout: HomeLayout) {
        _homeLayout.value = layout
    }

    /**
     * 初始化默认布局
     */
    fun initializeDefaultLayout(apps: List<GridItem>, appsPerPage: Int): HomeLayout {
        val pages = mutableListOf<PageLayout>()
        var currentPage = 0
        var currentItems = mutableListOf<GridItem>()

        apps.forEachIndexed { index, app ->
            if (index > 0 && index % appsPerPage == 0) {
                pages.add(PageLayout(currentPage, currentItems.toList()))
                currentPage++
                currentItems = mutableListOf()
            }
            currentItems.add(app.copy(position = currentItems.size))
        }

        // 添加最后一页
        if (currentItems.isNotEmpty()) {
            pages.add(PageLayout(currentPage, currentItems.toList()))
        }

        // 确保至少有一页
        if (pages.isEmpty()) {
            pages.add(PageLayout(0, emptyList()))
        }

        val layout = HomeLayout(pages = pages, currentPage = 0)
        _homeLayout.value = layout
        return layout
    }

    /**
     * 移动应用到新位置
     */
    fun moveItem(fromPosition: Int, toPosition: Int, pageIndex: Int): HomeLayout? {
        val layout = _homeLayout.value ?: return null
        if (pageIndex >= layout.pages.size) return null

        val page = layout.pages[pageIndex].toMutableList()
        if (fromPosition >= page.size || toPosition >= page.size) return null

        val item = page.removeAt(fromPosition)
        page.add(toPosition, item.copy(position = toPosition))

        // 重新编号
        val updatedPage = page.mapIndexed { index, app -> app.copy(position = index) }
        val newPages = layout.pages.toMutableList()
        newPages[pageIndex] = PageLayout(pageIndex, updatedPage)

        val newLayout = layout.copy(pages = newPages)
        _homeLayout.value = newLayout
        return newLayout
    }

    /**
     * 跨页移动应用
     */
    fun moveItemToPage(
        itemId: String,
        fromPage: Int,
        toPage: Int,
        toPosition: Int
    ): HomeLayout? {
        val layout = _homeLayout.value ?: return null

        val sourcePage = layout.pages.getOrNull(fromPage) ?: return null
        val item = sourcePage.items.find { it.id == itemId } ?: return null

        val newPages = layout.pages.toMutableList()

        // 从原页面移除
        val newSourceItems = sourcePage.items.filter { it.id != itemId }
            .mapIndexed { index, app -> app.copy(position = index) }
        newPages[fromPage] = PageLayout(fromPage, newSourceItems)

        // 添加到目标页面
        if (toPage < newPages.size) {
            val targetPage = newPages[toPage].items.toMutableList()
            val updatedItem = item.copy(position = toPosition)
            targetPage.add(toPosition.coerceAtMost(targetPage.size), updatedItem)
            newPages[toPage] = PageLayout(
                toPage,
                targetPage.mapIndexed { index, app -> app.copy(position = index) }
            )
        }

        val newLayout = layout.copy(pages = newPages)
        _homeLayout.value = newLayout
        return newLayout
    }
}
