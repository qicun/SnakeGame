package org.example.project.snake.animation

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.example.project.snake.model.Position
import org.example.project.snake.model.FoodType

/**
 * 游戏动画管理器
 * 
 * 负责管理所有游戏动画的播放、暂停、停止等操作
 * 使用Compose动画API实现流畅的动画效果
 */
class AnimationManager(
    private val scope: CoroutineScope
) {
    
    // 活跃的动画列表
    private val _activeAnimations = mutableStateMapOf<String, GameAnimation>()
    val activeAnimations: Map<String, GameAnimation> = _activeAnimations
    
    // 动画配置
    private val animationConfig = AnimationConfig()
    
    /**
     * 启动动画
     * 
     * @param animation 要启动的动画
     * @return 动画ID，用于后续控制
     */
    fun startAnimation(animation: GameAnimation): String {
        val animationId = generateAnimationId(animation)
        _activeAnimations[animationId] = animation
        
        // 启动动画协程
        scope.launch {
            animation.start()
            // 动画完成后自动清理
            _activeAnimations.remove(animationId)
        }
        
        return animationId
    }
    
    /**
     * 停止指定动画
     * 
     * @param animationId 动画ID
     */
    fun stopAnimation(animationId: String) {
        _activeAnimations[animationId]?.stop()
        _activeAnimations.remove(animationId)
    }
    
    /**
     * 停止所有动画
     */
    fun stopAllAnimations() {
        _activeAnimations.values.forEach { it.stop() }
        _activeAnimations.clear()
    }
    
    /**
     * 暂停所有动画
     */
    fun pauseAllAnimations() {
        _activeAnimations.values.forEach { it.pause() }
    }
    
    /**
     * 恢复所有动画
     */
    fun resumeAllAnimations() {
        _activeAnimations.values.forEach { it.resume() }
    }
    
    /**
     * 检查是否有指定类型的动画正在播放
     * 
     * @param animationType 动画类型
     * @return 是否有该类型动画正在播放
     */
    fun hasActiveAnimation(animationType: AnimationType): Boolean {
        return _activeAnimations.values.any { it.type == animationType }
    }
    
    /**
     * 获取动画配置
     */
    fun getAnimationConfig(): AnimationConfig = animationConfig
    
    /**
     * 更新动画配置
     * 
     * @param newConfig 新的动画配置
     */
    fun updateAnimationConfig(newConfig: AnimationConfig) {
        // 这里可以实现动画配置的热更新
        // 例如调整动画速度、启用/禁用某些动画等
    }
    
    /**
     * 生成动画ID
     */
    private fun generateAnimationId(animation: GameAnimation): String {
        return "${animation.type.name}_${System.currentTimeMillis()}_${animation.hashCode()}"
    }
}

/**
 * 游戏动画基类
 * 
 * 定义了所有游戏动画的通用接口和行为
 */
abstract class GameAnimation {
    abstract val type: AnimationType
    abstract val duration: Long
    
    // 动画状态
    private var _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying
    
    private var _isPaused = mutableStateOf(false)
    val isPaused: State<Boolean> = _isPaused
    
    /**
     * 启动动画
     */
    abstract suspend fun start()
    
    /**
     * 停止动画
     */
    open fun stop() {
        _isPlaying.value = false
        _isPaused.value = false
    }
    
    /**
     * 暂停动画
     */
    open fun pause() {
        if (_isPlaying.value) {
            _isPaused.value = true
        }
    }
    
    /**
     * 恢复动画
     */
    open fun resume() {
        if (_isPlaying.value && _isPaused.value) {
            _isPaused.value = false
        }
    }
    
    /**
     * 设置动画播放状态
     */
    protected fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }
}

/**
 * 蛇移动动画
 * 
 * 实现蛇从一个位置平滑移动到另一个位置的动画效果
 */
class SnakeMoveAnimation(
    private val fromPosition: Position,
    private val toPosition: Position,
    override val duration: Long = 200L
) : GameAnimation() {
    
    override val type = AnimationType.SNAKE_MOVE
    
    // 动画值
    private val _animatedX = Animatable(fromPosition.x.toFloat())
    private val _animatedY = Animatable(fromPosition.y.toFloat())
    
    val animatedX: State<Float> = _animatedX.asState()
    val animatedY: State<Float> = _animatedY.asState()
    
    override suspend fun start() {
        setPlaying(true)
        
        // 同时动画X和Y坐标
        kotlinx.coroutines.coroutineScope {
            launch {
                _animatedX.animateTo(
                    targetValue = toPosition.x.toFloat(),
                    animationSpec = tween(
                        durationMillis = duration.toInt(),
                        easing = LinearEasing
                    )
                )
            }
            launch {
                _animatedY.animateTo(
                    targetValue = toPosition.y.toFloat(),
                    animationSpec = tween(
                        durationMillis = duration.toInt(),
                        easing = LinearEasing
                    )
                )
            }
        }
        
        setPlaying(false)
    }
    
    override fun stop() {
        super.stop()
        kotlinx.coroutines.runBlocking {
            _animatedX.stop()
            _animatedY.stop()
        }
    }
}

/**
 * 食物出现动画
 * 
 * 实现食物出现时的缩放动画效果
 */
class FoodSpawnAnimation(
    private val position: Position,
    private val foodType: FoodType,
    override val duration: Long = 300L
) : GameAnimation() {
    
    override val type = AnimationType.FOOD_SPAWN
    
    // 缩放动画值
    private val _scale = Animatable(0f)
    val scale: State<Float> = _scale.asState()
    
    // 旋转动画值（特殊食物）
    private val _rotation = Animatable(0f)
    val rotation: State<Float> = _rotation.asState()
    
    override suspend fun start() {
        setPlaying(true)
        
        kotlinx.coroutines.coroutineScope {
            // 缩放动画
            launch {
                _scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = duration.toInt(),
                        easing = FastOutSlowInEasing
                    )
                )
            }
            
            // 特殊食物添加旋转效果
            if (foodType != FoodType.REGULAR) {
                launch {
                    _rotation.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(
                            durationMillis = duration.toInt(),
                            easing = LinearEasing
                        )
                    )
                }
            }
        }
        
        setPlaying(false)
    }
    
    override fun stop() {
        super.stop()
        kotlinx.coroutines.runBlocking {
            _scale.stop()
            _rotation.stop()
        }
    }
}

/**
 * 特效激活动画
 * 
 * 实现特效激活时的闪烁和发光效果
 */
class EffectActivationAnimation(
    private val effectType: FoodType,
    override val duration: Long = 500L
) : GameAnimation() {
    
    override val type = AnimationType.EFFECT_ACTIVATION
    
    // 透明度动画值
    private val _alpha = Animatable(1f)
    val alpha: State<Float> = _alpha.asState()
    
    // 发光强度动画值
    private val _glowIntensity = Animatable(0f)
    val glowIntensity: State<Float> = _glowIntensity.asState()
    
    override suspend fun start() {
        setPlaying(true)
        
        kotlinx.coroutines.coroutineScope {
            // 闪烁效果
            launch {
                repeat(3) {
                    _alpha.animateTo(0.3f, tween(100))
                    _alpha.animateTo(1f, tween(100))
                }
            }
            
            // 发光效果
            launch {
                _glowIntensity.animateTo(1f, tween(200))
                _glowIntensity.animateTo(0f, tween(300))
            }
        }
        
        setPlaying(false)
    }
    
    override fun stop() {
        super.stop()
        kotlinx.coroutines.runBlocking {
            _alpha.stop()
            _glowIntensity.stop()
        }
    }
}

/**
 * 分数增加动画
 * 
 * 实现分数增加时的弹跳和上升效果
 */
class ScoreIncreaseAnimation(
    private val points: Int,
    private val position: Position,
    override val duration: Long = 800L
) : GameAnimation() {
    
    override val type = AnimationType.SCORE_INCREASE
    
    // 垂直位移动画值
    private val _offsetY = Animatable(0f)
    val offsetY: State<Float> = _offsetY.asState()
    
    // 透明度动画值
    private val _alpha = Animatable(1f)
    val alpha: State<Float> = _alpha.asState()
    
    // 缩放动画值
    private val _scale = Animatable(1f)
    val scale: State<Float> = _scale.asState()
    
    override suspend fun start() {
        setPlaying(true)
        
        kotlinx.coroutines.coroutineScope {
            // 上升动画
            launch {
                _offsetY.animateTo(
                    targetValue = -50f,
                    animationSpec = tween(
                        durationMillis = duration.toInt(),
                        easing = FastOutSlowInEasing
                    )
                )
            }
            
            // 淡出动画
            launch {
                kotlinx.coroutines.delay(200) // 延迟开始淡出
                _alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = (duration - 200).toInt(),
                        easing = LinearEasing
                    )
                )
            }
            
            // 弹跳缩放动画
            launch {
                _scale.animateTo(1.2f, tween(150, easing = FastOutSlowInEasing))
                _scale.animateTo(1f, tween(150, easing = FastOutSlowInEasing))
            }
        }
        
        setPlaying(false)
    }
    
    override fun stop() {
        super.stop()
        kotlinx.coroutines.runBlocking {
            _offsetY.stop()
            _alpha.stop()
            _scale.stop()
        }
    }
}

/**
 * 游戏结束动画
 * 
 * 实现游戏结束时的震动和淡出效果
 */
class GameOverAnimation(
    override val duration: Long = 1000L
) : GameAnimation() {
    
    override val type = AnimationType.GAME_OVER
    
    // 震动偏移动画值
    private val _shakeOffset = Animatable(0f)
    val shakeOffset: State<Float> = _shakeOffset.asState()
    
    // 整体透明度动画值
    private val _alpha = Animatable(1f)
    val alpha: State<Float> = _alpha.asState()
    
    override suspend fun start() {
        setPlaying(true)
        
        kotlinx.coroutines.coroutineScope {
            // 震动效果
            launch {
                repeat(5) {
                    _shakeOffset.animateTo(5f, tween(50))
                    _shakeOffset.animateTo(-5f, tween(50))
                }
                _shakeOffset.animateTo(0f, tween(50))
            }
            
            // 淡出效果
            launch {
                kotlinx.coroutines.delay(300) // 延迟开始淡出
                _alpha.animateTo(
                    targetValue = 0.7f,
                    animationSpec = tween(
                        durationMillis = (duration - 300).toInt(),
                        easing = LinearEasing
                    )
                )
            }
        }
        
        setPlaying(false)
    }
    
    override fun stop() {
        super.stop()
        kotlinx.coroutines.runBlocking {
            _shakeOffset.stop()
            _alpha.stop()
        }
    }
}

/**
 * 动画类型枚举
 */
enum class AnimationType {
    SNAKE_MOVE,         // 蛇移动动画
    FOOD_SPAWN,         // 食物出现动画
    FOOD_CONSUME,       // 食物消失动画
    EFFECT_ACTIVATION,  // 特效激活动画
    SCORE_INCREASE,     // 分数增加动画
    GAME_OVER,          // 游戏结束动画
    UI_TRANSITION       // UI转场动画
}

/**
 * 动画配置类
 * 
 * 定义动画的全局配置参数
 */
data class AnimationConfig(
    val enabled: Boolean = true,                    // 是否启用动画
    val speedMultiplier: Float = 1f,               // 动画速度倍数
    val enableParticleEffects: Boolean = true,     // 是否启用粒子效果
    val enableScreenShake: Boolean = true,         // 是否启用屏幕震动
    val maxConcurrentAnimations: Int = 10          // 最大并发动画数量
) {
    
    /**
     * 计算调整后的动画时长
     * 
     * @param baseDuration 基础时长
     * @return 调整后的时长
     */
    fun adjustDuration(baseDuration: Long): Long {
        return if (enabled) {
            (baseDuration / speedMultiplier).toLong()
        } else {
            0L // 禁用动画时返回0，表示立即完成
        }
    }
}