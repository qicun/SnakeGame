package org.example.project.snake.engine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.snake.model.*

/**
 * 贪吃蛇游戏的ViewModel
 * 
 * 负责管理游戏状态、处理用户输入、控制游戏循环
 */
class SnakeViewModel : ViewModel() {
    
    // 游戏引擎实例
    private val gameEngine = SnakeGameEngine()
    
    // 游戏循环协程Job，用于控制游戏循环的启动和停止
    private var gameLoopJob: Job? = null
    
    // 待处理的方向变更（用于缓冲用户输入）
    private var pendingDirection: Direction? = null
    
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
     * 开始游戏
     * 
     * 启动游戏循环协程，开始定期更新游戏状态
     */
    fun startGame() {
        // 如果游戏循环已经在运行，先停止它
        stopGameLoop()
        
        // 启动新的游戏循环
        gameLoopJob = viewModelScope.launch {
            while (true) {
                val currentData = _gameData.value
                
                // 只有在游戏进行中才继续循环
                if (currentData.gameState is GameState.Playing) {
                    // 获取当前游戏速度
                    val speed = currentData.gameState.speed
                    
                    // 处理待处理的方向变更
                    val directionToUse = pendingDirection
                    pendingDirection = null // 清除待处理的方向
                    
                    // 更新游戏状态
                    val newData = gameEngine.updateGame(currentData, directionToUse)
                    _gameData.value = newData
                    
                    // 根据游戏速度延迟
                    delay(speed)
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
     * ViewModel清理时停止游戏循环
     */
    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
