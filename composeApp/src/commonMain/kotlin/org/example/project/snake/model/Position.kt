package org.example.project.snake.model

/**
 * 表示游戏中的二维坐标位置
 * 
 * @param x 横坐标，表示在游戏网格中的列位置
 * @param y 纵坐标，表示在游戏网格中的行位置
 */
data class Position(
    val x: Int,
    val y: Int
) {
    /**
     * 根据给定方向移动到新位置
     * 
     * @param direction 移动方向
     * @return 移动后的新位置
     */
    fun move(direction: Direction): Position {
        return when (direction) {
            Direction.UP -> Position(x, y - 1)      // 向上移动，y坐标减1
            Direction.DOWN -> Position(x, y + 1)    // 向下移动，y坐标加1
            Direction.LEFT -> Position(x - 1, y)    // 向左移动，x坐标减1
            Direction.RIGHT -> Position(x + 1, y)   // 向右移动，x坐标加1
        }
    }
    
    /**
     * 检查位置是否在指定的边界内
     * 
     * @param width 游戏区域宽度（网格列数）
     * @param height 游戏区域高度（网格行数）
     * @return 如果位置在边界内返回true，否则返回false
     */
    fun isInBounds(width: Int, height: Int): Boolean {
        return x >= 0 && x < width && y >= 0 && y < height
    }
    
    /**
     * 计算与另一个位置的曼哈顿距离
     * 
     * @param other 另一个位置
     * @return 曼哈顿距离（|x1-x2| + |y1-y2|）
     */
    fun manhattanDistanceTo(other: Position): Int {
        return kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)
    }
}
