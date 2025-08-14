package org.example.project.snake.data

import kotlinx.serialization.Serializable
import org.example.project.snake.config.GameMode
import org.example.project.snake.config.Difficulty

/**
 * 玩家统计数据
 * 
 * 记录玩家的游戏统计信息，包括总体统计、每日统计、每周统计等
 */
@Serializable
data class PlayerStatistics(
    // 基础统计
    val totalGames: Int = 0,
    val totalScore: Long = 0L,
    val highestScore: Int = 0,
    val totalPlayTime: Long = 0L, // 毫秒
    val averageScore: Double = 0.0,
    val gamesWon: Int = 0,
    
    // 游戏内容统计
    val longestSnake: Int = 0,
    val totalFoodEaten: Int = 0,
    val totalEffectsUsed: Int = 0,
    
    // 按模式统计
    val gamesByMode: Map<GameMode, Int> = emptyMap(),
    val gamesByDifficulty: Map<Difficulty, Int> = emptyMap(),
    
    // 时间统计
    val dailyStats: Map<String, DailyStats> = emptyMap(), // 日期 -> 统计
    val weeklyStats: Map<String, WeeklyStats> = emptyMap(), // 周 -> 统计
    val monthlyStats: Map<String, MonthlyStats> = emptyMap(), // 月 -> 统计
    
    // 连续性统计
    val lastPlayDate: Long = 0L,
    val consecutiveDays: Int = 0,
    val bestStreak: Int = 0,
    
    // 成就相关
    val achievements: Set<String> = emptySet(),
    val achievementProgress: Map<String, Int> = emptyMap(),
    
    // 创建和更新时间
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    
    /**
     * 计算胜率
     */
    val winRate: Double
        get() = if (totalGames > 0) gamesWon.toDouble() / totalGames else 0.0
    
    /**
     * 计算平均游戏时长（分钟）
     */
    val averagePlayTimeMinutes: Double
        get() = if (totalGames > 0) (totalPlayTime / 1000.0 / 60.0) / totalGames else 0.0
    
    /**
     * 获取最喜欢的游戏模式
     */
    val favoriteGameMode: GameMode?
        get() = gamesByMode.maxByOrNull { it.value }?.key
    
    /**
     * 获取最常用的难度
     */
    val favoriteDifficulty: Difficulty?
        get() = gamesByDifficulty.maxByOrNull { it.value }?.key
    
    /**
     * 获取今日统计
     */
    fun getTodayStats(): DailyStats? {
        val today = getCurrentDateString()
        return dailyStats[today]
    }
    
    /**
     * 获取本周统计
     */
    fun getThisWeekStats(): WeeklyStats? {
        val thisWeek = getCurrentWeekString()
        return weeklyStats[thisWeek]
    }
    
    /**
     * 获取本月统计
     */
    fun getThisMonthStats(): MonthlyStats? {
        val thisMonth = getCurrentMonthString()
        return monthlyStats[thisMonth]
    }
    
    /**
     * 获取最近N天的统计
     */
    fun getRecentDaysStats(days: Int): List<DailyStats> {
        val recentDates = getRecentDates(days)
        return recentDates.mapNotNull { date ->
            dailyStats[date]
        }
    }
    
    /**
     * 获取排名信息
     */
    fun getRankingInfo(): RankingInfo {
        return RankingInfo(
            totalScore = totalScore,
            highestScore = highestScore,
            totalGames = totalGames,
            winRate = winRate,
            averageScore = averageScore,
            longestSnake = longestSnake,
            bestStreak = bestStreak
        )
    }
    
    private fun getCurrentDateString(): String {
        return kotlinx.datetime.Clock.System.now().toString().substring(0, 10)
    }
    
    private fun getCurrentWeekString(): String {
        val now = kotlinx.datetime.Clock.System.now()
        val year = now.toString().substring(0, 4)
        // 简化的周计算
        val dayOfYear = now.epochSeconds / (24 * 60 * 60) % 365
        val week = (dayOfYear / 7) + 1
        return "$year-W${week.toString().padStart(2, '0')}"
    }
    
    private fun getCurrentMonthString(): String {
        return kotlinx.datetime.Clock.System.now().toString().substring(0, 7)
    }
    
    private fun getRecentDates(days: Int): List<String> {
        val dates = mutableListOf<String>()
        val now = kotlinx.datetime.Clock.System.now()
        val currentDateStr = now.toString().substring(0, 10)
        
        // 简化实现，只返回当前日期
        for (i in 0 until days) {
            dates.add(currentDateStr)
        }
        
        return dates
    }
}

/**
 * 每日统计数据
 */
@Serializable
data class DailyStats(
    val date: String, // YYYY-MM-DD
    val gamesPlayed: Int = 0,
    val totalScore: Long = 0L,
    val highestScore: Int = 0,
    val totalPlayTime: Long = 0L,
    val foodEaten: Int = 0,
    val effectsUsed: Int = 0,
    val firstPlayTime: Long = 0L,
    val lastPlayTime: Long = 0L
) {
    val averageScore: Double
        get() = if (gamesPlayed > 0) totalScore.toDouble() / gamesPlayed else 0.0
    
    val averagePlayTime: Double
        get() = if (gamesPlayed > 0) totalPlayTime.toDouble() / gamesPlayed else 0.0
}

/**
 * 每周统计数据
 */
@Serializable
data class WeeklyStats(
    val week: String, // YYYY-WXX
    val gamesPlayed: Int = 0,
    val totalScore: Long = 0L,
    val highestScore: Int = 0,
    val totalPlayTime: Long = 0L,
    val averageScore: Double = 0.0,
    val daysPlayed: Int = 0
)

/**
 * 每月统计数据
 */
@Serializable
data class MonthlyStats(
    val month: String, // YYYY-MM
    val gamesPlayed: Int = 0,
    val totalScore: Long = 0L,
    val highestScore: Int = 0,
    val totalPlayTime: Long = 0L,
    val averageScore: Double = 0.0,
    val daysPlayed: Int = 0
)

/**
 * 排名信息
 */
@Serializable
data class RankingInfo(
    val totalScore: Long,
    val highestScore: Int,
    val totalGames: Int,
    val winRate: Double,
    val averageScore: Double,
    val longestSnake: Int,
    val bestStreak: Int
)

/**
 * 成就数据
 */
@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconResource: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L,
    val progress: Int = 0,
    val maxProgress: Int = 1,
    val category: AchievementCategory = AchievementCategory.GENERAL
)

/**
 * 成就类别
 */
@Serializable
enum class AchievementCategory {
    GENERAL,    // 通用成就
    SCORE,      // 分数相关
    SNAKE,      // 蛇长度相关
    FOOD,       // 食物相关
    TIME,       // 时间相关
    STREAK,     // 连续性相关
    MODE,       // 游戏模式相关
    SPECIAL     // 特殊成就
}