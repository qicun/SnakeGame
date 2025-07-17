package org.example.project.snake.model

import kotlin.random.Random

/**
 * 表示游戏中的食物
 * 
 * @param position 食物的位置
 * @param type 食物类型
 * @param points 食物的分数值
 */
data class Food(
    val position: Position,
    val type: FoodType = FoodType.NORMAL,
    val points: Int = type.defaultPoints
) {
    
    /**
     * 食物类型枚举
     */
    enum class FoodType(val defaultPoints: Int) {
        NORMAL(10),      // 普通食物，10分
        BONUS(25),       // 奖励食物，25分
        SUPER(50)        // 超级食物，50分
    }
    
    companion object {
        /**
         * 在指定区域内生成随机食物，避免与蛇身重叠
         * 
         * @param gameWidth 游戏区域宽度
         * @param gameHeight 游戏区域高度
         * @param occupiedPositions 已被占据的位置（通常是蛇身）
         * @param foodType 食物类型（默认为普通食物）
         * @return 新生成的食物对象
         */
        fun generateRandom(
            gameWidth: Int,
            gameHeight: Int,
            occupiedPositions: Set<Position>,
            foodType: FoodType = FoodType.NORMAL
        ): Food {
            // 获取所有可用位置
            val availablePositions = mutableListOf<Position>()
            
            for (x in 0 until gameWidth) {
                for (y in 0 until gameHeight) {
                    val position = Position(x, y)
                    if (position !in occupiedPositions) {
                        availablePositions.add(position)
                    }
                }
            }
            
            // 如果没有可用位置，抛出异常
            if (availablePositions.isEmpty()) {
                throw IllegalStateException("没有可用位置生成食物")
            }
            
            // 随机选择一个位置
            val randomPosition = availablePositions[Random.nextInt(availablePositions.size)]
            
            return Food(
                position = randomPosition,
                type = foodType,
                points = foodType.defaultPoints
            )
        }
        
        /**
         * 生成随机类型的食物
         * 
         * @param gameWidth 游戏区域宽度
         * @param gameHeight 游戏区域高度
         * @param occupiedPositions 已被占据的位置
         * @param bonusChance 生成奖励食物的概率（0.0-1.0）
         * @param superChance 生成超级食物的概率（0.0-1.0）
         * @return 新生成的食物对象
         */
        fun generateRandomWithType(
            gameWidth: Int,
            gameHeight: Int,
            occupiedPositions: Set<Position>,
            bonusChance: Double = 0.15,  // 15%概率生成奖励食物
            superChance: Double = 0.05   // 5%概率生成超级食物
        ): Food {
            val randomValue = Random.nextDouble()
            
            val foodType = when {
                randomValue < superChance -> FoodType.SUPER
                randomValue < superChance + bonusChance -> FoodType.BONUS
                else -> FoodType.NORMAL
            }
            
            return generateRandom(gameWidth, gameHeight, occupiedPositions, foodType)
        }
        
        /**
         * 在指定位置创建食物（主要用于测试）
         * 
         * @param position 食物位置
         * @param type 食物类型
         * @return 食物对象
         */
        fun createAt(position: Position, type: FoodType = FoodType.NORMAL): Food {
            return Food(
                position = position,
                type = type,
                points = type.defaultPoints
            )
        }
    }
    
    /**
     * 检查食物是否在指定边界内
     * 
     * @param gameWidth 游戏区域宽度
     * @param gameHeight 游戏区域高度
     * @return 如果在边界内返回true，否则返回false
     */
    fun isInBounds(gameWidth: Int, gameHeight: Int): Boolean {
        return position.isInBounds(gameWidth, gameHeight)
    }
}
