package org.example.project.snake.data

import kotlinx.serialization.Serializable
import org.example.project.snake.config.GameMode
import org.example.project.snake.config.Difficulty

/**
 * 排行榜条目
 * 
 * 记录单次游戏的排行榜信息
 */
@Serializable
data class LeaderboardEntry(
    val id: String,
    val playerName: String = "Player",
    val score: Int,
    val snakeLength: Int,
    val gameMode: GameMode,
    val difficulty: Difficulty,
    val playTime: Long, // 游戏时长（毫秒）
    val foodEaten: Int,
    val effectsUsed: Int,
    val timestamp: Long,
    val rank: Int = 0, // 排名，由系统计算
    val isPersonalBest: Boolean = false, // 是否为个人最佳
    val gameVersion: String = "1.0"
) {
    
    /**
     * 计算综合评分
     * 用于排行榜排序的综合指标
     */
    val compositeScore: Double
        get() {
            val baseScore = score.toDouble()
            val lengthBonus = snakeLength * 10.0
            val timeBonus = if (playTime > 0) (score.toDouble() / (playTime / 1000.0)) * 100 else 0.0
            val difficultyMultiplier = when (difficulty) {
                Difficulty.EASY -> 1.0
                Difficulty.NORMAL -> 1.2
                Difficulty.HARD -> 1.5
                Difficulty.EXPERT -> 2.0
            }
            val modeMultiplier = when (gameMode) {
                GameMode.CLASSIC -> 1.0
                GameMode.ARCADE -> 1.1
                GameMode.SURVIVAL -> 1.3
                GameMode.TIME_ATTACK -> 1.2
                GameMode.CHALLENGE -> 1.4
            }
            
            return (baseScore + lengthBonus + timeBonus) * difficultyMultiplier * modeMultiplier
        }
    
    /**
     * 获取显示用的游戏时长
     */
    val formattedPlayTime: String
        get() {
            val seconds = playTime / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return if (minutes > 0) {
                "${minutes}m ${remainingSeconds}s"
            } else {
                "${remainingSeconds}s"
            }
        }
    
    /**
     * 获取效率评分（分数/时间）
     */
    val efficiency: Double
        get() = if (playTime > 0) score.toDouble() / (playTime / 1000.0) else 0.0
    
    /**
     * 检查是否为有效记录
     */
    val isValid: Boolean
        get() = score > 0 && snakeLength >= 1 && playTime > 0
    
    /**
     * 获取排行榜显示信息
     */
    fun getDisplayInfo(): LeaderboardDisplayInfo {
        return LeaderboardDisplayInfo(
            rank = rank,
            playerName = playerName,
            score = score,
            snakeLength = snakeLength,
            gameMode = gameMode.displayName,
            difficulty = difficulty.displayName,
            playTime = formattedPlayTime,
            efficiency = String.format("%.1f", efficiency),
            timestamp = timestamp,
            isPersonalBest = isPersonalBest
        )
    }
}

/**
 * 排行榜显示信息
 */
@Serializable
data class LeaderboardDisplayInfo(
    val rank: Int,
    val playerName: String,
    val score: Int,
    val snakeLength: Int,
    val gameMode: String,
    val difficulty: String,
    val playTime: String,
    val efficiency: String,
    val timestamp: Long,
    val isPersonalBest: Boolean
)

/**
 * 排行榜查询参数
 */
data class LeaderboardQuery(
    val gameMode: GameMode? = null,
    val difficulty: Difficulty? = null,
    val timeRange: TimeRange? = null,
    val limit: Int = 100,
    val offset: Int = 0,
    val sortBy: LeaderboardSortBy = LeaderboardSortBy.SCORE,
    val sortOrder: SortOrder = SortOrder.DESCENDING
)

/**
 * 排行榜排序方式
 */
enum class LeaderboardSortBy {
    SCORE,          // 按分数排序
    SNAKE_LENGTH,   // 按蛇长度排序
    PLAY_TIME,      // 按游戏时长排序
    EFFICIENCY,     // 按效率排序
    COMPOSITE_SCORE,// 按综合评分排序
    TIMESTAMP       // 按时间排序
}

/**
 * 排序顺序
 */
enum class SortOrder {
    ASCENDING,      // 升序
    DESCENDING      // 降序
}

/**
 * 时间范围
 */
enum class TimeRange {
    TODAY,          // 今天
    THIS_WEEK,      // 本周
    THIS_MONTH,     // 本月
    ALL_TIME        // 全部时间
}

/**
 * 排行榜统计信息
 */
@Serializable
data class LeaderboardStats(
    val totalEntries: Int,
    val averageScore: Double,
    val highestScore: Int,
    val averageSnakeLength: Double,
    val longestSnake: Int,
    val averagePlayTime: Double,
    val totalPlayTime: Long,
    val playerCount: Int,
    val lastUpdated: Long
)

/**
 * 个人排行榜信息
 */
@Serializable
data class PersonalLeaderboardInfo(
    val bestScore: Int,
    val bestScoreRank: Int,
    val longestSnake: Int,
    val longestSnakeRank: Int,
    val bestEfficiency: Double,
    val bestEfficiencyRank: Int,
    val totalEntries: Int,
    val averageRank: Double,
    val improvementTrend: Double, // 最近的排名改善趋势
    val recentBests: List<LeaderboardEntry> // 最近的个人最佳记录
)

/**
 * 排行榜管理器
 * 
 * 负责排行榜的计算、排序和统计
 */
class LeaderboardManager {
    
    /**
     * 计算排行榜排名
     */
    fun calculateRanks(entries: List<LeaderboardEntry>, sortBy: LeaderboardSortBy): List<LeaderboardEntry> {
        val sortedEntries = when (sortBy) {
            LeaderboardSortBy.SCORE -> entries.sortedByDescending { it.score }
            LeaderboardSortBy.SNAKE_LENGTH -> entries.sortedByDescending { it.snakeLength }
            LeaderboardSortBy.PLAY_TIME -> entries.sortedBy { it.playTime }
            LeaderboardSortBy.EFFICIENCY -> entries.sortedByDescending { it.efficiency }
            LeaderboardSortBy.COMPOSITE_SCORE -> entries.sortedByDescending { it.compositeScore }
            LeaderboardSortBy.TIMESTAMP -> entries.sortedByDescending { it.timestamp }
        }
        
        return sortedEntries.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }
    
    /**
     * 过滤排行榜条目
     */
    fun filterEntries(entries: List<LeaderboardEntry>, query: LeaderboardQuery): List<LeaderboardEntry> {
        var filtered = entries
        
        // 按游戏模式过滤
        query.gameMode?.let { mode ->
            filtered = filtered.filter { it.gameMode == mode }
        }
        
        // 按难度过滤
        query.difficulty?.let { difficulty ->
            filtered = filtered.filter { it.difficulty == difficulty }
        }
        
        // 按时间范围过滤
        query.timeRange?.let { timeRange ->
            val timeFilter = getTimeRangeFilter(timeRange)
            filtered = filtered.filter { timeFilter(it.timestamp) }
        }
        
        return filtered
    }
    
    /**
     * 计算排行榜统计信息
     */
    fun calculateStats(entries: List<LeaderboardEntry>): LeaderboardStats {
        if (entries.isEmpty()) {
            return LeaderboardStats(
                totalEntries = 0,
                averageScore = 0.0,
                highestScore = 0,
                averageSnakeLength = 0.0,
                longestSnake = 0,
                averagePlayTime = 0.0,
                totalPlayTime = 0L,
                playerCount = 0,
                lastUpdated = System.currentTimeMillis()
            )
        }
        
        val totalScore = entries.sumOf { it.score.toLong() }
        val totalSnakeLength = entries.sumOf { it.snakeLength }
        val totalPlayTime = entries.sumOf { it.playTime }
        val uniquePlayers = entries.map { it.playerName }.toSet().size
        
        return LeaderboardStats(
            totalEntries = entries.size,
            averageScore = totalScore.toDouble() / entries.size,
            highestScore = entries.maxOfOrNull { it.score } ?: 0,
            averageSnakeLength = totalSnakeLength.toDouble() / entries.size,
            longestSnake = entries.maxOfOrNull { it.snakeLength } ?: 0,
            averagePlayTime = totalPlayTime.toDouble() / entries.size,
            totalPlayTime = totalPlayTime,
            playerCount = uniquePlayers,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * 获取个人排行榜信息
     */
    fun getPersonalInfo(entries: List<LeaderboardEntry>, playerName: String): PersonalLeaderboardInfo {
        val playerEntries = entries.filter { it.playerName == playerName }
        
        if (playerEntries.isEmpty()) {
            return PersonalLeaderboardInfo(
                bestScore = 0,
                bestScoreRank = 0,
                longestSnake = 0,
                longestSnakeRank = 0,
                bestEfficiency = 0.0,
                bestEfficiencyRank = 0,
                totalEntries = 0,
                averageRank = 0.0,
                improvementTrend = 0.0,
                recentBests = emptyList()
            )
        }
        
        val bestScore = playerEntries.maxOfOrNull { it.score } ?: 0
        val longestSnake = playerEntries.maxOfOrNull { it.snakeLength } ?: 0
        val bestEfficiency = playerEntries.maxOfOrNull { it.efficiency } ?: 0.0
        
        // 计算排名
        val scoreRankedEntries = calculateRanks(entries, LeaderboardSortBy.SCORE)
        val lengthRankedEntries = calculateRanks(entries, LeaderboardSortBy.SNAKE_LENGTH)
        val efficiencyRankedEntries = calculateRanks(entries, LeaderboardSortBy.EFFICIENCY)
        
        val bestScoreRank = scoreRankedEntries.find { it.playerName == playerName && it.score == bestScore }?.rank ?: 0
        val longestSnakeRank = lengthRankedEntries.find { it.playerName == playerName && it.snakeLength == longestSnake }?.rank ?: 0
        val bestEfficiencyRank = efficiencyRankedEntries.find { it.playerName == playerName && it.efficiency == bestEfficiency }?.rank ?: 0
        
        val averageRank = playerEntries.map { entry ->
            scoreRankedEntries.find { it.id == entry.id }?.rank ?: 0
        }.filter { it > 0 }.average()
        
        // 计算改善趋势（最近10场游戏的排名趋势）
        val recentEntries = playerEntries.sortedByDescending { it.timestamp }.take(10)
        val improvementTrend = calculateImprovementTrend(recentEntries, scoreRankedEntries)
        
        // 获取最近的个人最佳记录
        val recentBests = playerEntries.filter { it.isPersonalBest }
            .sortedByDescending { it.timestamp }
            .take(5)
        
        return PersonalLeaderboardInfo(
            bestScore = bestScore,
            bestScoreRank = bestScoreRank,
            longestSnake = longestSnake,
            longestSnakeRank = longestSnakeRank,
            bestEfficiency = bestEfficiency,
            bestEfficiencyRank = bestEfficiencyRank,
            totalEntries = playerEntries.size,
            averageRank = averageRank,
            improvementTrend = improvementTrend,
            recentBests = recentBests
        )
    }
    
    /**
     * 获取时间范围过滤器
     */
    private fun getTimeRangeFilter(timeRange: TimeRange): (Long) -> Boolean {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneWeekMs = 7 * oneDayMs
        val oneMonthMs = 30 * oneDayMs
        
        return when (timeRange) {
            TimeRange.TODAY -> { timestamp -> now - timestamp <= oneDayMs }
            TimeRange.THIS_WEEK -> { timestamp -> now - timestamp <= oneWeekMs }
            TimeRange.THIS_MONTH -> { timestamp -> now - timestamp <= oneMonthMs }
            TimeRange.ALL_TIME -> { _ -> true }
        }
    }
    
    /**
     * 计算改善趋势
     */
    private fun calculateImprovementTrend(recentEntries: List<LeaderboardEntry>, rankedEntries: List<LeaderboardEntry>): Double {
        if (recentEntries.size < 2) return 0.0
        
        val recentRanks = recentEntries.mapNotNull { entry ->
            rankedEntries.find { it.id == entry.id }?.rank
        }
        
        if (recentRanks.size < 2) return 0.0
        
        // 计算排名变化趋势（排名越小越好，所以改善是负数）
        val firstHalf = recentRanks.take(recentRanks.size / 2).average()
        val secondHalf = recentRanks.drop(recentRanks.size / 2).average()
        
        return firstHalf - secondHalf // 正数表示排名改善
    }
}
