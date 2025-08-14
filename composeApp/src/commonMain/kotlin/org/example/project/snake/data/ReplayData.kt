package org.example.project.snake.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import org.example.project.snake.config.GameConfig
import org.example.project.snake.model.Direction
import org.example.project.snake.model.Position
import org.example.project.snake.model.FoodType

/**
 * 游戏回放数据
 * 
 * 记录完整的游戏过程，用于回放功能
 */
@Serializable
data class ReplayData(
    val gameId: String,
    @Contextual
    val gameConfig: GameConfig,
    val initialState: ReplayGameState,
    val actions: List<ReplayAction>,
    val events: List<ReplayEvent>,
    val metadata: ReplayMetadata,
    val version: String = "1.0"
) {
    
    /**
     * 获取回放总时长
     */
    val totalDuration: Long
        get() = actions.lastOrNull()?.timestamp ?: 0L
    
    /**
     * 获取回放帧数
     */
    val frameCount: Int
        get() = actions.size
    
    /**
     * 获取平均FPS
     */
    val averageFps: Double
        get() = if (totalDuration > 0) frameCount * 1000.0 / totalDuration else 0.0
    
    /**
     * 检查回放数据是否有效
     */
    val isValid: Boolean
        get() = gameId.isNotEmpty() && actions.isNotEmpty() && 
                actions.all { it.timestamp >= 0 } &&
                actions.zipWithNext().all { (a, b) -> a.timestamp <= b.timestamp }
    
    /**
     * 获取指定时间点的游戏状态
     */
    fun getStateAtTime(timestamp: Long): ReplayGameState? {
        if (timestamp < 0) return initialState
        
        val relevantActions = actions.filter { it.timestamp <= timestamp }
        if (relevantActions.isEmpty()) return initialState
        
        // 重建游戏状态
        return reconstructGameState(relevantActions)
    }
    
    /**
     * 获取时间范围内的动作
     */
    fun getActionsInRange(startTime: Long, endTime: Long): List<ReplayAction> {
        return actions.filter { it.timestamp in startTime..endTime }
    }
    
    /**
     * 获取关键时刻（如吃食物、死亡等）
     */
    fun getKeyMoments(): List<KeyMoment> {
        val keyMoments = mutableListOf<KeyMoment>()
        
        events.forEach { event ->
            when (event.type) {
                ReplayEventType.FOOD_EATEN -> {
                    keyMoments.add(KeyMoment(
                        timestamp = event.timestamp,
                        type = KeyMomentType.FOOD_EATEN,
                        description = "吃到${event.data["foodType"]}食物",
                        score = event.data["score"]?.toIntOrNull() ?: 0
                    ))
                }
                ReplayEventType.GAME_OVER -> {
                    keyMoments.add(KeyMoment(
                        timestamp = event.timestamp,
                        type = KeyMomentType.GAME_OVER,
                        description = "游戏结束: ${event.data["reason"]}",
                        score = event.data["finalScore"]?.toIntOrNull() ?: 0
                    ))
                }
                ReplayEventType.LEVEL_UP -> {
                    keyMoments.add(KeyMoment(
                        timestamp = event.timestamp,
                        type = KeyMomentType.LEVEL_UP,
                        description = "升级到第${event.data["level"]}级",
                        score = event.data["score"]?.toIntOrNull() ?: 0
                    ))
                }
                ReplayEventType.EFFECT_ACTIVATED -> {
                    keyMoments.add(KeyMoment(
                        timestamp = event.timestamp,
                        type = KeyMomentType.EFFECT_USED,
                        description = "使用${event.data["effectType"]}效果",
                        score = event.data["score"]?.toIntOrNull() ?: 0
                    ))
                }
                else -> { /* 其他事件暂不处理 */ }
            }
        }
        
        return keyMoments.sortedBy { it.timestamp }
    }
    
    /**
     * 重建指定时间点的游戏状态
     */
    private fun reconstructGameState(actions: List<ReplayAction>): ReplayGameState {
        var currentState = initialState
        
        actions.forEach { action ->
            currentState = when (action.type) {
                ReplayActionType.MOVE -> {
                    val direction = Direction.valueOf(action.data["direction"] ?: "UP")
                    val newHead = Position(
                        action.data["headX"]?.toIntOrNull() ?: currentState.snakeHead.x,
                        action.data["headY"]?.toIntOrNull() ?: currentState.snakeHead.y
                    )
                    currentState.copy(
                        snakeHead = newHead,
                        snakeBody = action.data["snakeBody"]?.split(",")?.chunked(2)?.map { 
                            Position(it[0].toInt(), it[1].toInt()) 
                        } ?: currentState.snakeBody,
                        direction = direction
                    )
                }
                ReplayActionType.EAT_FOOD -> {
                    currentState.copy(
                        score = action.data["score"]?.toIntOrNull() ?: currentState.score,
                        snakeLength = action.data["snakeLength"]?.toIntOrNull() ?: currentState.snakeLength,
                        foodPosition = null // 食物被吃掉
                    )
                }
                ReplayActionType.SPAWN_FOOD -> {
                    val foodPos = Position(
                        action.data["foodX"]?.toIntOrNull() ?: 0,
                        action.data["foodY"]?.toIntOrNull() ?: 0
                    )
                    val foodType = action.data["foodType"]?.let { FoodType.valueOf(it) } ?: FoodType.REGULAR
                    currentState.copy(
                        foodPosition = foodPos,
                        foodType = foodType
                    )
                }
                ReplayActionType.GAME_OVER -> {
                    currentState.copy(isGameOver = true)
                }
                else -> currentState
            }
        }
        
        return currentState
    }
}

/**
 * 回放游戏状态
 */
@Serializable
data class ReplayGameState(
    @Contextual
    val snakeHead: Position,
    @Contextual
    val snakeBody: List<Position>,
    val direction: Direction,
    @Contextual
    val foodPosition: Position?,
    val foodType: FoodType?,
    val score: Int,
    val snakeLength: Int,
    val level: Int = 1,
    val isGameOver: Boolean = false,
    val activeEffects: List<String> = emptyList()
)

/**
 * 回放动作
 */
@Serializable
data class ReplayAction(
    val timestamp: Long,
    val type: ReplayActionType,
    val data: Map<String, String> = emptyMap()
)

/**
 * 回放动作类型
 */
@Serializable
enum class ReplayActionType {
    MOVE,           // 移动
    EAT_FOOD,       // 吃食物
    SPAWN_FOOD,     // 生成食物
    DIRECTION_CHANGE, // 改变方向
    EFFECT_ACTIVATE,  // 激活效果
    EFFECT_DEACTIVATE, // 取消效果
    LEVEL_UP,       // 升级
    GAME_OVER,      // 游戏结束
    PAUSE,          // 暂停
    RESUME          // 恢复
}

/**
 * 回放事件
 */
@Serializable
data class ReplayEvent(
    val timestamp: Long,
    val type: ReplayEventType,
    val data: Map<String, String> = emptyMap()
)

/**
 * 回放事件类型
 */
@Serializable
enum class ReplayEventType {
    GAME_START,     // 游戏开始
    GAME_OVER,      // 游戏结束
    FOOD_EATEN,     // 食物被吃
    COLLISION,      // 碰撞
    LEVEL_UP,       // 升级
    EFFECT_ACTIVATED, // 效果激活
    ACHIEVEMENT_UNLOCKED, // 成就解锁
    MILESTONE_REACHED // 里程碑达成
}

/**
 * 回放元数据
 */
@Serializable
data class ReplayMetadata(
    val playerName: String,
    val finalScore: Int,
    val finalSnakeLength: Int,
    val playTime: Long,
    val createdAt: Long,
    val gameMode: String,
    val difficulty: String,
    val isPersonalBest: Boolean = false,
    val tags: List<String> = emptyList(),
    val description: String = ""
) {
    
    /**
     * 获取显示标题
     */
    val displayTitle: String
        get() = if (description.isNotEmpty()) {
            description
        } else {
            "$gameMode - $difficulty - ${finalScore}分"
        }
    
    /**
     * 获取格式化的游戏时长
     */
    val formattedPlayTime: String
        get() {
            val seconds = playTime / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return if (minutes > 0) {
                "${minutes}分${remainingSeconds}秒"
            } else {
                "${remainingSeconds}秒"
            }
        }
}

/**
 * 关键时刻
 */
@Serializable
data class KeyMoment(
    val timestamp: Long,
    val type: KeyMomentType,
    val description: String,
    val score: Int
)

/**
 * 关键时刻类型
 */
@Serializable
enum class KeyMomentType {
    GAME_START,     // 游戏开始
    FOOD_EATEN,     // 吃到食物
    LEVEL_UP,       // 升级
    EFFECT_USED,    // 使用效果
    MILESTONE,      // 里程碑
    GAME_OVER       // 游戏结束
}

/**
 * 回放录制器
 * 
 * 负责录制游戏过程
 */
class ReplayRecorder(
    private val gameConfig: GameConfig,
    private val gameId: String
) {
    private val actions = mutableListOf<ReplayAction>()
    private val events = mutableListOf<ReplayEvent>()
    private var initialState: ReplayGameState? = null
    private var startTime: Long = 0L
    
    /**
     * 开始录制
     */
    fun startRecording(gameState: ReplayGameState) {
        startTime = System.currentTimeMillis()
        initialState = gameState
        actions.clear()
        events.clear()
        
        // 记录游戏开始事件
        recordEvent(ReplayEventType.GAME_START, mapOf(
            "gameMode" to gameConfig.gameMode.name,
            "difficulty" to gameConfig.difficulty.name
        ))
    }
    
    /**
     * 记录动作
     */
    fun recordAction(type: ReplayActionType, data: Map<String, String> = emptyMap()) {
        val timestamp = System.currentTimeMillis() - startTime
        actions.add(ReplayAction(timestamp, type, data))
    }
    
    /**
     * 记录事件
     */
    fun recordEvent(type: ReplayEventType, data: Map<String, String> = emptyMap()) {
        val timestamp = System.currentTimeMillis() - startTime
        events.add(ReplayEvent(timestamp, type, data))
    }
    
    /**
     * 记录移动
     */
    fun recordMove(head: Position, body: List<Position>, direction: Direction) {
        recordAction(ReplayActionType.MOVE, mapOf(
            "headX" to head.x.toString(),
            "headY" to head.y.toString(),
            "direction" to direction.name,
            "snakeBody" to body.joinToString(",") { "${it.x},${it.y}" }
        ))
    }
    
    /**
     * 记录吃食物
     */
    fun recordEatFood(score: Int, snakeLength: Int, foodType: FoodType) {
        recordAction(ReplayActionType.EAT_FOOD, mapOf(
            "score" to score.toString(),
            "snakeLength" to snakeLength.toString(),
            "foodType" to foodType.name
        ))
        
        recordEvent(ReplayEventType.FOOD_EATEN, mapOf(
            "score" to score.toString(),
            "foodType" to foodType.name
        ))
    }
    
    /**
     * 记录生成食物
     */
    fun recordSpawnFood(position: Position, foodType: FoodType) {
        recordAction(ReplayActionType.SPAWN_FOOD, mapOf(
            "foodX" to position.x.toString(),
            "foodY" to position.y.toString(),
            "foodType" to foodType.name
        ))
    }
    
    /**
     * 记录游戏结束
     */
    fun recordGameOver(finalScore: Int, reason: String) {
        recordAction(ReplayActionType.GAME_OVER, mapOf(
            "finalScore" to finalScore.toString(),
            "reason" to reason
        ))
        
        recordEvent(ReplayEventType.GAME_OVER, mapOf(
            "finalScore" to finalScore.toString(),
            "reason" to reason
        ))
    }
    
    /**
     * 完成录制并生成回放数据
     */
    fun finishRecording(
        playerName: String,
        finalScore: Int,
        finalSnakeLength: Int,
        isPersonalBest: Boolean = false,
        tags: List<String> = emptyList(),
        description: String = ""
    ): ReplayData? {
        val initial = initialState ?: return null
        
        val metadata = ReplayMetadata(
            playerName = playerName,
            finalScore = finalScore,
            finalSnakeLength = finalSnakeLength,
            playTime = System.currentTimeMillis() - startTime,
            createdAt = System.currentTimeMillis(),
            gameMode = gameConfig.gameMode.displayName,
            difficulty = gameConfig.difficulty.displayName,
            isPersonalBest = isPersonalBest,
            tags = tags,
            description = description
        )
        
        return ReplayData(
            gameId = gameId,
            gameConfig = gameConfig,
            initialState = initial,
            actions = actions.toList(),
            events = events.toList(),
            metadata = metadata
        )
    }
}

/**
 * 回放播放器
 * 
 * 负责播放回放数据
 */
class ReplayPlayer(private val replayData: ReplayData) {
    
    private var currentTime: Long = 0L
    private var isPlaying: Boolean = false
    private var playbackSpeed: Float = 1.0f
    
    /**
     * 开始播放
     */
    fun play() {
        isPlaying = true
    }
    
    /**
     * 暂停播放
     */
    fun pause() {
        isPlaying = false
    }
    
    /**
     * 停止播放
     */
    fun stop() {
        isPlaying = false
        currentTime = 0L
    }
    
    /**
     * 跳转到指定时间
     */
    fun seekTo(timestamp: Long) {
        currentTime = timestamp.coerceIn(0L, replayData.totalDuration)
    }
    
    /**
     * 设置播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed.coerceIn(0.1f, 5.0f)
    }
    
    /**
     * 获取当前播放状态
     */
    fun getPlaybackState(): ReplayPlaybackState {
        return ReplayPlaybackState(
            currentTime = currentTime,
            totalDuration = replayData.totalDuration,
            isPlaying = isPlaying,
            playbackSpeed = playbackSpeed,
            progress = if (replayData.totalDuration > 0) {
                currentTime.toFloat() / replayData.totalDuration
            } else 0f
        )
    }
    
    /**
     * 获取当前游戏状态
     */
    fun getCurrentGameState(): ReplayGameState? {
        return replayData.getStateAtTime(currentTime)
    }
    
    /**
     * 更新播放时间
     */
    fun updateTime(deltaTime: Long) {
        if (isPlaying) {
            currentTime += (deltaTime * playbackSpeed).toLong()
            if (currentTime >= replayData.totalDuration) {
                currentTime = replayData.totalDuration
                isPlaying = false
            }
        }
    }
    
    /**
     * 获取下一个关键时刻
     */
    fun getNextKeyMoment(): KeyMoment? {
        val keyMoments = replayData.getKeyMoments()
        return keyMoments.find { it.timestamp > currentTime }
    }
    
    /**
     * 获取上一个关键时刻
     */
    fun getPreviousKeyMoment(): KeyMoment? {
        val keyMoments = replayData.getKeyMoments()
        return keyMoments.findLast { it.timestamp < currentTime }
    }
    
    /**
     * 跳转到下一个关键时刻
     */
    fun skipToNextKeyMoment() {
        getNextKeyMoment()?.let { keyMoment ->
            seekTo(keyMoment.timestamp)
        }
    }
    
    /**
     * 跳转到上一个关键时刻
     */
    fun skipToPreviousKeyMoment() {
        getPreviousKeyMoment()?.let { keyMoment ->
            seekTo(keyMoment.timestamp)
        }
    }
}

/**
 * 回放播放状态
 */
@Serializable
data class ReplayPlaybackState(
    val currentTime: Long,
    val totalDuration: Long,
    val isPlaying: Boolean,
    val playbackSpeed: Float,
    val progress: Float
) {
    
    /**
     * 获取格式化的当前时间
     */
    val formattedCurrentTime: String
        get() = formatTime(currentTime)
    
    /**
     * 获取格式化的总时长
     */
    val formattedTotalDuration: String
        get() = formatTime(totalDuration)
    
    /**
     * 获取剩余时间
     */
    val remainingTime: Long
        get() = totalDuration - currentTime
    
    /**
     * 获取格式化的剩余时间
     */
    val formattedRemainingTime: String
        get() = formatTime(remainingTime)
    
    private fun formatTime(timeMs: Long): String {
        val seconds = timeMs / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "${minutes}:${remainingSeconds.toString().padStart(2, '0')}"
        } else {
            "${remainingSeconds}s"
        }
    }
}

/**
 * 回放管理器
 * 
 * 负责回放数据的管理和操作
 */
class ReplayManager {
    
    /**
     * 验证回放数据
     */
    fun validateReplayData(replayData: ReplayData): ReplayValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // 基本验证
        if (replayData.gameId.isEmpty()) {
            errors.add("游戏ID不能为空")
        }
        
        if (replayData.actions.isEmpty()) {
            errors.add("回放动作不能为空")
        }
        
        // 时间戳验证
        val timestamps = replayData.actions.map { it.timestamp }
        if (timestamps.any { it < 0 }) {
            errors.add("时间戳不能为负数")
        }
        
        if (timestamps.zipWithNext().any { (a, b) -> a > b }) {
            errors.add("时间戳必须按升序排列")
        }
        
        // 数据完整性验证
        val moveActions = replayData.actions.filter { it.type == ReplayActionType.MOVE }
        if (moveActions.isEmpty()) {
            warnings.add("没有移动动作记录")
        }
        
        // 游戏状态验证
        val initialState = replayData.initialState
        if (initialState.snakeBody.isEmpty()) {
            errors.add("初始蛇身不能为空")
        }
        
        // 版本兼容性验证
        if (!isVersionSupported(replayData.version)) {
            warnings.add("回放版本可能不兼容: ${replayData.version}")
        }
        
        return ReplayValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * 压缩回放数据
     */
    fun compressReplayData(replayData: ReplayData): ReplayData {
        // 移除冗余的移动动作
        val compressedActions = mutableListOf<ReplayAction>()
        var lastDirection: String? = null
        
        replayData.actions.forEach { action ->
            when (action.type) {
                ReplayActionType.MOVE -> {
                    val currentDirection = action.data["direction"]
                    if (currentDirection != lastDirection) {
                        compressedActions.add(action)
                        lastDirection = currentDirection
                    }
                }
                else -> {
                    compressedActions.add(action)
                }
            }
        }
        
        return replayData.copy(actions = compressedActions)
    }
    
    /**
     * 生成回放摘要
     */
    fun generateReplaySummary(replayData: ReplayData): ReplaySummary {
        val actions = replayData.actions
        val events = replayData.events
        
        val moveCount = actions.count { it.type == ReplayActionType.MOVE }
        val foodEatenCount = actions.count { it.type == ReplayActionType.EAT_FOOD }
        val directionChanges = actions.count { it.type == ReplayActionType.DIRECTION_CHANGE }
        
        val keyMoments = replayData.getKeyMoments()
        val achievements = events.filter { it.type == ReplayEventType.ACHIEVEMENT_UNLOCKED }
        
        return ReplaySummary(
            gameId = replayData.gameId,
            playerName = replayData.metadata.playerName,
            finalScore = replayData.metadata.finalScore,
            playTime = replayData.metadata.playTime,
            moveCount = moveCount,
            foodEatenCount = foodEatenCount,
            directionChanges = directionChanges,
            keyMomentsCount = keyMoments.size,
            achievementsUnlocked = achievements.size,
            averageFps = replayData.averageFps,
            dataSize = estimateDataSize(replayData),
            createdAt = replayData.metadata.createdAt
        )
    }
    
    /**
     * 合并多个回放数据
     */
    fun mergeReplays(replays: List<ReplayData>): ReplayData? {
        if (replays.isEmpty()) return null
        if (replays.size == 1) return replays.first()
        
        // 简单的合并实现，实际项目中可能需要更复杂的逻辑
        val firstReplay = replays.first()
        val allActions = mutableListOf<ReplayAction>()
        val allEvents = mutableListOf<ReplayEvent>()
        
        var timeOffset = 0L
        replays.forEach { replay ->
            // 调整时间戳
            val adjustedActions = replay.actions.map { action ->
                action.copy(timestamp = action.timestamp + timeOffset)
            }
            val adjustedEvents = replay.events.map { event ->
                event.copy(timestamp = event.timestamp + timeOffset)
            }
            
            allActions.addAll(adjustedActions)
            allEvents.addAll(adjustedEvents)
            
            timeOffset += replay.totalDuration
        }
        
        return firstReplay.copy(
            gameId = "merged_${System.currentTimeMillis()}",
            actions = allActions,
            events = allEvents,
            metadata = firstReplay.metadata.copy(
                description = "合并回放 (${replays.size}个游戏)",
                playTime = timeOffset
            )
        )
    }
    
    /**
     * 检查版本支持
     */
    private fun isVersionSupported(version: String): Boolean {
        return version == "1.0"
    }
    
    /**
     * 估算数据大小
     */
    private fun estimateDataSize(replayData: ReplayData): Long {
        // 简单的大小估算
        val actionsSize = replayData.actions.size * 100L // 每个动作约100字节
        val eventsSize = replayData.events.size * 80L    // 每个事件约80字节
        val metadataSize = 500L                          // 元数据约500字节
        
        return actionsSize + eventsSize + metadataSize
    }
}

/**
 * 回放验证结果
 */
data class ReplayValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)

/**
 * 回放摘要
 */
@Serializable
data class ReplaySummary(
    val gameId: String,
    val playerName: String,
    val finalScore: Int,
    val playTime: Long,
    val moveCount: Int,
    val foodEatenCount: Int,
    val directionChanges: Int,
    val keyMomentsCount: Int,
    val achievementsUnlocked: Int,
    val averageFps: Double,
    val dataSize: Long,
    val createdAt: Long
) {
    
    /**
     * 获取效率指标
     */
    val efficiency: Double
        get() = if (playTime > 0) finalScore.toDouble() / (playTime / 1000.0) else 0.0
    
    /**
     * 获取动作密度（每秒动作数）
     */
    val actionDensity: Double
        get() = if (playTime > 0) moveCount.toDouble() / (playTime / 1000.0) else 0.0
    
    /**
     * 获取格式化的数据大小
     */
    val formattedDataSize: String
        get() {
            return when {
                dataSize < 1024 -> "${dataSize}B"
                dataSize < 1024 * 1024 -> "${dataSize / 1024}KB"
                else -> "${dataSize / (1024 * 1024)}MB"
            }
        }
}
