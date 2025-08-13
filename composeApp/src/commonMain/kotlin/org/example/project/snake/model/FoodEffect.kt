package org.example.project.snake.model

/**
 * 食物效果接口
 * 
 * 定义了食物特殊效果的通用行为
 */
interface FoodEffect {
    /**
     * 效果持续时间（毫秒）
     */
    val duration: Long
    
    /**
     * 效果类型
     */
    val type: EffectType
    
    /**
     * 效果开始时间戳
     */
    var startTime: Long
    
    /**
     * 应用效果到游戏状态
     * 
     * @param currentSpeed 当前游戏速度
     * @param canPassThroughWalls 当前是否可以穿墙
     * @param canPassThroughSelf 当前是否可以穿过自身
     * @return 修改后的效果参数
     */
    fun applyEffect(
        currentSpeed: Long,
        canPassThroughWalls: Boolean,
        canPassThroughSelf: Boolean
    ): EffectResult
    
    /**
     * 检查效果是否已过期
     * 
     * @param currentTime 当前时间戳
     * @return 如果效果已过期返回true，否则返回false
     */
    fun isExpired(currentTime: Long): Boolean {
        return currentTime - startTime >= duration
    }
    
    /**
     * 获取剩余时间
     * 
     * @param currentTime 当前时间戳
     * @return 剩余时间（毫秒），如果已过期返回0
     */
    fun getRemainingTime(currentTime: Long): Long {
        val remaining = duration - (currentTime - startTime)
        return maxOf(0L, remaining)
    }
}

/**
 * 效果类型枚举
 */
enum class EffectType(val displayName: String, val description: String) {
    SPEED_UP("加速", "提高蛇的移动速度"),
    SPEED_DOWN("减速", "降低蛇的移动速度"),
    GHOST("幽灵", "可以穿过墙壁和自身"),
    SHRINK("缩小", "减少蛇的长度")
}

/**
 * 效果应用结果
 * 
 * @param speed 修改后的游戏速度
 * @param canPassThroughWalls 是否可以穿墙
 * @param canPassThroughSelf 是否可以穿过自身
 */
data class EffectResult(
    val speed: Long,
    val canPassThroughWalls: Boolean,
    val canPassThroughSelf: Boolean
)

/**
 * 加速效果实现
 */
class SpeedUpEffect(
    override val duration: Long = 3000L
) : FoodEffect {
    override val type = EffectType.SPEED_UP
    override var startTime: Long = 0L
    
    override fun applyEffect(
        currentSpeed: Long,
        canPassThroughWalls: Boolean,
        canPassThroughSelf: Boolean
    ): EffectResult {
        val newSpeed = (currentSpeed * 0.7f).toLong().coerceAtLeast(50L)
        return EffectResult(newSpeed, canPassThroughWalls, canPassThroughSelf)
    }
}

/**
 * 减速效果实现
 */
class SpeedDownEffect(
    override val duration: Long = 3000L
) : FoodEffect {
    override val type = EffectType.SPEED_DOWN
    override var startTime: Long = 0L
    
    override fun applyEffect(
        currentSpeed: Long,
        canPassThroughWalls: Boolean,
        canPassThroughSelf: Boolean
    ): EffectResult {
        val newSpeed = (currentSpeed * 1.5f).toLong().coerceAtMost(1000L)
        return EffectResult(newSpeed, canPassThroughWalls, canPassThroughSelf)
    }
}

/**
 * 幽灵效果实现
 */
class GhostEffect(
    override val duration: Long = 5000L
) : FoodEffect {
    override val type = EffectType.GHOST
    override var startTime: Long = 0L
    
    override fun applyEffect(
        currentSpeed: Long,
        canPassThroughWalls: Boolean,
        canPassThroughSelf: Boolean
    ): EffectResult {
        return EffectResult(currentSpeed, true, true)
    }
}

/**
 * 缩小效果实现
 * 注意：这是一个即时效果，不需要持续时间
 */
class ShrinkEffect : FoodEffect {
    override val duration: Long = 0L // 即时效果
    override val type = EffectType.SHRINK
    override var startTime: Long = 0L
    
    override fun applyEffect(
        currentSpeed: Long,
        canPassThroughWalls: Boolean,
        canPassThroughSelf: Boolean
    ): EffectResult {
        // 缩小效果在Snake类中处理，这里不修改其他参数
        return EffectResult(currentSpeed, canPassThroughWalls, canPassThroughSelf)
    }
    
    override fun isExpired(currentTime: Long): Boolean {
        return true // 即时效果，立即过期
    }
}

/**
 * 效果管理器
 * 
 * 管理所有活跃的食物效果
 */
class EffectManager {
    private val activeEffects = mutableListOf<FoodEffect>()
    
    /**
     * 添加新效果
     * 
     * @param effect 要添加的效果
     * @param currentTime 当前时间戳
     */
    fun addEffect(effect: FoodEffect, currentTime: Long) {
        effect.startTime = currentTime
        
        // 如果是相同类型的效果，移除旧的
        activeEffects.removeAll { it.type == effect.type }
        
        // 添加新效果
        activeEffects.add(effect)
    }
    
    /**
     * 应用所有活跃效果
     * 
     * @param baseSpeed 基础游戏速度
     * @param currentTime 当前时间戳
     * @return 应用所有效果后的结果
     */
    fun applyAllEffects(baseSpeed: Long, currentTime: Long): EffectResult {
        // 清理过期效果
        activeEffects.removeAll { it.isExpired(currentTime) }
        
        // 应用所有活跃效果
        var result = EffectResult(baseSpeed, false, false)
        
        for (effect in activeEffects) {
            result = effect.applyEffect(
                result.speed,
                result.canPassThroughWalls,
                result.canPassThroughSelf
            )
        }
        
        return result
    }
    
    /**
     * 获取所有活跃效果
     */
    fun getActiveEffects(): List<FoodEffect> {
        return activeEffects.toList()
    }
    
    /**
     * 清除所有效果
     */
    fun clearAllEffects() {
        activeEffects.clear()
    }
    
    /**
     * 检查是否有特定类型的效果
     */
    fun hasEffect(type: EffectType): Boolean {
        return activeEffects.any { it.type == type }
    }
}