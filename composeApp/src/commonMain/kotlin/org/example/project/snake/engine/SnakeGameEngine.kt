package org.example.project.snake.engine

import org.example.project.snake.config.GameConfig
import org.example.project.snake.model.*

/**
 * 贪吃蛇游戏的核心引擎
 * 
 * 负责处理游戏逻辑、碰撞检测、分数计算等核心功能
 * 
 * @param gameConfig 游戏配置
 */
class SnakeGameEngine(
    private val gameConfig: GameConfig = GameConfig()
) {
    
    // 游戏区域尺寸
    val gameWidth: Int = gameConfig.gameWidth
    val gameHeight: Int = gameConfig.gameHeight
    
    // 游戏模式策略
    private val modeStrategy = GameModeHandler.createStrategy(gameConfig.gameMode)
    
    // 效果管理器
    private val effectManager = EffectManager()
    
    // 障碍物列表
    private var obstacles: List<Position> = emptyList()
    
    /**
     * 扩展的游戏状态数据类
     * 
     * @param snake 蛇对象
     * @param food 食物对象
     * @param gameState 游戏状态
     * @param obstacles 障碍物位置列表
     * @param activeEffects 当前活跃的效果列表
     * @param gameStartTime 游戏开始时间（用于时间挑战模式）
     */
    data class GameData(
        val snake: Snake,
        val food: Food,
        val gameState: GameState,
        val obstacles: List<Position> = emptyList(),
        val activeEffects: List<FoodEffect> = emptyList(),
        val gameStartTime: Long = System.currentTimeMillis()
    )
    
    /**
     * 初始化游戏数据
     * 
     * @return 初始的游戏数据
     */
    fun initializeGame(): GameData {
        // 清理效果管理器
        effectManager.clearAllEffects()
        
        // 创建初始蛇（在游戏区域中央）
        val initialSnake = Snake.createInitial(
            startPosition = Position(gameWidth / 2, gameHeight / 2),
            initialDirection = Direction.RIGHT,
            initialLength = gameConfig.getInitialSnakeLength()
        )
        
        // 生成障碍物（如果游戏模式支持）
        obstacles = modeStrategy.generateObstacles(
            gameWidth = gameWidth,
            gameHeight = gameHeight,
            occupiedPositions = initialSnake.getOccupiedPositions(),
            maxObstacles = gameConfig.maxObstacles
        )
        
        // 生成初始食物
        val initialFood = Food.generateRandomWithType(
            gameWidth = gameWidth,
            gameHeight = gameHeight,
            occupiedPositions = initialSnake.getOccupiedPositions() + obstacles,
            enableEffects = gameConfig.enableEffects,
            currentScore = 0
        )
        
        // 创建初始游戏状态
        val initialGameState = GameState.Playing(
            score = 0,
            level = 1,
            speed = gameConfig.getAdjustedSpeed()
        )
        
        return GameData(
            snake = initialSnake,
            food = initialFood,
            gameState = initialGameState,
            obstacles = obstacles,
            activeEffects = emptyList(),
            gameStartTime = System.currentTimeMillis()
        )
    }
    
    /**
     * 更新游戏状态（每个游戏循环调用）
     * 
     * @param currentData 当前游戏数据
     * @param newDirection 新的移动方向（可选）
     * @return 更新后的游戏数据
     */
    fun updateGame(currentData: GameData, newDirection: Direction? = null): GameData {
        // 只有在游戏进行中才更新
        if (currentData.gameState !is GameState.Playing) {
            return currentData
        }
        
        val currentTime = System.currentTimeMillis()
        val currentSnake = currentData.snake
        val currentFood = currentData.food
        val currentGameState = currentData.gameState
        
        // 检查时间限制（时间挑战模式）
        if (modeStrategy.supportsTimeLimit()) {
            val elapsedTime = currentTime - currentData.gameStartTime
            if (elapsedTime >= gameConfig.timeLimitSeconds * 1000) {
                return currentData.copy(
                    gameState = GameState.GameOver(
                        finalScore = currentGameState.score,
                        finalLevel = currentGameState.level,
                        reason = GameState.GameOverReason.TIME_UP
                    )
                )
            }
        }
        
        // 应用当前活跃的效果
        val effectResult = effectManager.applyAllEffects(currentGameState.speed, currentTime)
        
        // 检查蛇是否会吃到食物
        val nextHeadPosition = currentSnake.head.move(newDirection ?: currentSnake.direction)
        val willEatFood = nextHeadPosition == currentFood.position
        
        // 使用游戏模式策略处理移动
        val newSnake = modeStrategy.handleMovement(
            snake = currentSnake,
            direction = newDirection,
            gameWidth = gameWidth,
            gameHeight = gameHeight,
            grow = willEatFood
        )
        
        // 检查碰撞
        val collisionResult = checkCollisions(newSnake, effectResult)
        if (collisionResult != null) {
            // 游戏结束
            return currentData.copy(
                snake = newSnake,
                gameState = GameState.GameOver(
                    finalScore = currentGameState.score,
                    finalLevel = currentGameState.level,
                    reason = collisionResult
                )
            )
        }
        
        // 处理食物消费和效果
        val (updatedFood, updatedGameState, newEffects) = if (willEatFood) {
            // 处理食物效果
            if (currentFood.hasEffect() && gameConfig.enableEffects) {
                currentFood.effect?.let { effect ->
                    effectManager.addEffect(effect, currentTime)
                }
            }
            
            // 处理缩小效果（特殊处理）
            val processedSnake = if (currentFood.type == FoodType.SHRINK && newSnake.body.size > 2) {
                newSnake.copy(body = newSnake.body.dropLast(1))
            } else {
                newSnake
            }
            
            // 生成新食物
            val newFood = Food.generateRandomWithType(
                gameWidth = gameWidth,
                gameHeight = gameHeight,
                occupiedPositions = processedSnake.getOccupiedPositions() + currentData.obstacles,
                enableEffects = gameConfig.enableEffects,
                currentScore = currentGameState.score
            )
            
            // 计算调整后的分数
            val adjustedPoints = gameConfig.difficulty.calculateScore(currentFood.points)
            val newGameState = currentGameState.addScore(adjustedPoints)
            
            Triple(newFood, newGameState, effectManager.getActiveEffects())
        } else {
            Triple(currentFood, currentGameState, effectManager.getActiveEffects())
        }
        
        return GameData(
            snake = if (willEatFood && currentFood.type == FoodType.SHRINK && newSnake.body.size > 2) {
                newSnake.copy(body = newSnake.body.dropLast(1))
            } else {
                newSnake
            },
            food = updatedFood,
            gameState = updatedGameState.copy(speed = effectResult.speed),
            obstacles = currentData.obstacles,
            activeEffects = newEffects,
            gameStartTime = currentData.gameStartTime
        )
    }
    
    /**
     * 检查碰撞
     * 
     * @param snake 要检查的蛇对象
     * @return 碰撞类型，如果没有碰撞返回null
     */
    private fun checkCollisions(snake: Snake): GameState.GameOverReason? {
        // 检查墙壁碰撞
        if (snake.checkWallCollision(gameWidth, gameHeight)) {
            return GameState.GameOverReason.WALL_COLLISION
        }
        
        // 检查自身碰撞
        if (snake.checkSelfCollision()) {
            return GameState.GameOverReason.SELF_COLLISION
        }
        
        return null
    }
    
    /**
     * 暂停游戏
     * 
     * @param currentData 当前游戏数据
     * @return 暂停状态的游戏数据
     */
    fun pauseGame(currentData: GameData): GameData {
        return if (currentData.gameState is GameState.Playing) {
            currentData.copy(
                gameState = GameState.Paused(currentData.gameState)
            )
        } else {
            currentData
        }
    }
    
    /**
     * 恢复游戏
     * 
     * @param currentData 当前游戏数据
     * @return 恢复后的游戏数据
     */
    fun resumeGame(currentData: GameData): GameData {
        return if (currentData.gameState is GameState.Paused) {
            currentData.copy(
                gameState = currentData.gameState.resume()
            )
        } else {
            currentData
        }
    }
    
    /**
     * 重置游戏
     * 
     * @return 重置后的游戏数据
     */
    fun resetGame(): GameData {
        return initializeGame()
    }
    
    /**
     * 验证移动方向是否有效
     * 
     * @param currentDirection 当前方向
     * @param newDirection 新方向
     * @return 如果新方向有效返回true，否则返回false
     */
    fun isValidDirectionChange(currentDirection: Direction, newDirection: Direction): Boolean {
        // 不能直接反向移动
        return newDirection != currentDirection.opposite()
    }
}
