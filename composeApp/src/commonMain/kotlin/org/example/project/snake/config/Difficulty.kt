package org.example.project.snake.config

/**
 * 游戏难度枚举
 * 
 * 定义了不同的游戏难度级别，影响游戏速度、分数倍数等参数
 */
enum class Difficulty(
    val displayName: String,
    val description: String,
    val speedMultiplier: Float,
    val scoreMultiplier: Float,
    val initialSnakeLength: Int
) {
    /**
     * 简单难度
     * 适合新手玩家，游戏速度较慢，分数倍数较低
     */
    EASY(
        displayName = "简单",
        description = "适合新手，游戏速度较慢",
        speedMultiplier = 1.5f,      // 速度降低50%
        scoreMultiplier = 0.8f,      // 分数降低20%
        initialSnakeLength = 2       // 初始长度较短
    ),
    
    /**
     * 普通难度
     * 标准的游戏体验，平衡的参数设置
     */
    NORMAL(
        displayName = "普通",
        description = "标准的游戏体验",
        speedMultiplier = 1.0f,      // 标准速度
        scoreMultiplier = 1.0f,      // 标准分数
        initialSnakeLength = 3       // 标准初始长度
    ),
    
    /**
     * 困难难度
     * 适合有经验的玩家，游戏速度较快，分数倍数较高
     */
    HARD(
        displayName = "困难",
        description = "适合有经验的玩家，速度较快",
        speedMultiplier = 0.7f,      // 速度提高30%
        scoreMultiplier = 1.5f,      // 分数提高50%
        initialSnakeLength = 4       // 初始长度较长
    ),
    
    /**
     * 专家难度
     * 极具挑战性，游戏速度很快，分数倍数很高
     */
    EXPERT(
        displayName = "专家",
        description = "极具挑战性，速度很快",
        speedMultiplier = 0.5f,      // 速度提高100%
        scoreMultiplier = 2.0f,      // 分数提高100%
        initialSnakeLength = 5       // 初始长度最长
    );
    
    /**
     * 计算基于基础速度的实际游戏速度
     * 
     * @param baseSpeed 基础游戏速度（毫秒）
     * @return 调整后的游戏速度
     */
    fun calculateSpeed(baseSpeed: Long): Long {
        return (baseSpeed * speedMultiplier).toLong().coerceAtLeast(50L)
    }
    
    /**
     * 计算基于基础分数的实际得分
     * 
     * @param baseScore 基础分数
     * @return 调整后的分数
     */
    fun calculateScore(baseScore: Int): Int {
        return (baseScore * scoreMultiplier).toInt().coerceAtLeast(1)
    }
    
    /**
     * 获取升级到下一个难度所需的分数阈值
     * 
     * @return 升级所需分数，如果已是最高难度则返回null
     */
    fun getUpgradeThreshold(): Int? {
        return when (this) {
            EASY -> 100
            NORMAL -> 300
            HARD -> 600
            EXPERT -> null  // 已是最高难度
        }
    }
    
    /**
     * 获取下一个难度级别
     * 
     * @return 下一个难度级别，如果已是最高难度则返回null
     */
    fun getNextDifficulty(): Difficulty? {
        return when (this) {
            EASY -> NORMAL
            NORMAL -> HARD
            HARD -> EXPERT
            EXPERT -> null
        }
    }
    
    /**
     * 检查是否可以升级到下一个难度
     * 
     * @param currentScore 当前分数
     * @return 如果可以升级返回true，否则返回false
     */
    fun canUpgrade(currentScore: Int): Boolean {
        val threshold = getUpgradeThreshold()
        return threshold != null && currentScore >= threshold
    }
}