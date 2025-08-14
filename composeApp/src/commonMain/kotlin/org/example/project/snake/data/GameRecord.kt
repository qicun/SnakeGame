package org.example.project.snake.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import org.example.project.snake.config.GameMode
import org.example.project.snake.config.Difficulty
import org.example.project.snake.model.GameState
import org.example.project.snake.model.Position
import org.example.project.snake.model.Direction

/**
 * 游戏记录数据模型
 * 
 * 记录单局游戏的完整信息，用于统计分析和回放功能
 */
@Serializable
data class GameRecord(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val gameMode: GameMode,
    val difficulty: Difficulty,
    val finalScore: Int,
    val maxSnakeLength: Int,
    val playTime: Long, // 游戏时长（毫秒）
    val foodEaten: Int,
    val effectsUsed: Int,
    val gameOverReason: GameState.GameOverReason,
    val playerName: String = "Player",
    val replayData: ReplayData? = null
) {
    
    /**
     * 计算每分钟得分
     */
    fun getScorePerMinute(): Double {
        val minutes = playTime / 60000.0
        return if (minutes > 0) finalScore / minutes else 0.0
    }
    
    /**
     * 计算游戏效率（分数/时间/难度系数）
     */
    fun getGameEfficiency(): Double {
        val minutes = playTime / 60000.0
        val difficultyMultiplier = difficulty.scoreMultiplier
        return if (minutes > 0) (finalScore / minutes) * difficultyMultiplier else 0.0
    }
    
    /**
     * 获取游戏时长的格式化字符串
     */
    fun getFormattedPlayTime(): String {
        val seconds = playTime / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "${minutes}分${remainingSeconds}秒"
        } else {
            "${remainingSeconds}秒"
        }
    }
    
    /**
     * 检查是否为高分游戏
     */
    fun isHighScore(threshold: Int = 100): Boolean {
        return finalScore >= threshold
    }
    
    /**
     * 检查是否为长时间游戏
     */
    fun isLongGame(thresholdMinutes: Int = 5): Boolean {
        return playTime >= thresholdMinutes * 60 * 1000
    }
    
    companion object {
        
        /**
         * 创建游戏记录
         */
        fun create(
            gameMode: GameMode,
            difficulty: Difficulty,
            finalScore: Int,
            maxSnakeLength: Int,
            playTime: Long,
            foodEaten: Int,
            effectsUsed: Int,
            gameOverReason: GameState.GameOverReason,
            playerName: String = "Player",
            replayData: ReplayData? = null
        ): GameRecord {
            return GameRecord(
                id = generateId(),
                timestamp = System.currentTimeMillis(),
                gameMode = gameMode,
                difficulty = difficulty,
                finalScore = finalScore,
                maxSnakeLength = maxSnakeLength,
                playTime = playTime,
                foodEaten = foodEaten,
                effectsUsed = effectsUsed,
                gameOverReason = gameOverReason,
                playerName = playerName,
                replayData = replayData
            )
        }
        
        /**
         * 生成唯一ID
         */
        private fun generateId(): String {
            return "game_${System.currentTimeMillis()}_${(0..9999).random()}"
        }
    }
}

/**
 * 回放移动记录
 */
@Serializable
data class ReplayMove(
    val timestamp: Long,
    val direction: Direction,
    @Contextual val snakeHead: Position,
    val snakeLength: Int,
    val score: Int
)

/**
 * 回放食物记录
 */
@Serializable
data class ReplayFood(
    val timestamp: Long,
    val position: Position,
    val type: String, // FoodType的字符串表示
    val consumed: Boolean = false
)

/**
 * 回放游戏配置
 */
@Serializable
data class ReplayGameConfig(
    val gameMode: GameMode,
    val difficulty: Difficulty,
    val gameWidth: Int,
    val gameHeight: Int,
    val enableEffects: Boolean
)
