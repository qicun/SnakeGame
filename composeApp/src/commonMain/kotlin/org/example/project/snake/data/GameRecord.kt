package org.example.project.snake.data

import kotlinx.serialization.Serializable
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
 * 回放数据模型
 * 
 * 记录游戏过程中的所有操作，用于回放功能
 */
@Serializable
data class ReplayData(
    val moves: List<ReplayMove>,
    val foodPositions: List<ReplayFood>,
    val gameConfig: ReplayGameConfig,
    val duration: Long,
    val version: String = "1.0"
) {
    
    /**
     * 获取回放时长的格式化字符串
     */
    fun getFormattedDuration(): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "${minutes}:${remainingSeconds.toString().padStart(2, '0')}"
        } else {
            "0:${remainingSeconds.toString().padStart(2, '0')}"
        }
    }
    
    /**
     * 检查回放数据是否有效
     */
    fun isValid(): Boolean {
        return moves.isNotEmpty() && 
               foodPositions.isNotEmpty() && 
               duration > 0
    }
    
    /**
     * 获取回放数据大小（估算）
     */
    fun getDataSize(): Int {
        return moves.size * 20 + foodPositions.size * 12 + 100 // 估算字节数
    }
}

/**
 * 回放移动记录
 */
@Serializable
data class ReplayMove(
    val timestamp: Long,
    val direction: Direction,
    val snakeHead: Position,
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

/**
 * 玩家统计数据模型
 * 
 * 记录玩家的累计统计信息
 */
@Serializable
data class PlayerStatistics(
    val totalGames: Int = 0,
    val totalScore: Long = 0,
    val highestScore: Int = 0,
    val totalPlayTime: Long = 0, // 总游戏时长（毫秒）
    val averageScore: Double = 0.0,
    val gamesWon: Int = 0, // 这里定义为达到一定分数的游戏
    val longestSnake: Int = 0,
    val totalFoodEaten: Int = 0,
    val totalEffectsUsed: Int = 0,
    val gamesByMode: Map<GameMode, Int> = emptyMap(),
    val gamesByDifficulty: Map<Difficulty, Int> = emptyMap(),
    val dailyStats: Map<String, DailyStats> = emptyMap(), // 日期 -> 统计
    val weeklyStats: Map<String, WeeklyStats> = emptyMap(), // 周 -> 统计
    val monthlyStats: Map<String, MonthlyStats> = emptyMap(), // 月 -> 统计
    val achievements: Set<String> = emptySet(),
    val lastPlayDate: Long = 0,
    val consecutiveDays: Int = 0,
    val bestStreak: Int = 0
) {
    
    /**
     * 计算平均游戏时长
     */
    fun getAveragePlayTime(): Long {
        return if (totalGames > 0) totalPlayTime / totalGames else 0
    }
    
    /**
     * 计算胜率
     */
    fun getWinRate(): Double {
        return if (totalGames > 0) gamesWon.toDouble() / totalGames else 0.0
    }
    
    /**
     * 获取最喜欢的游戏模式
     */
    fun getFavoriteGameMode(): GameMode? {
        return gamesByMode.maxByOrNull { it.value }?.key
    }
    
    /**
     * 获取最常用的难度
     */
    fun getFavoriteDifficulty(): Difficulty? {
        return gamesByDifficulty.maxByOrNull { it.value }?.key
    }
    
    /**
     * 计算总游戏时长的格式化字符串
     */
    fun getFormattedTotalPlayTime(): String {
        val totalSeconds = totalPlayTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return when {
            hours > 0 -> "${hours}小时${minutes}分钟"
            minutes > 0 -> "${minutes}分钟${seconds}秒"
            else -> "${seconds}秒"
        }
    }
    
    /**
     * 检查是否为活跃玩家
     */
    fun isActivePlayer(): Boolean {
        val daysSinceLastPlay = (System.currentTimeMillis() - lastPlayDate) / (24 * 60 * 60 * 1000)
        return daysSinceLastPlay <= 7 && totalGames >= 10
    }
    
    /**
     * 获取玩家等级（基于总分数）
     */
    fun getPlayerLevel(): Int {
        return when {
            totalScore < 500 -> 1
            totalScore < 1500 -> 2
            totalScore < 3000 -> 3
            totalScore < 6000 -> 4
            totalScore < 10000 -> 5
            totalScore < 15000 -> 6
            totalScore < 25000 -> 7
            totalScore < 40000 -> 8
            totalScore < 60000 -> 9
            else -> 10
        }
    }
    
    /**
     * 获取下一等级所需分数
     */
    fun getScoreToNextLevel(): Long {
        val currentLevel = getPlayerLevel()
        val nextLevelThreshold = when (currentLevel) {
            1 -> 500L
            2 -> 1500L
            3 -> 3000L
            4 -> 6000L
            5 -> 10000L
            6 -> 15000L
            7 -> 25000L
            8 -> 40000L
            9 -> 60000L
            else -> 0L // 已达到最高等级
        }
        return maxOf(0, nextLevelThreshold - totalScore)
    }
}

/**
 * 每日统计数据
 */
@Serializable
data class DailyStats(
    val date: String, // YYYY-MM-DD格式
    val gamesPlayed: Int = 0,
    val totalScore: Int = 0,
    val highestScore: Int = 0,
    val totalPlayTime: Long = 0,
    val foodEaten: Int = 0,
    val effectsUsed: Int = 0
)

/**
 * 每周统计数据
 */
@Serializable
data class WeeklyStats(
    val week: String, // YYYY-WW格式
    val gamesPlayed: Int = 0,
    val totalScore: Long = 0,
    val highestScore: Int = 0,
    val totalPlayTime: Long = 0,
    val averageScore: Double = 0.0,
    val bestDay: String = "" // 本周表现最好的日期
)

/**
 * 每月统计数据
 */
@Serializable
data class MonthlyStats(
    val month: String, // YYYY-MM格式
    val gamesPlayed: Int = 0,
    val totalScore: Long = 0,
    val highestScore: Int = 0,
    val totalPlayTime: Long = 0,
    val averageScore: Double = 0.0,
    val bestWeek: String = "", // 本月表现最好的周
    val improvement: Double = 0.0 // 相比上月的改进百分比
)

/**
 * 排行榜条目数据模型
 */
@Serializable
data class LeaderboardEntry(
    val id: String,
    val playerName: String,
    val score: Int,
    val gameMode: GameMode,
    val difficulty: Difficulty,
    val timestamp: Long,
    val playTime: Long,
    val snakeLength: Int,
    val rank: Int = 0
) {
    
    /**
     * 计算分数效率
     */
    fun getScoreEfficiency(): Double {
        val minutes = playTime / 60000.0
        return if (minutes > 0) score / minutes else 0.0
    }
    
    /**
     * 获取格式化的时间戳
     */
    fun getFormattedDate(): String {
        val date = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
        return date.toString().substring(0, 10) // YYYY-MM-DD
    }
    
    companion object {
        
        /**
         * 从游戏记录创建排行榜条目
         */
        fun fromGameRecord(record: GameRecord): LeaderboardEntry {
            return LeaderboardEntry(
                id = record.id,
                playerName = record.playerName,
                score = record.finalScore,
                gameMode = record.gameMode,
                difficulty = record.difficulty,
                timestamp = record.timestamp,
                playTime = record.playTime,
                snakeLength = record.maxSnakeLength
            )
        }
    }
}