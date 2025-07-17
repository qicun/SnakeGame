package org.example.project.snake.engine

import org.example.project.snake.model.*

/**
 * 贪吃蛇游戏的核心引擎
 * 
 * 负责处理游戏逻辑、碰撞检测、分数计算等核心功能
 * 
 * @param gameWidth 游戏区域宽度（网格列数）
 * @param gameHeight 游戏区域高度（网格行数）
 */
class SnakeGameEngine(
    val gameWidth: Int = 20,
    val gameHeight: Int = 20
) {
    
    /**
     * 游戏状态数据类
     * 
     * @param snake 蛇对象
     * @param food 食物对象
     * @param gameState 游戏状态
     */
    data class GameData(
        val snake: Snake,
        val food: Food,
        val gameState: GameState
    )
    
    /**
     * 初始化游戏数据
     * 
     * @return 初始的游戏数据
     */
    fun initializeGame(): GameData {
        // 创建初始蛇（在游戏区域中央）
        val initialSnake = Snake.createInitial(
            startPosition = Position(gameWidth / 2, gameHeight / 2),
            initialDirection = Direction.RIGHT,
            initialLength = 3
        )
        
        // 生成初始食物
        val initialFood = Food.generateRandom(
            gameWidth = gameWidth,
            gameHeight = gameHeight,
            occupiedPositions = initialSnake.getOccupiedPositions()
        )
        
        // 创建初始游戏状态
        val initialGameState = GameState.Playing()
        
        return GameData(
            snake = initialSnake,
            food = initialFood,
            gameState = initialGameState
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
        
        val currentSnake = currentData.snake
        val currentFood = currentData.food
        val currentGameState = currentData.gameState
        
        // 检查蛇是否会吃到食物
        val willEatFood = currentSnake.head.move(newDirection ?: currentSnake.direction) == currentFood.position
        
        // 移动蛇
        val newSnake = currentSnake.move(newDirection, grow = willEatFood)
        
        // 检查碰撞
        val collisionResult = checkCollisions(newSnake)
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
        
        // 处理食物和分数
        val (updatedFood, updatedGameState) = if (willEatFood) {
            // 生成新食物
            val newFood = Food.generateRandomWithType(
                gameWidth = gameWidth,
                gameHeight = gameHeight,
                occupiedPositions = newSnake.getOccupiedPositions()
            )
            
            // 更新分数
            val newGameState = currentGameState.addScore(currentFood.points)
            
            Pair(newFood, newGameState)
        } else {
            Pair(currentFood, currentGameState)
        }
        
        return GameData(
            snake = newSnake,
            food = updatedFood,
            gameState = updatedGameState
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
