package org.example.project.snake.config

import org.example.project.snake.model.Position

/**
 * 游戏配置数据类
 * 
 * 统一管理游戏的各种配置参数，支持不同的游戏模式和难度设置
 * 
 * @param gameWidth 游戏区域宽度（网格列数）
 * @param gameHeight 游戏区域高度（网格行数）
 * @param gameMode 游戏模式
 * @param difficulty 游戏难度
 * @param initialSpeed 初始游戏速度（毫秒间隔）
 * @param enableEffects 是否启用特殊食物效果
 * @param maxObstacles 最大障碍物数量（仅在障碍物模式下有效）
 * @param timeLimitSeconds 时间限制（秒，仅在时间挑战模式下有效）
 */
data class GameConfig(
    val gameWidth: Int = 20,
    val gameHeight: Int = 20,
    val gameMode: GameMode = GameMode.CLASSIC,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val initialSpeed: Long = 500L,
    val enableEffects: Boolean = true,
    val maxObstacles: Int = 5,
    val timeLimitSeconds: Int = 120
) {
    
    /**
     * 根据难度调整游戏速度
     * 
     * @return 调整后的游戏速度
     */
    fun getAdjustedSpeed(): Long {
        return when (difficulty) {
            Difficulty.EASY -> (initialSpeed * 1.5).toLong()
            Difficulty.NORMAL -> initialSpeed
            Difficulty.HARD -> (initialSpeed * 0.7).toLong()
            Difficulty.EXPERT -> (initialSpeed * 0.5).toLong()
        }
    }
    
    /**
     * 获取初始蛇的长度
     * 
     * @return 根据难度调整的初始蛇长度
     */
    fun getInitialSnakeLength(): Int {
        return when (difficulty) {
            Difficulty.EASY -> 2
            Difficulty.NORMAL -> 3
            Difficulty.HARD -> 4
            Difficulty.EXPERT -> 5
        }
    }
    
    /**
     * 获取分数倍数
     * 
     * @return 根据难度调整的分数倍数
     */
    fun getScoreMultiplier(): Float {
        return when (difficulty) {
            Difficulty.EASY -> 0.8f
            Difficulty.NORMAL -> 1.0f
            Difficulty.HARD -> 1.5f
            Difficulty.EXPERT -> 2.0f
        }
    }
    
    /**
     * 检查游戏配置是否有效
     * 
     * @return 如果配置有效返回true，否则返回false
     */
    fun isValid(): Boolean {
        return gameWidth > 5 && 
               gameHeight > 5 && 
               initialSpeed > 50 && 
               maxObstacles >= 0 && 
               timeLimitSeconds > 0
    }
    
    /**
     * 创建默认配置的副本，只修改指定参数
     */
    companion object {
        /**
         * 创建简单模式配置
         */
        fun createEasyConfig(): GameConfig {
            return GameConfig(
                gameWidth = 15,
                gameHeight = 15,
                difficulty = Difficulty.EASY,
                initialSpeed = 600L,
                enableEffects = false
            )
        }
        
        /**
         * 创建专家模式配置
         */
        fun createExpertConfig(): GameConfig {
            return GameConfig(
                gameWidth = 25,
                gameHeight = 25,
                difficulty = Difficulty.EXPERT,
                initialSpeed = 200L,
                enableEffects = true,
                maxObstacles = 10
            )
        }
        
        /**
         * 创建时间挑战模式配置
         */
        fun createTimeChallengeConfig(): GameConfig {
            return GameConfig(
                gameMode = GameMode.TIME_CHALLENGE,
                difficulty = Difficulty.NORMAL,
                timeLimitSeconds = 60,
                enableEffects = true
            )
        }
    }
}