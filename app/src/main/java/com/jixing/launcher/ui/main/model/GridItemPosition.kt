package com.jixing.launcher.ui.main.model

import com.jixing.launcher.model.GridItem

/**
 * 网格位置模型
 */
data class GridItemPosition(
    val pageIndex: Int,
    val row: Int,
    val column: Int
) {
    /**
     * 转换为在页面中的索引位置
     */
    fun toIndex(columns: Int): Int = row * columns + column
    
    companion object {
        /**
         * 从索引位置创建
         */
        fun fromIndex(index: Int, columns: Int): GridItemPosition {
            val row = index / columns
            val column = index % columns
            return GridItemPosition(0, row, column)
        }
        
        /**
         * 从屏幕坐标计算位置
         */
        fun fromCoordinates(
            x: Float,
            y: Float,
            itemWidth: Float,
            itemHeight: Float,
            columns: Int,
            pageIndex: Int
        ): GridItemPosition {
            val column = (x / itemWidth).toInt().coerceIn(0, columns - 1)
            val row = (y / itemHeight).toInt().coerceIn(0, 3) // 最多 4 行
            return GridItemPosition(pageIndex, row, column)
        }
    }
}

/**
 * 位置交换信息
 */
data class PositionSwap(
    val fromPosition: GridItemPosition,
    val toPosition: GridItemPosition
) {
    /**
     * 是否在同一页面内移动
     */
    val isSamePage: Boolean get() = fromPosition.pageIndex == toPosition.pageIndex
}

/**
 * 拖拽目标信息
 */
data class DropTarget(
    val position: GridItemPosition,
    val isEmpty: Boolean,
    val currentItem: GridItem? = null
)
