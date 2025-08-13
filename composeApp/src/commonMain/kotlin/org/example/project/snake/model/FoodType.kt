package org.example.project.snake.model

import androidx.compose.ui.graphics.Color

/**
 * 食物类型枚举
 * 
 * 定义了不同类型的食物，每种食物都有独特的属性和效果
 * 
 * @param displayName 显示名称
 * @param points 基础分数
 * @param color 食物颜色
 * @param rarity 稀有度（0.0-1.0，值越小越稀有）
 * @param effectFactory 效果工厂函数，用于创建食物效果
 */
enum class FoodType(
    val displayName: String,
    val points: Int,
    val color: Color,
    val rarity: Float,
    val effectFactory: () -> FoodEffect?
) {
    /**
     * 普通食物
     * 最常见的食物，提供基础分数，无特殊效果
     */
    REGULAR(
        displayName = "普通食物",
        points = 1,
        color = Color.Red,
        rarity = 0.7f,
        effectFactory = { null }
    ),
    
    /**
     * 奖励食物
     * 提供额外分数，无特殊效果
     */
    BONUS(
        displayName = "奖励食物",
        points = 5,
        color = Color(0xFFFFD700), // 金色
        rarity = 0.2f,
        effectFactory = { null }
    ),
    
    /**
     * 加速食物
     * 临时提高蛇的移动速度
     */
    SPEED_UP(
        displayName = "加速食物",
        points = 2,
        color = Color.Blue,
        rarity = 0.15f,
        effectFactory = { SpeedUpEffect(3000L) }
    ),
    
    /**
     * 减速食物
     * 临时降低蛇的移动速度
     */
    SPEED_DOWN(
        displayName = "减速食物",
        points = 2,
        color = Color(0xFF800080), // 紫色
        rarity = 0.15f,
        effectFactory = { SpeedDownEffect(3000L) }
    ),
    
    /**
     * 幽灵食物
     * 临时允许蛇穿过墙壁或自身
     */
    GHOST(
        displayName = "幽灵食物",
        points = 3,
        color = Color.Gray,
        rarity = 0.08f,
        effectFactory = { GhostEffect(5000L) }
    ),
    
    /**
     * 缩小食物
     * 减少蛇的长度（如果长度大于最小值）
     */
    SHRINK(
        displayName = "缩小食物",
        points = 1,
        color = Color(0xFFFF4500), // 橙红色
        rarity = 0.1f,
        effectFactory = { ShrinkEffect() }
    );
    
    /**
     * 根据稀有度随机选择食物类型
     * 
     * @param enableEffects 是否启用特殊效果食物
     * @return 随机选择的食物类型
     */
    companion object {
        /**
         * 根据稀有度权重随机选择食物类型
         */
        fun randomType(enableEffects: Boolean = true): FoodType {
            val availableTypes = if (enableEffects) {
                values().toList()
            } else {
                listOf(REGULAR, BONUS) // 只包含无效果的食物
            }
            
            // 计算总权重
            val totalWeight = availableTypes.sumOf { it.rarity.toDouble() }
            
            // 生成随机数
            val random = kotlin.random.Random.nextDouble(totalWeight)
            
            // 根据权重选择
            var currentWeight = 0.0
            for (type in availableTypes) {
                currentWeight += type.rarity
                if (random <= currentWeight) {
                    return type
                }
            }
            
            // 默认返回普通食物
            return REGULAR
        }
        
        /**
         * 获取所有有效果的食物类型
         */
        fun getEffectTypes(): List<FoodType> {
            return values().filter { it.effectFactory() != null }
        }
        
        /**
         * 获取所有无效果的食物类型
         */
        fun getBasicTypes(): List<FoodType> {
            return values().filter { it.effectFactory() == null }
        }
        
        /**
         * 根据分数范围获取合适的食物类型
         */
        fun getTypeByScoreRange(score: Int): List<FoodType> {
            return when {
                score < 50 -> listOf(REGULAR, BONUS)
                score < 150 -> listOf(REGULAR, BONUS, SPEED_UP, SPEED_DOWN)
                score < 300 -> listOf(REGULAR, BONUS, SPEED_UP, SPEED_DOWN, SHRINK)
                else -> values().toList()
            }
        }
    }
    
    /**
     * 检查是否有特殊效果
     */
    fun hasEffect(): Boolean {
        return effectFactory() != null
    }
    
    /**
     * 创建食物效果实例
     */
    fun createEffect(): FoodEffect? {
        return effectFactory()
    }
    
    /**
     * 获取食物的显示符号
     */
    fun getDisplaySymbol(): String {
        return when (this) {
            REGULAR -> "●"
            BONUS -> "★"
            SPEED_UP -> "▲"
            SPEED_DOWN -> "▼"
            GHOST -> "◐"
            SHRINK -> "◦"
        }
    }
}