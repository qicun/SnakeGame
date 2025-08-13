package org.example.project.snake.model

import kotlin.random.Random

/**
 * 食物类
 * 
 * 表示游戏中的食物对象，包含位置、类型和效果信息
 * 
 * @param position 食物位置
 * @param type 食物类型
 * @param effect 食物效果（可选）
 * @param createdTime 创建时间戳，用于效果计算
 */
data class Food(
    val position: Position,
    val type: FoodType,
    val effect: FoodEffect? = null,
    val createdTime: Long = System.currentTimeMillis()
) {
    
    /**
     * 获取食物分数
     */
    val points: Int
        get() = type.points
    
    /**
     * 获取食物颜色
     */
    val color
        get() = type.color
    
    /**
     * 检查是否有特殊效果
     */
    fun hasEffect(): Boolean {
        return effect != null
    }
    
    /**
     * 获取显示符号
     */
    fun getDisplaySymbol(): String {
        return type.getDisplaySymbol()
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
    
    companion object {
        
        /**
         * 在指定区域内随机生成普通食物
         * 
         * @param gameWidth 游戏区域宽度
         * @param gameHeight 游戏区域高度
         * @param occupiedPositions 已被占用的位置列表（蛇身位置）
         * @return 新生成的普通食物对象
         */
        fun generateRandom(
            gameWidth: Int,
            gameHeight: Int,
            occupiedPositions: List<Position>
        ): Food {
            val randomPosition = generateRandomPosition(gameWidth, gameHeight, occupiedPositions)
            
            return Food(
                position = randomPosition,
                type = FoodType.REGULAR,
                effect = null
            )
        }
        
        /**
         * 生成带有随机类型和效果的食物
         * 
         * @param gameWidth 游戏区域宽度
         * @param gameHeight 游戏区域高度
         * @param occupiedPositions 已被占用的位置列表
         * @param enableEffects 是否启用特殊效果
         * @param currentScore 当前分数，用于确定可用的食物类型
         * @return 新生成的带随机类型的食物对象
         */
        fun generateRandomWithType(
            gameWidth: Int,
            gameHeight: Int,
            occupiedPositions: List<Position>,
            enableEffects: Boolean = true,
            currentScore: Int = 0
        ): Food {
            val randomPosition = generateRandomPosition(gameWidth, gameHeight, occupiedPositions)
            
            // 根据分数确定可用的食物类型
            val availableTypes = if (enableEffects) {
                FoodType.getTypeByScoreRange(currentScore)
            } else {
                FoodType.getBasicTypes()
            }
            
            // 从可用类型中随机选择
            val foodType = if (availableTypes.isNotEmpty()) {
                // 使用加权随机选择
                selectWeightedRandom(availableTypes)
            } else {
                FoodType.REGULAR
            }
            
            // 创建效果（如果有）
            val effect = if (enableEffects) {
                foodType.createEffect()
            } else {
                null
            }
            
            return Food(
                position = randomPosition,
                type = foodType,
                effect = effect
            )
        }
        
        /**
         * 生成特定类型的食物
         * 
         * @param gameWidth 游戏区域宽度
         * @param gameHeight 游戏区域高度
         * @param occupiedPositions 已被占用的位置列表
         * @param foodType 指定的食物类型
         * @param enableEffects 是否启用效果
         * @return 指定类型的食物对象
         */
        fun generateSpecificType(
            gameWidth: Int,
            gameHeight: Int,
            occupiedPositions: List<Position>,
            foodType: FoodType,
            enableEffects: Boolean = true
        ): Food {
            val randomPosition = generateRandomPosition(gameWidth, gameHeight, occupiedPositions)
            
            val effect = if (enableEffects) {
                foodType.createEffect()
            } else {
                null
            }
            
            return Food(
                position = randomPosition,
                type = foodType,
                effect = effect
            )
        }
        
        /**
         * 生成随机位置，确保不与已占用位置重叠
         * 
         * @param gameWidth 游戏区域宽度
         * @param gameHeight 游戏区域高度
         * @param occupiedPositions 已被占用的位置列表
         * @return 随机生成的有效位置
         */
        private fun generateRandomPosition(
            gameWidth: Int,
            gameHeight: Int,
            occupiedPositions: List<Position>
        ): Position {
            var randomPosition: Position
            var attempts = 0
            val maxAttempts = gameWidth * gameHeight
            
            // 确保食物不会生成在已占用的位置上
            do {
                randomPosition = Position(
                    x = Random.nextInt(gameWidth),
                    y = Random.nextInt(gameHeight)
                )
                attempts++
                
                // 防止无限循环（当游戏区域几乎被完全占满时）
                if (attempts >= maxAttempts) {
                    // 寻找第一个未被占用的位置
                    for (x in 0 until gameWidth) {
                        for (y in 0 until gameHeight) {
                            val pos = Position(x, y)
                            if (!occupiedPositions.contains(pos)) {
                                return pos
                            }
                        }
                    }
                    // 如果所有位置都被占用，返回一个默认位置
                    return Position(0, 0)
                }
            } while (occupiedPositions.contains(randomPosition))
            
            return randomPosition
        }
        
        /**
         * 从可用类型列表中进行加权随机选择
         * 
         * @param availableTypes 可用的食物类型列表
         * @return 随机选择的食物类型
         */
        private fun selectWeightedRandom(availableTypes: List<FoodType>): FoodType {
            if (availableTypes.isEmpty()) return FoodType.REGULAR
            if (availableTypes.size == 1) return availableTypes[0]
            
            // 计算总权重
            val totalWeight = availableTypes.sumOf { it.rarity.toDouble() }
            
            // 生成随机数
            val random = Random.nextDouble(totalWeight)
            
            // 根据权重选择
            var currentWeight = 0.0
            for (type in availableTypes) {
                currentWeight += type.rarity
                if (random <= currentWeight) {
                    return type
                }
            }
            
            // 默认返回列表中的第一个
            return availableTypes[0]
        }
        
        /**
         * 创建测试用食物
         * 
         * @param x X坐标
         * @param y Y坐标
         * @param type 食物类型
         * @return 测试用食物对象
         */
        fun createTestFood(x: Int, y: Int, type: FoodType = FoodType.REGULAR): Food {
            return Food(
                position = Position(x, y),
                type = type,
                effect = type.createEffect()
            )
        }
        
        /**
         * 在指定位置创建食物（主要用于测试）
         * 
         * @param position 食物位置
         * @param type 食物类型
         * @return 食物对象
         */
        fun createAt(position: Position, type: FoodType = FoodType.REGULAR): Food {
            return Food(
                position = position,
                type = type,
                effect = type.createEffect()
            )
        }
    }
}