package org.example.project.snake.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.example.project.snake.storage.PlatformStorage
import org.example.project.snake.storage.StorageKeys
import org.example.project.snake.config.GameConfig
import org.example.project.snake.config.GameMode
import org.example.project.snake.config.Difficulty

/**
 * 游戏数据仓库接口
 * 
 * 定义所有数据操作的抽象接口
 */
interface GameDataRepository {
    
    // 游戏记录相关
    suspend fun saveGameRecord(record: GameRecord)
    suspend fun getGameRecords(limit: Int = 100): List<GameRecord>
    suspend fun getGameRecordById(id: String): GameRecord?
    suspend fun deleteGameRecord(id: String)
    suspend fun clearGameRecords()
    
    // 玩家统计相关
    suspend fun getPlayerStatistics(): PlayerStatistics
    suspend fun updatePlayerStatistics(statistics: PlayerStatistics)
    suspend fun resetPlayerStatistics()
    
    // 排行榜相关
    suspend fun saveLeaderboardEntry(entry: LeaderboardEntry)
    suspend fun getLeaderboard(gameMode: GameMode?, difficulty: Difficulty?, limit: Int = 50): List<LeaderboardEntry>
    suspend fun getPlayerRank(playerName: String, gameMode: GameMode?, difficulty: Difficulty?): Int?
    suspend fun clearLeaderboard()
    
    // 配置相关
    suspend fun saveGameConfig(config: GameConfig)
    suspend fun getGameConfig(): GameConfig
    
    // 回放数据相关
    suspend fun saveReplayData(gameId: String, replayData: ReplayData)
    suspend fun getReplayData(gameId: String): ReplayData?
    suspend fun deleteReplayData(gameId: String)
    suspend fun getAllReplayIds(): List<String>
    
    // 数据管理
    suspend fun exportAllData(): String
    suspend fun importAllData(data: String): Boolean
    suspend fun clearAllData()
    suspend fun getDataSize(): Long
}

/**
 * 游戏数据仓库实现
 * 
 * 使用PlatformStorage实现数据持久化
 */
class GameDataRepositoryImpl(
    private val storage: PlatformStorage
) : GameDataRepository {
    
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // 内存缓存
    private var cachedStatistics: PlayerStatistics? = null
    private var cachedConfig: GameConfig? = null
    
    override suspend fun saveGameRecord(record: GameRecord) = withContext(Dispatchers.Default) {
        try {
            val records = getGameRecords().toMutableList()
            
            // 移除相同ID的记录（如果存在）
            records.removeAll { it.id == record.id }
            
            // 添加新记录到开头
            records.add(0, record)
            
            // 保持最多1000条记录
            if (records.size > 1000) {
                records.removeAt(records.size - 1)
            }
            
            val jsonString = json.encodeToString(records)
            storage.saveString(StorageKeys.GAME_RECORDS, jsonString)
            
            // 同时保存到排行榜
            val leaderboardEntry = LeaderboardEntry.fromGameRecord(record)
            saveLeaderboardEntry(leaderboardEntry)
            
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to save game record", e)
        }
    }
    
    override suspend fun getGameRecords(limit: Int): List<GameRecord> = withContext(Dispatchers.Default) {
        try {
            val jsonString = storage.getString(StorageKeys.GAME_RECORDS, "[]")
            val records = json.decodeFromString<List<GameRecord>>(jsonString)
            records.take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getGameRecordById(id: String): GameRecord? = withContext(Dispatchers.Default) {
        try {
            val records = getGameRecords(1000) // 搜索更多记录
            records.find { it.id == id }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun deleteGameRecord(id: String) = withContext(Dispatchers.Default) {
        try {
            val records = getGameRecords(1000).toMutableList()
            records.removeAll { it.id == id }
            
            val jsonString = json.encodeToString(records)
            storage.saveString(StorageKeys.GAME_RECORDS, jsonString)
            
            // 同时删除回放数据
            deleteReplayData(id)
            
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to delete game record", e)
        }
    }
    
    override suspend fun clearGameRecords() = withContext(Dispatchers.Default) {
        try {
            storage.remove(StorageKeys.GAME_RECORDS)
            
            // 同时清除所有回放数据
            val replayIds = getAllReplayIds()
            replayIds.forEach { deleteReplayData(it) }
            
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to clear game records", e)
        }
    }
    
    override suspend fun getPlayerStatistics(): PlayerStatistics = withContext(Dispatchers.Default) {
        try {
            // 先检查缓存
            cachedStatistics?.let { return@withContext it }
            
            val jsonString = storage.getString(StorageKeys.PLAYER_STATISTICS, "")
            val statistics = if (jsonString.isNotEmpty()) {
                json.decodeFromString<PlayerStatistics>(jsonString)
            } else {
                PlayerStatistics()
            }
            
            // 缓存结果
            cachedStatistics = statistics
            statistics
            
        } catch (e: Exception) {
            PlayerStatistics()
        }
    }
    
    override suspend fun updatePlayerStatistics(statistics: PlayerStatistics) = withContext(Dispatchers.Default) {
        try {
            val jsonString = json.encodeToString(statistics)
            storage.saveString(StorageKeys.PLAYER_STATISTICS, jsonString)
            
            // 更新缓存
            cachedStatistics = statistics
            
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to update player statistics", e)
        }
    }
    
    override suspend fun resetPlayerStatistics() = withContext(Dispatchers.Default) {
        try {
            storage.remove(StorageKeys.PLAYER_STATISTICS)
            cachedStatistics = null
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to reset player statistics", e)
        }
    }
    
    override suspend fun saveLeaderboardEntry(entry: LeaderboardEntry) = withContext(Dispatchers.Default) {
        try {
            val entries = getLeaderboard(null, null, 1000).toMutableList()
            
            // 移除相同ID的条目
            entries.removeAll { it.id == entry.id }
            
            // 添加新条目
            entries.add(entry)
            
            // 按分数排序
            entries.sortByDescending { it.score }
            
            // 更新排名
            entries.forEachIndexed { index, leaderboardEntry ->
                entries[index] = leaderboardEntry.copy(rank = index + 1)
            }
            
            // 保持最多500条记录
            val limitedEntries = entries.take(500)
            
            val jsonString = json.encodeToString(limitedEntries)
            storage.saveString(StorageKeys.LEADERBOARD_ENTRIES, jsonString)
            
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to save leaderboard entry", e)
        }
    }
    
    override suspend fun getLeaderboard(
        gameMode: GameMode?, 
        difficulty: Difficulty?, 
        limit: Int
    ): List<LeaderboardEntry> = withContext(Dispatchers.Default) {
        try {
            val jsonString = storage.getString(StorageKeys.LEADERBOARD_ENTRIES, "[]")
            val allEntries = json.decodeFromString<List<LeaderboardEntry>>(jsonString)
            
            val filteredEntries = allEntries.filter { entry ->
                (gameMode == null || entry.gameMode == gameMode) &&
                (difficulty == null || entry.difficulty == difficulty)
            }
            
            // 重新计算排名
            val rankedEntries = filteredEntries.sortedByDescending { it.score }
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
            
            rankedEntries.take(limit)
            
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getPlayerRank(
        playerName: String, 
        gameMode: GameMode?, 
        difficulty: Difficulty?
    ): Int? = withContext(Dispatchers.Default) {
        try {
            val leaderboard = getLeaderboard(gameMode, difficulty, 1000)
            leaderboard.find { it.playerName == playerName }?.rank
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun clearLeaderboard() = withContext(Dispatchers.Default) {
        try {
            storage.remove(StorageKeys.LEADERBOARD_ENTRIES)
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to clear leaderboard", e)
        }
    }
    
    override suspend fun saveGameConfig(config: GameConfig) = withContext(Dispatchers.Default) {
        try {
            val jsonString = json.encodeToString(config)
            storage.saveString(StorageKeys.GAME_CONFIG, jsonString)
            
            // 更新缓存
            cachedConfig = config
            
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to save game config", e)
        }
    }
    
    override suspend fun getGameConfig(): GameConfig = withContext(Dispatchers.Default) {
        try {
            // 先检查缓存
            cachedConfig?.let { return@withContext it }
            
            val jsonString = storage.getString(StorageKeys.GAME_CONFIG, "")
            val config = if (jsonString.isNotEmpty()) {
                json.decodeFromString<GameConfig>(jsonString)
            } else {
                GameConfig()
            }
            
            // 缓存结果
            cachedConfig = config
            config
            
        } catch (e: Exception) {
            GameConfig()
        }
    }
    
    override suspend fun saveReplayData(gameId: String, replayData: ReplayData) = withContext(Dispatchers.Default) {
        try {
            val key = "${StorageKeys.REPLAY_DATA}_$gameId"
            val jsonString = json.encodeToString(replayData)
            storage.saveString(key, jsonString)
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to save replay data", e)
        }
    }
    
    override suspend fun getReplayData(gameId: String): ReplayData? = withContext(Dispatchers.Default) {
        try {
            val key = "${StorageKeys.REPLAY_DATA}_$gameId"
            val jsonString = storage.getString(key, "")
            if (jsonString.isNotEmpty()) {
                json.decodeFromString<ReplayData>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun deleteReplayData(gameId: String) = withContext(Dispatchers.Default) {
        try {
            val key = "${StorageKeys.REPLAY_DATA}_$gameId"
            storage.remove(key)
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to delete replay data", e)
        }
    }
    
    override suspend fun getAllReplayIds(): List<String> = withContext(Dispatchers.Default) {
        try {
            val allKeys = storage.getAllKeys()
            val replayKeys = allKeys.filter { it.startsWith("${StorageKeys.REPLAY_DATA}_") }
            replayKeys.map { it.removePrefix("${StorageKeys.REPLAY_DATA}_") }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun exportAllData(): String = withContext(Dispatchers.Default) {
        try {
            val exportData = ExportData(
                gameRecords = getGameRecords(1000),
                playerStatistics = getPlayerStatistics(),
                gameConfig = getGameConfig(),
                leaderboard = getLeaderboard(null, null, 500),
                exportTimestamp = System.currentTimeMillis(),
                version = "1.0"
            )
            
            json.encodeToString(exportData)
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to export data", e)
        }
    }
    
    override suspend fun importAllData(data: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val exportData = json.decodeFromString<ExportData>(data)
            
            // 验证数据版本兼容性
            if (!isVersionCompatible(exportData.version)) {
                return@withContext false
            }
            
            // 清除现有数据
            clearAllData()
            
            // 导入数据
            exportData.gameRecords.forEach { saveGameRecord(it) }
            updatePlayerStatistics(exportData.playerStatistics)
            saveGameConfig(exportData.gameConfig)
            exportData.leaderboard.forEach { saveLeaderboardEntry(it) }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun clearAllData() = withContext(Dispatchers.Default) {
        try {
            storage.clear()
            
            // 清除缓存
            cachedStatistics = null
            cachedConfig = null
            
        } catch (e: Exception) {
            throw DataRepositoryException("Failed to clear all data", e)
        }
    }
    
    override suspend fun getDataSize(): Long = withContext(Dispatchers.Default) {
        try {
            val allKeys = storage.getAllKeys()
            var totalSize = 0L
            
            allKeys.forEach { key ->
                val value = storage.getString(key, "")
                totalSize += value.toByteArray(Charsets.UTF_8).size
            }
            
            totalSize
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 检查版本兼容性
     */
    private fun isVersionCompatible(version: String): Boolean {
        // 简单的版本检查，实际项目中可能需要更复杂的逻辑
        return version == "1.0"
    }
}

/**
 * 数据导出模型
 */
@kotlinx.serialization.Serializable
private data class ExportData(
    val gameRecords: List<GameRecord>,
    val playerStatistics: PlayerStatistics,
    val gameConfig: GameConfig,
    val leaderboard: List<LeaderboardEntry>,
    val exportTimestamp: Long,
    val version: String
)

/**
 * 数据仓库异常类
 */
class DataRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 统计管理器
 * 
 * 负责更新和计算玩家统计数据
 */
class StatisticsManager(
    private val repository: GameDataRepository
) {
    
    /**
     * 记录游戏结束，更新统计数据
     */
    suspend fun recordGameEnd(
        gameRecord: GameRecord,
        gameConfig: GameConfig
    ) {
        try {
            // 保存游戏记录
            repository.saveGameRecord(gameRecord)
            
            // 更新统计数据
            val currentStats = repository.getPlayerStatistics()
            val updatedStats = updateStatistics(currentStats, gameRecord, gameConfig)
            repository.updatePlayerStatistics(updatedStats)
            
        } catch (e: Exception) {
            throw StatisticsException("Failed to record game end", e)
        }
    }
    
    /**
     * 更新统计数据
     */
    private fun updateStatistics(
        current: PlayerStatistics,
        record: GameRecord,
        config: GameConfig
    ): PlayerStatistics {
        val today = getCurrentDateString()
        val thisWeek = getCurrentWeekString()
        val thisMonth = getCurrentMonthString()
        
        // 检查连续游戏天数
        val (consecutiveDays, bestStreak) = updateStreakData(current, record.timestamp)
        
        // 更新每日统计
        val updatedDailyStats = updateDailyStats(current.dailyStats, today, record)
        
        // 更新每周统计
        val updatedWeeklyStats = updateWeeklyStats(current.weeklyStats, thisWeek, record)
        
        // 更新每月统计
        val updatedMonthlyStats = updateMonthlyStats(current.monthlyStats, thisMonth, record)
        
        return current.copy(
            totalGames = current.totalGames + 1,
            totalScore = current.totalScore + record.finalScore,
            highestScore = maxOf(current.highestScore, record.finalScore),
            totalPlayTime = current.totalPlayTime + record.playTime,
            averageScore = (current.totalScore + record.finalScore).toDouble() / (current.totalGames + 1),
            gamesWon = current.gamesWon + if (isWinningGame(record)) 1 else 0,
            longestSnake = maxOf(current.longestSnake, record.maxSnakeLength),
            totalFoodEaten = current.totalFoodEaten + record.foodEaten,
            totalEffectsUsed = current.totalEffectsUsed + record.effectsUsed,
            gamesByMode = current.gamesByMode + (config.gameMode to (current.gamesByMode[config.gameMode] ?: 0) + 1),
            gamesByDifficulty = current.gamesByDifficulty + (config.difficulty to (current.gamesByDifficulty[config.difficulty] ?: 0) + 1),
            dailyStats = updatedDailyStats,
            weeklyStats = updatedWeeklyStats,
            monthlyStats = updatedMonthlyStats,
            lastPlayDate = record.timestamp,
            consecutiveDays = consecutiveDays,
            bestStreak = bestStreak
        )
    }
    
    /**
     * 判断是否为获胜游戏
     */
    private fun isWinningGame(record: GameRecord): Boolean {
        // 定义获胜条件：分数达到50分或蛇长度达到10
        return record.finalScore >= 50 || record.maxSnakeLength >= 10
    }
    
    /**
     * 更新连续游戏天数
     */
    private fun updateStreakData(current: PlayerStatistics, timestamp: Long): Pair<Int, Int> {
        val today = getCurrentDateString()
        val lastPlayDate = if (current.lastPlayDate > 0) {
            kotlinx.datetime.Instant.fromEpochMilliseconds(current.lastPlayDate)
                .toString().substring(0, 10)
        } else {
            ""
        }
        
        val consecutiveDays = when {
            lastPlayDate.isEmpty() -> 1 // 第一次游戏
            lastPlayDate == today -> current.consecutiveDays // 同一天
            isConsecutiveDay(lastPlayDate, today) -> current.consecutiveDays + 1 // 连续天数
            else -> 1 // 重新开始计算
        }
        
        val bestStreak = maxOf(current.bestStreak, consecutiveDays)
        
        return Pair(consecutiveDays, bestStreak)
    }
    
    /**
     * 检查是否为连续的天
     */
    private fun isConsecutiveDay(lastDate: String, currentDate: String): Boolean {
        // 简化实现，实际项目中需要更精确的日期计算
        return try {
            val last = kotlinx.datetime.LocalDate.parse(lastDate)
            val current = kotlinx.datetime.LocalDate.parse(currentDate)
            val daysBetween = current.toEpochDays() - last.toEpochDays()
            daysBetween == 1L
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 更新每日统计
     */
    private fun updateDailyStats(
        currentDailyStats: Map<String, DailyStats>,
        today: String,
        record: GameRecord
    ): Map<String, DailyStats> {
        val todayStats = currentDailyStats[today] ?: DailyStats(today)
        
        val updatedTodayStats = todayStats.copy(
            gamesPlayed = todayStats.gamesPlayed + 1,
            totalScore = todayStats.totalScore + record.finalScore,
            highestScore = maxOf(todayStats.highestScore, record.finalScore),
            totalPlayTime = todayStats.totalPlayTime + record.playTime,
            foodEaten = todayStats.foodEaten + record.foodEaten,
            effectsUsed = todayStats.effectsUsed + record.effectsUsed
        )
        
        return currentDailyStats + (today to updatedTodayStats)
    }
    
    /**
     * 更新每周统计
     */
    private fun updateWeeklyStats(
        currentWeeklyStats: Map<String, WeeklyStats>,
        thisWeek: String,
        record: GameRecord
    ): Map<String, WeeklyStats> {
        val weekStats = currentWeeklyStats[thisWeek] ?: WeeklyStats(thisWeek)
        
        val updatedWeekStats = weekStats.copy(
            gamesPlayed = weekStats.gamesPlayed + 1,
            totalScore = weekStats.totalScore + record.finalScore,
            highestScore = maxOf(weekStats.highestScore, record.finalScore),
            totalPlayTime = weekStats.totalPlayTime + record.playTime,
            averageScore = (weekStats.totalScore + record.finalScore).toDouble() / (weekStats.gamesPlayed + 1)
        )
        
        return currentWeeklyStats + (thisWeek to updatedWeekStats)
    }
    
    /**
     * 更新每月统计
     */
    private fun updateMonthlyStats(
        currentMonthlyStats: Map<String, MonthlyStats>,
        thisMonth: String,
        record: GameRecord
    ): Map<String, MonthlyStats> {
        val monthStats = currentMonthlyStats[thisMonth] ?: MonthlyStats(thisMonth)
        
        val updatedMonthStats = monthStats.copy(
            gamesPlayed = monthStats.gamesPlayed + 1,
            totalScore = monthStats.totalScore + record.finalScore,
            highestScore = maxOf(monthStats.highestScore, record.finalScore),
            totalPlayTime = monthStats.totalPlayTime + record.playTime,
            averageScore = (monthStats.totalScore + record.finalScore).toDouble() / (monthStats.gamesPlayed + 1)
        )
        
        return currentMonthlyStats + (thisMonth to updatedMonthStats)
    }
    
    /**
     * 获取当前日期字符串
     */
    private fun getCurrentDateString(): String {
        return kotlinx.datetime.Clock.System.now().toString().substring(0, 10)
    }
    
    /**
     * 获取当前周字符串
     */
    private fun getCurrentWeekString(): String {
        val now = kotlinx.datetime.Clock.System.now()
        val year = now.toString().substring(0, 4)
        // 简化的周计算，实际项目中需要更精确的实现
        val dayOfYear = now.toEpochDays() % 365
        val week = (dayOfYear / 7) + 1
        return "$year-W${week.toString().padStart(2, '0')}"
    }
    
    /**
     * 获取当前月字符串
     */
    private fun getCurrentMonthString(): String {
        return kotlinx.datetime.Clock.System.now().toString().substring(0, 7)
    }
}

/**
 * 统计异常类
 */
class StatisticsException(message: String, cause: Throwable? = null) : Exception(message, cause)
