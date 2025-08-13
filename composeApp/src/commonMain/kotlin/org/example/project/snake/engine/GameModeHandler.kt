package org.example.project.snake.engine

import org.example.project.snake.config.GameMode
import org.example.project.snake.model.*

/**
 * 游戏模式处理器接口
 * 
 * 定义了不同游戏模式的通用行为，使用策略模式实现
 */
interface GameModeStrategy {
    
    /**
     * 处理蛇的移动逻辑
     * 
     * @param snake 当前蛇对象
     * @param direction 移动方向
     * @param gameWidth 游戏区域宽度
     * @param gameHeight 游戏区域高度
     * @param grow 是否需要增长
     * @return 移动后的蛇对象
     */
    fun handleMovement(
        snake: Snake,
        direction: Direction?,
        gameWidth: Int,
        gameHeight: Int,
        grow: Boolean = false
    ): Snake
    
    /**
     * 检查边界碰撞
     * 
     * @param snake 蛇对象
     * @param gameWidth 游戏区域宽度
     * @param gameHeight 游戏区域高度
     * @return 如果发生边界碰撞返回true，否则返回false
     */
    fun checkBoundaryCollision(
        snake: Snake,
        gameWidth: Int,
        gameHeight: Int
    ): Boolean
    
    /**
     * 生成障碍物
     * 
     * @param gameWidth 游戏区域宽度
     * @param gameHeight 游戏区域高度
     * @param occupiedPositions 已被占用的位置
     * @param maxObstacles 最大障碍物数量
     * @return 障碍物位置列表
     */
    fun generateObstacles(
        gameWidth: Int,
        gameHeight: Int,
        occupiedPositions: List<Position>,
        maxObstacles: Int
    ): List<Position>
    
    /**
     * 检查是否支持时间限制
     * 
     * @return 如果支持时间限制返回true，否则返回false
     */
    fun supportsTimeLimit(): Boolean
}

/**
 * 经典模式策略实现
 * 
 * 传统的贪吃蛇游戏规则：撞墙或撞到自己就游戏结束
 */
class ClassicModeStrategy : GameModeStrategy {
    
    override fun handleMovement(
        snake: Snake,
        direction: Direction?,
        gameWidth: Int,
        gameHeight: Int,
        grow: Boolean
    ): Snake {
        // 经典模式下的标准移动
        return snake.move(direction, grow)
    }
    
    override fun checkBoundaryCollision(
        snake: Snake,
        gameWidth: Int,
        gameHeight: Int
    ): Boolean {
        val head = snake.head
        return head.x < 0 || head.x >= gameWidth || head.y < 0 || head.y >= gameHeight
    }
    
    override fun generateObstacles(
        gameWidth: Int,
        gameHeight: Int,
        occupiedPositions: List<Position>,
        maxObstacles: Int
    ): List<Position> {
        // 经典模式不生成障碍物
        return emptyList()
    }
    
    override fun supportsTimeLimit(): Boolean = false
}

/**
 * 无墙模式策略实现
 * 
 * 蛇可以穿越边界，从另一侧出现
 */
class BorderlessModeStrategy : GameModeStrategy {
    
    override fun handleMovement(
        snake: Snake,
        direction: Direction?,
        gameWidth: Int,
        gameHeight: Int,
        grow: Boolean
    ): Snake {
        // 先进行标准移动
        val movedSnake = snake.move(direction, grow)
        
        // 然后处理边界穿越
        val wrappedHead = wrapPosition(movedSnake.head, gameWidth, gameHeight)
        
        // 如果头部位置发生了包装，更新蛇的位置
        return if (wrappedHead != movedSnake.head) {
            val newBody = listOf(wrappedHead) + movedSnake.body.drop(1)
            movedSnake.copy(body = newBody)
        } else {
            movedSnake
        }
    }
    
    override fun checkBoundaryCollision(
        snake: Snake,
        gameWidth: Int,
        gameHeight: Int
    ): Boolean {
        // 无墙模式不检查边界碰撞
        return false
    }
    
    override fun generateObstacles(
        gameWidth: Int,
        gameHeight: Int,
        occupiedPositions: List<Position>,
        maxObstacles: Int
    ): List<Position> {
        // 无墙模式不生成障碍物
        return emptyList()
    }
    
    override fun supportsTimeLimit(): Boolean = false
    
    /**
     * 将位置包装到游戏边界内
     * 
     * @param position 原始位置
     * @param gameWidth 游戏区域宽度
     * @param gameHeight 游戏区域高度
     * @return 包装后的位置
     */
    private fun wrapPosition(position: Position, gameWidth: Int, gameHeight: Int): Position {
        val wrappedX = when {
            position.x < 0 -> gameWidth - 1
            position.x >= gameWidth -> 0
            else -> position.x
        }
        
        val wrappedY = when {
            position.y < 0 -> gameHeight - 1
            position.y >= gameHeight -> 0
            else -> position.y
        }
        
        return Position(wrappedX, wrappedY)
    }
}

/**
 * 障碍物模式策略实现
 * 
 * 游戏区域中会有静态障碍物，增加游戏难度
 */
class ObstaclesModeStrategy : GameModeStrategy {
    
    override fun handleMovement(
        snake: Snake,
        direction: Direction?,
        gameWidth: Int,
        gameHeight: Int,
        grow: Boolean
    ): Snake {
        // 障碍物模式下的标准移动
        return snake.move(direction, grow)
    }
    
    override fun checkBoundaryCollision(
        snake: Snake,
        gameWidth: Int,
        gameHeight: Int
    ): Boolean {
        val head = snake.head
        return head.x < 0 || head.x >= gameWidth || head.y < 0 || head.y >= gameHeight
    }
    
    override fun generateObstacles(
        gameWidth: Int,
        gameHeight: Int,
        occupiedPositions: List<Position>,
        maxObstacles: Int
    ): List<Position> {
        val obstacles = mutableListOf<Position>()
        val availablePositions = mutableListOf<Position>()
        
        // 收集所有可用位置
        for (x in 0 until gameWidth) {
            for (y in 0 until gameHeight) {
                val position = Position(x, y)
                if (!occupiedPositions.contains(position)) {
                    availablePositions.add(position)
                }
            }
        }
        
        // 随机选择障碍物位置
        val actualObstacleCount = minOf(maxObstacles, availablePositions.size)
        repeat(actualObstacleCount) {
            if (availablePositions.isNotEmpty()) {
                val randomIndex = kotlin.random.Random.nextInt(availablePositions.size)
                val obstaclePosition = availablePositions.removeAt(randomIndex)
                obstacles.add(obstaclePosition)
            }
        }
        
        return obstacles
    }
    
    override fun supportsTimeLimit(): Boolean = false
}

/**
 * 时间挑战模式策略实现
 * 
 * 在限定时间内尽可能获得高分
 */
class TimeChallengeStrategy : GameModeStrategy {
    
    override fun handleMovement(
        snake: Snake,
        direction: Direction?,
        gameWidth: Int,
        gameHeight: Int,
        grow: Boolean
    ): Snake {
        // 时间挑战模式下的标准移动
        return snake.move(direction, grow)
    }
    
    override fun checkBoundaryCollision(
        snake: Snake,
        gameWidth: Int,
        gameHeight: Int
    ): Boolean {
        val head = snake.head
        return head.x < 0 || head.x >= gameWidth || head.y < 0 || head.y >= gameHeight
    }
    
    override fun generateObstacles(
        gameWidth: Int,
        gameHeight: Int,
        occupiedPositions: List<Position>,
        maxObstacles: Int
    ): List<Position> {
        // 时间挑战模式不生成障碍物，专注于快速收集食物
        return emptyList()
    }
    
    override fun supportsTimeLimit(): Boolean = true
}

/**
 * 游戏模式处理器工厂
 * 
 * 根据游戏模式创建相应的策略实现
 */
object GameModeHandler {
    
    /**
     * 根据游戏模式创建策略实现
     * 
     * @param gameMode 游戏模式
     * @return 对应的策略实现
     */
    fun createStrategy(gameMode: GameMode): GameModeStrategy {
        return when (gameMode) {
            GameMode.CLASSIC -> ClassicModeStrategy()
            GameMode.BORDERLESS -> BorderlessModeStrategy()
            GameMode.OBSTACLES -> ObstaclesModeStrategy()
            GameMode.TIME_CHALLENGE -> TimeChallengeStrategy()
        }
    }
    
    /**
     * 检查蛇是否与障碍物碰撞
     * 
     * @param snake 蛇对象
     * @param obstacles 障碍物位置列表
     * @return 如果发生碰撞返回true，否则返回false
     */
    fun checkObstacleCollision(snake: Snake, obstacles: List<Position>): Boolean {
        return obstacles.contains(snake.head)
    }
    
    /**
     * 获取所有支持的游戏模式
     * 
     * @return 游戏模式列表
     */
    fun getSupportedModes(): List<GameMode> {
        return GameMode.values().toList()
    }
    
    /**
     * 检查游戏模式是否需要特殊处理
     * 
     * @param gameMode 游戏模式
     * @return 如果需要特殊处理返回true，否则返回false
     */
    fun requiresSpecialHandling(gameMode: GameMode): Boolean {
        return when (gameMode) {
            GameMode.CLASSIC -> false
            GameMode.BORDERLESS -> true
            GameMode.OBSTACLES -> true
            GameMode.TIME_CHALLENGE -> true
        }
    }
}