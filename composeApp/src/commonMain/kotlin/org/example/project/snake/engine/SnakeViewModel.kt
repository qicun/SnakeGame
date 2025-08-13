package org.example.project.snake.engine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.snake.config.GameConfig
import org.example.project.snake.config.GameMode
import org.example.project.snake.config.Difficulty
import org.example.project.snake.model.*
import org.example.project.snake.data.GameDataRepository
import org.example.project.snake.data.GameRecord
import org.example.project.snake.data.PlayerStatistics
import org.example.project.snake.data.StatisticsManager
import org.example.project.snake.data.ReplayRecorder

/**
 * 贪吃蛇游戏的ViewModel
 * 
 * 负责管理游戏状态、处理用户输入、控制游戏循环
 * 支持多种游戏模式、难度级别和特效系统
 */
class SnakeViewModel : ViewModel() {
    
    // 当前游戏配置
    private val _gameConfig = MutableStateFlow(GameConfig())
    val gameConfig: StateFlow<GameConfig> = _gameConfig.asStateFlow()
    
    // 游戏引擎实例
    private var gameEngine = SnakeGameEngine(_gameConfig.value)
    
    // 数据管理组件
    private val statisticsManager = StatisticsManager(dataRepository)
    private var replayRecorder: ReplayRecorder? = null
    
    // 游戏循环协程Job，用于控制游戏循环的启动和停止
    private var gameLoopJob: Job? = null
    
    // 待处理的方向变更（用于缓冲用户输入）
    private var pendingDirection: Direction? = null
    
    // 游戏开始时间（用于计算游戏时长）
    private var gameStartTime: Long = 0L
    
    // 玩家统计数据
    private val _playerStatistics = MutableStateFlow<PlayerStatistics?>(null)
    val playerStatistics: StateFlow<PlayerStatistics?> = _playerStatistics.asStateFlow()
    
    // 私有的可变状态流
    private val _gameData = MutableStateFlow(gameEngine.initializeGame())
    
    // 公开的只读状态流，供UI观察
    val gameData: StateFlow<SnakeGameEngine.GameData> = _gameData.asStateFlow()
    
    // 当前游戏数据的便捷访问属性
    val currentGameData: SnakeGameEngine.GameData
        get() = _gameData.value
    
    init {
        // ViewModel初始化时自动开始游戏
        startGame()
    }
    
    /**
     * 更新游戏配置
     * 
     * @param newConfig 新的游戏配置
     */
    fun updateGameConfig(newConfig: GameConfig) {
        if (_gameConfig.value != newConfig) {
            _gameConfig.value = newConfig
            gameEngine = SnakeGameEngine(newConfig)
            resetGame()
            
            // 异步保存配置
            viewModelScope.launch {
                saveGameConfig()
            }
        }
    }
    
    /**
     * 设置游戏模式
     * 
     * @param gameMode 新的游戏模式
     */
    fun setGameMode(gameMode: GameMode) {
        val newConfig = _gameConfig.value.copy(gameMode = gameMode)
        updateGameConfig(newConfig)
    }
    
    /**
     * 设置游戏难度
     * 
     * @param difficulty 新的游戏难度
     */
    fun setDifficulty(difficulty: Difficulty) {
        val newConfig = _gameConfig.value.copy(difficulty = difficulty)
        updateGameConfig(newConfig)
    }
    
    /**
     * 切换特效开关
     * 
     * @param enabled 是否启用特效
     */
    fun toggleEffects(enabled: Boolean) {
        val newConfig = _gameConfig.value.copy(enableEffects = enabled)
        updateGameConfig(newConfig)
    }
    
    /**
     * 开始游戏
     * 
     * 启动游戏循环协程，开始定期更新游戏状态
     */
    fun startGame() {
        // 如果游戏循环已经在运行，先停止它
        stopGameLoop()
        
        // 记录游戏开始时间
        gameStartTime = System.currentTimeMillis()
        
        // 初始化回放录制器
        replayRecorder = ReplayRecorder(_gameConfig.value, "game_${gameStartTime}")
        
        // 启动新的游戏循环
        gameLoopJob = viewModelScope.launch {
            while (true) {
                val currentData = _gameData.value
                
                // 只有在游戏进行中才继续循环
                if (currentData.gameState is GameState.Playing) {
                    // 获取当前游戏速度（可能被特效影响）
                    val baseSpeed = currentData.gameState.speed
                    val adjustedSpeed = _gameConfig.value.difficulty.calculateSpeed(baseSpeed)
                    
                    // 处理待处理的方向变更
                    val directionToUse = pendingDirection
                    pendingDirection = null // 清除待处理的方向
                    
                    // 更新游戏状态
                    val newData = gameEngine.updateGame(currentData, directionToUse)
                    _gameData.value = newData
                    
                    // 检查游戏是否结束
                    if (newData.gameState is GameState.GameOver) {
                        // 记录游戏结束
                        recordGameEnd()
                    }
                    
                    // 根据调整后的游戏速度延迟
                    delay(adjustedSpeed)
                } else {
                    // 如果游戏不在进行中，延迟较长时间再检查
                    delay(100)
                }
            }
        }
    }
    
    /**
     * 停止游戏循环
     */
    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }
    
    /**
     * 暂停游戏
     */
    fun pauseGame() {
        val currentData = _gameData.value
        if (currentData.gameState is GameState.Playing) {
            _gameData.value = gameEngine.pauseGame(currentData)
        }
    }
    
    /**
     * 恢复游戏
     */
    fun resumeGame() {
        val currentData = _gameData.value
        if (currentData.gameState is GameState.Paused) {
            _gameData.value = gameEngine.resumeGame(currentData)
        }
    }
    
    /**
     * 重置游戏
     * 
     * 重新初始化游戏数据并重启游戏循环
     */
    fun resetGame() {
        // 停止当前游戏循环
        stopGameLoop()
        
        // 重置游戏数据
        _gameData.value = gameEngine.resetGame()
        
        // 清除待处理的方向
        pendingDirection = null
        
        // 重新开始游戏
        startGame()
    }
    
    /**
     * 改变蛇的移动方向
     * 
     * @param newDirection 新的移动方向
     */
    fun changeDirection(newDirection: Direction) {
        val currentData = _gameData.value
        
        // 只有在游戏进行中才处理方向变更
        if (currentData.gameState is GameState.Playing) {
            val currentDirection = currentData.snake.direction
            
            // 验证方向变更是否有效
            if (gameEngine.isValidDirectionChange(currentDirection, newDirection)) {
                // 缓冲方向变更，避免在一个游戏循环内多次变更方向
                pendingDirection = newDirection
            }
        }
    }
    
    /**
     * 切换游戏暂停状态
     */
    fun togglePause() {
        val currentData = _gameData.value
        when (currentData.gameState) {
            is GameState.Playing -> pauseGame()
            is GameState.Paused -> resumeGame()
            is GameState.GameOver -> {
                // 游戏结束时，切换暂停相当于重置游戏
                resetGame()
            }
        }
    }
    
    /**
     * 获取当前分数
     * 
     * @return 当前分数
     */
    fun getCurrentScore(): Int {
        return _gameData.value.gameState.getCurrentScore()
    }
    
    /**
     * 获取当前等级
     * 
     * @return 当前等级
     */
    fun getCurrentLevel(): Int {
        return when (val state = _gameData.value.gameState) {
            is GameState.Playing -> state.level
            is GameState.Paused -> state.previousState.level
            is GameState.GameOver -> state.finalLevel
        }
    }
    
    /**
     * 获取当前活跃的特效列表
     * 
     * @return 活跃特效列表
     */
    fun getActiveEffects(): List<FoodEffect> {
        return _gameData.value.activeEffects
    }
    
    /**
     * 获取剩余时间（时间挑战模式）
     * 
     * @return 剩余时间（秒），如果不是时间挑战模式返回null
     */
    fun getRemainingTime(): Int? {
        if (_gameConfig.value.gameMode != GameMode.TIME_CHALLENGE) {
            return null
        }
        
        val currentData = _gameData.value
        val elapsedTime = (System.currentTimeMillis() - currentData.gameStartTime) / 1000
        val remainingTime = _gameConfig.value.timeLimitSeconds - elapsedTime.toInt()
        return maxOf(0, remainingTime)
    }
    
    /**
     * 检查游戏是否正在进行
     * 
     * @return 如果游戏正在进行返回true，否则返回false
     */
    fun isGamePlaying(): Boolean {
        return _gameData.value.gameState is GameState.Playing
    }
    
    /**
     * 检查游戏是否暂停
     * 
     * @return 如果游戏暂停返回true，否则返回false
     */
    fun isGamePaused(): Boolean {
        return _gameData.value.gameState is GameState.Paused
    }
    
    /**
     * 检查游戏是否结束
     * 
     * @return 如果游戏结束返回true，否则返回false
     */
    fun isGameOver(): Boolean {
        return _gameData.value.gameState is GameState.GameOver
    }
    
    /**
     * 获取游戏结束原因
     * 
     * @return 游戏结束原因，如果游戏未结束返回null
     */
    fun getGameOverReason(): GameState.GameOverReason? {
        val gameState = _gameData.value.gameState
        return if (gameState is GameState.GameOver) {
            gameState.reason
        } else {
            null
        }
    }
    
    /**
     * 获取当前游戏模式的显示名称
     * 
     * @return 游戏模式显示名称
     */
    fun getCurrentGameModeDisplayName(): String {
        return _gameConfig.value.gameMode.displayName
    }
    
    /**
     * 获取当前难度的显示名称
     * 
     * @return 难度显示名称
     */
    fun getCurrentDifficultyDisplayName(): String {
        return _gameConfig.value.difficulty.displayName
    }
    
    /**
     * 加载游戏配置
     */
    private suspend fun loadGameConfig() {
        try {
            val savedConfig = dataRepository.getGameConfig()
            _gameConfig.value = savedConfig
            gameEngine = SnakeGameEngine(savedConfig)
        } catch (e: Exception) {
            // 使用默认配置
            println("Failed to load game config: ${e.message}")
        }
    }
    
    /**
     * 保存游戏配置
     */
    private suspend fun saveGameConfig() {
        try {
            dataRepository.saveGameConfig(_gameConfig.value)
        } catch (e: Exception) {
            println("Failed to save game config: ${e.message}")
        }
    }
    
    /**
     * 加载玩家统计数据
     */
    private suspend fun loadPlayerStatistics() {
        try {
            val statistics = dataRepository.getPlayerStatistics()
            _playerStatistics.value = statistics
        } catch (e: Exception) {
            println("Failed to load player statistics: ${e.message}")
        }
    }
    
    /**
     * 记录游戏结束
     */
    private suspend fun recordGameEnd() {
        try {
            val currentData = _gameData.value
            val gameState = currentData.gameState
            
            if (gameState is GameState.GameOver) {
                // 创建游戏记录
                val gameRecord = GameRecord(
                    gameMode = _gameConfig.value.gameMode,
                    difficulty = _gameConfig.value.difficulty,
                    finalScore = gameState.finalScore,
                    maxSnakeLength = currentData.snake.body.size,
                    playTime = System.currentTimeMillis() - gameStartTime,
                    foodEaten = gameState.finalScore / 10, // 简化计算
                    effectsUsed = currentData.activeEffects.size,
                    gameOverReason = gameState.reason
                )
                
                // 使用统计管理器记录游戏结束
                statisticsManager.recordGameEnd(gameRecord, _gameConfig.value)
                
                // 完成回放录制
                replayRecorder?.finishRecording(
                    playerName = "Player",
                    finalScore = gameState.finalScore,
                    finalSnakeLength = currentData.snake.body.size
                )?.let { replayData ->
                    dataRepository.saveReplayData(gameRecord.id, replayData)
                }
                
                // 重新加载统计数据
                loadPlayerStatistics()
            }
        } catch (e: Exception) {
            println("Failed to record game end: ${e.message}")
        }
    }
    
    /**
     * 获取游戏记录列表
     */
    suspend fun getGameRecords(limit: Int = 10): List<GameRecord> {
        return try {
            dataRepository.getGameRecords(limit)
        } catch (e: Exception) {
            println("Failed to get game records: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 获取排行榜
     */
    suspend fun getLeaderboard(limit: Int = 10) = try {
        dataRepository.getLeaderboard(_gameConfig.value.gameMode, _gameConfig.value.difficulty, limit)
    } catch (e: Exception) {
        println("Failed to get leaderboard: ${e.message}")
        emptyList()
    }
    
    /**
     * 清除所有数据
     */
    suspend fun clearAllData() {
        try {
            dataRepository.clearAllData()
            _playerStatistics.value = null
            loadPlayerStatistics()
        } catch (e: Exception) {
            println("Failed to clear all data: ${e.message}")
        }
    }
    
    /**
     * ViewModel清理时停止游戏循环
     */
    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
        
        // 保存当前配置
        viewModelScope.launch {
            saveGameConfig()
        }
    }
}