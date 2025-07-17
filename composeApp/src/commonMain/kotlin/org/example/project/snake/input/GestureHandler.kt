package org.example.project.snake.input

import org.example.project.snake.model.Direction

/**
 * 手势处理器
 *
 * 简化版本，主要用于方向控制逻辑
 */
class GestureHandler {

    // 防抖动时间间隔（毫秒）
    private var lastInputTime = 0L
    private val inputDebounceTime = 200L

    /**
     * 处理方向输入（带防抖动）
     *
     * @param direction 输入的方向
     * @param onDirectionChange 方向改变回调
     */
    fun handleDirectionInput(
        direction: Direction,
        onDirectionChange: (Direction) -> Unit
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInputTime > inputDebounceTime) {
            lastInputTime = currentTime
            onDirectionChange(direction)
        }
    }

    /**
     * 重置输入状态
     *
     * 用于游戏重置时清除输入状态
     */
    fun reset() {
        lastInputTime = 0L
    }

    companion object {
        /**
         * 创建默认的手势处理器
         *
         * @return 配置好的手势处理器实例
         */
        fun createDefault(): GestureHandler {
            return GestureHandler()
        }
    }
}
