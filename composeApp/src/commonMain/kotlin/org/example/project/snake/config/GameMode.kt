package org.example.project.snake.config

/**
 * 游戏模式枚举
 * 
 * 定义了不同的游戏玩法模式，每种模式都有独特的规则和挑战
 */
enum class GameMode(
    val displayName: String,
    val description: String,
    val icon: String = "🐍"
) {
    /**
     * 经典模式
     * 传统的贪吃蛇游戏，撞墙或撞到自己就游戏结束
     */
    CLASSIC(
        displayName = "经典模式",
        description = "传统贪吃蛇游戏，撞墙或撞到自己就结束",
        icon = "🐍"
    ),
    
    /**
     * 无墙模式
     * 蛇可以穿越边界，从另一侧出现，只有撞到自己才会游戏结束
     */
    BORDERLESS(
        displayName = "无墙模式", 
        description = "蛇可以穿越边界，从另一侧出现",
        icon = "🌀"
    ),
    
    /**
     * 障碍物模式
     * 游戏区域中会随机生成静态障碍物，增加游戏难度
     */
    OBSTACLES(
        displayName = "障碍物模式",
        description = "游戏区域中有静态障碍物",
        icon = "🧱"
    ),
    
    /**
     * 时间挑战模式
     * 在限定时间内尽可能获得高分，时间结束游戏结束
     */
    TIME_CHALLENGE(
        displayName = "时间挑战",
        description = "在限定时间内获得尽可能高的分数",
        icon = "⏰"
    );
    
    /**
     * 检查是否需要边界碰撞检测
     * 
     * @return 如果需要边界碰撞检测返回true，否则返回false
     */
    fun requiresBoundaryCollision(): Boolean {
        return this != BORDERLESS
    }
    
    /**
     * 检查是否支持障碍物
     * 
     * @return 如果支持障碍物返回true，否则返回false
     */
    fun supportsObstacles(): Boolean {
        return this == OBSTACLES
    }
    
    /**
     * 检查是否有时间限制
     * 
     * @return 如果有时间限制返回true，否则返回false
     */
    fun hasTimeLimit(): Boolean {
        return this == TIME_CHALLENGE
    }
    
    /**
     * 获取推荐的游戏区域大小
     * 
     * @return 推荐的游戏区域大小（宽度和高度）
     */
    fun getRecommendedSize(): Pair<Int, Int> {
        return when (this) {
            CLASSIC -> Pair(20, 20)
            BORDERLESS -> Pair(18, 18)  // 稍小一些，因为没有边界限制
            OBSTACLES -> Pair(22, 22)   // 稍大一些，为障碍物留出空间
            TIME_CHALLENGE -> Pair(20, 20)
        }
    }
    
    /**
     * 获取该模式的默认难度
     * 
     * @return 推荐的默认难度
     */
    fun getDefaultDifficulty(): Difficulty {
        return when (this) {
            CLASSIC -> Difficulty.NORMAL
            BORDERLESS -> Difficulty.EASY    // 无墙模式相对简单
            OBSTACLES -> Difficulty.HARD     // 障碍物模式更有挑战性
            TIME_CHALLENGE -> Difficulty.NORMAL
        }
    }
}