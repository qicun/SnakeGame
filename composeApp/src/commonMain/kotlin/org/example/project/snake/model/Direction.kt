package org.example.project.snake.model

/**
 * 表示蛇的移动方向
 * 
 * 定义了四个基本方向，并提供了获取相反方向的功能
 */
enum class Direction {
    UP,     // 向上
    DOWN,   // 向下
    LEFT,   // 向左
    RIGHT;  // 向右
    
    /**
     * 获取当前方向的相反方向
     * 
     * @return 相反的方向
     */
    fun opposite(): Direction {
        return when (this) {
            UP -> DOWN      // 上的相反是下
            DOWN -> UP      // 下的相反是上
            LEFT -> RIGHT   // 左的相反是右
            RIGHT -> LEFT   // 右的相反是左
        }
    }
    
    /**
     * 检查是否为水平方向（左或右）
     * 
     * @return 如果是水平方向返回true，否则返回false
     */
    fun isHorizontal(): Boolean {
        return this == LEFT || this == RIGHT
    }
    
    /**
     * 检查是否为垂直方向（上或下）
     * 
     * @return 如果是垂直方向返回true，否则返回false
     */
    fun isVertical(): Boolean {
        return this == UP || this == DOWN
    }
    
    /**
     * 检查两个方向是否垂直（互相垂直）
     * 
     * @param other 另一个方向
     * @return 如果两个方向垂直返回true，否则返回false
     */
    fun isPerpendicularTo(other: Direction): Boolean {
        return (this.isHorizontal() && other.isVertical()) || 
               (this.isVertical() && other.isHorizontal())
    }
}
