package org.example.project.snake.model

/**
 * 表示游戏的不同状态
 * 
 * 使用密封类确保状态的类型安全，便于when表达式的穷尽性检查
 */
sealed class GameState {
    
    /**
     * 游戏正在进行中
     * 
     * @param score 当前分数
     * @param level 当前等级（影响游戏速度）
     * @param speed 游戏速度（毫秒间隔）
     */
    data class Playing(
        val score: Int = 0,
        val level: Int = 1,
        val speed: Long = 500L  // 默认500毫秒移动一次
    ) : GameState() {
        
        /**
         * 增加分数并可能提升等级
         * 
         * @param points 要增加的分数
         * @return 更新后的Playing状态
         */
        fun addScore(points: Int): Playing {
            val newScore = score + points
            val newLevel = (newScore / 100) + 1  // 每100分升一级
            val newSpeed = maxOf(100L, 500L - (newLevel - 1) * 50L)  // 速度逐渐加快，最快100ms
            
            return copy(
                score = newScore,
                level = newLevel,
                speed = newSpeed
            )
        }
    }
    
    /**
     * 游戏暂停状态
     * 
     * @param previousState 暂停前的游戏状态，用于恢复游戏
     */
    data class Paused(
        val previousState: Playing
    ) : GameState() {
        
        /**
         * 恢复游戏到暂停前的状态
         * 
         * @return 暂停前的Playing状态
         */
        fun resume(): Playing = previousState
    }
    
    /**
     * 游戏结束状态
     * 
     * @param finalScore 最终分数
     * @param finalLevel 最终等级
     * @param reason 游戏结束的原因
     */
    data class GameOver(
        val finalScore: Int,
        val finalLevel: Int,
        val reason: GameOverReason
    ) : GameState()
    
    /**
     * 游戏结束的原因
     */
    enum class GameOverReason {
        WALL_COLLISION,    // 撞墙
        SELF_COLLISION     // 撞到自己
    }
    
    /**
     * 检查游戏是否处于活跃状态（正在进行或暂停）
     * 
     * @return 如果游戏处于活跃状态返回true，否则返回false
     */
    fun isActive(): Boolean {
        return this is Playing || this is Paused
    }
    
    /**
     * 获取当前分数（如果游戏处于活跃状态）
     * 
     * @return 当前分数，如果游戏结束则返回最终分数
     */
    fun getCurrentScore(): Int {
        return when (this) {
            is Playing -> score
            is Paused -> previousState.score
            is GameOver -> finalScore
        }
    }
}
