package org.example.project.snake.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import org.example.project.snake.model.FoodType

/**
 * 游戏主题系统
 * 
 * 提供完整的主题管理功能，包括颜色方案、动画配置等
 */

/**
 * 游戏主题接口
 * 
 * 定义了一个完整游戏主题应该包含的所有元素
 */
interface GameTheme {
    val id: String
    val name: String
    val description: String
    val colorScheme: GameColorScheme
    val materialColorScheme: ColorScheme
    val isDark: Boolean
}

/**
 * 游戏颜色方案
 * 
 * 定义游戏中所有UI元素的颜色
 */
data class GameColorScheme(
    // 背景颜色
    val background: Color,
    val surface: Color,
    val gameArea: Color,
    
    // 蛇的颜色
    val snakeHead: Color,
    val snakeBody: Color,
    val snakeBodySecondary: Color, // 用于创建渐变效果
    
    // 食物颜色映射
    val foodColors: Map<FoodType, Color>,
    
    // 特效颜色
    val effectGlow: Color,
    val effectParticle: Color,
    
    // UI元素颜色
    val scoreText: Color,
    val levelText: Color,
    val buttonPrimary: Color,
    val buttonSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    
    // 游戏状态颜色
    val pausedOverlay: Color,
    val gameOverOverlay: Color,
    
    // 网格和边框
    val gridLine: Color,
    val border: Color
)

/**
 * 经典主题
 * 
 * 传统的绿色蛇和红色食物主题
 */
class ClassicTheme : GameTheme {
    override val id = "classic"
    override val name = "经典主题"
    override val description = "传统的绿色蛇和红色食物，怀旧经典"
    override val isDark = false
    
    override val colorScheme = GameColorScheme(
        background = Color(0xFFF5F5DC), // 米色背景
        surface = Color(0xFFFFFFFF),
        gameArea = Color(0xFF90EE90), // 浅绿色游戏区域
        
        snakeHead = Color(0xFF228B22), // 深绿色蛇头
        snakeBody = Color(0xFF32CD32), // 绿色蛇身
        snakeBodySecondary = Color(0xFF90EE90), // 浅绿色蛇身渐变
        
        foodColors = mapOf(
            FoodType.REGULAR to Color(0xFFFF0000),      // 红色普通食物
            FoodType.BONUS to Color(0xFFFFD700),        // 金色奖励食物
            FoodType.SPEED_UP to Color(0xFF0000FF),     // 蓝色加速食物
            FoodType.SPEED_DOWN to Color(0xFF800080),   // 紫色减速食物
            FoodType.GHOST to Color(0xFFC0C0C0),        // 银色幽灵食物
            FoodType.SHRINK to Color(0xFFFFA500)        // 橙色缩小食物
        ),
        
        effectGlow = Color(0xFFFFFF00), // 黄色发光效果
        effectParticle = Color(0xFFFFFFFF), // 白色粒子
        
        scoreText = Color(0xFF000000),
        levelText = Color(0xFF000000),
        buttonPrimary = Color(0xFF228B22),
        buttonSecondary = Color(0xFF90EE90),
        textPrimary = Color(0xFF000000),
        textSecondary = Color(0xFF666666),
        
        pausedOverlay = Color(0x80000000), // 半透明黑色
        gameOverOverlay = Color(0x80FF0000), // 半透明红色
        
        gridLine = Color(0x40000000), // 淡黑色网格线
        border = Color(0xFF228B22) // 绿色边框
    )
    
    override val materialColorScheme = lightColorScheme(
        primary = Color(0xFF228B22),
        secondary = Color(0xFF32CD32),
        background = Color(0xFFF5F5DC),
        surface = Color(0xFFFFFFFF)
    )
}

/**
 * 深色主题
 * 
 * 适合夜间游戏的深色主题
 */
class DarkTheme : GameTheme {
    override val id = "dark"
    override val name = "深色主题"
    override val description = "护眼的深色主题，适合夜间游戏"
    override val isDark = true
    
    override val colorScheme = GameColorScheme(
        background = Color(0xFF121212), // 深灰色背景
        surface = Color(0xFF1E1E1E),
        gameArea = Color(0xFF2D2D2D), // 深灰色游戏区域
        
        snakeHead = Color(0xFF4CAF50), // 亮绿色蛇头
        snakeBody = Color(0xFF66BB6A), // 绿色蛇身
        snakeBodySecondary = Color(0xFF81C784), // 浅绿色蛇身渐变
        
        foodColors = mapOf(
            FoodType.REGULAR to Color(0xFFFF5722),      // 橙红色普通食物
            FoodType.BONUS to Color(0xFFFFC107),        // 琥珀色奖励食物
            FoodType.SPEED_UP to Color(0xFF2196F3),     // 蓝色加速食物
            FoodType.SPEED_DOWN to Color(0xFF9C27B0),   // 紫色减速食物
            FoodType.GHOST to Color(0xFF9E9E9E),        // 灰色幽灵食物
            FoodType.SHRINK to Color(0xFFFF9800)        // 橙色缩小食物
        ),
        
        effectGlow = Color(0xFF00E676), // 绿色发光效果
        effectParticle = Color(0xFFE0E0E0), // 浅灰色粒子
        
        scoreText = Color(0xFFFFFFFF),
        levelText = Color(0xFFFFFFFF),
        buttonPrimary = Color(0xFF4CAF50),
        buttonSecondary = Color(0xFF66BB6A),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFB0B0B0),
        
        pausedOverlay = Color(0x80000000), // 半透明黑色
        gameOverOverlay = Color(0x80D32F2F), // 半透明深红色
        
        gridLine = Color(0x40FFFFFF), // 淡白色网格线
        border = Color(0xFF4CAF50) // 绿色边框
    )
    
    override val materialColorScheme = darkColorScheme(
        primary = Color(0xFF4CAF50),
        secondary = Color(0xFF66BB6A),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E)
    )
}

/**
 * 霓虹主题
 * 
 * 炫酷的霓虹发光效果主题
 */
class NeonTheme : GameTheme {
    override val id = "neon"
    override val name = "霓虹主题"
    override val description = "炫酷的霓虹发光效果，科技感十足"
    override val isDark = true
    
    override val colorScheme = GameColorScheme(
        background = Color(0xFF0A0A0A), // 纯黑背景
        surface = Color(0xFF1A1A1A),
        gameArea = Color(0xFF000000), // 纯黑游戏区域
        
        snakeHead = Color(0xFF00FFFF), // 青色霓虹蛇头
        snakeBody = Color(0xFF00E5FF), // 亮青色蛇身
        snakeBodySecondary = Color(0xFF40C4FF), // 浅青色蛇身渐变
        
        foodColors = mapOf(
            FoodType.REGULAR to Color(0xFFFF0080),      // 洋红色普通食物
            FoodType.BONUS to Color(0xFFFFFF00),        // 黄色奖励食物
            FoodType.SPEED_UP to Color(0xFF0080FF),     // 蓝色加速食物
            FoodType.SPEED_DOWN to Color(0xFF8000FF),   // 紫色减速食物
            FoodType.GHOST to Color(0xFFFFFFFF),        // 白色幽灵食物
            FoodType.SHRINK to Color(0xFFFF8000)        // 橙色缩小食物
        ),
        
        effectGlow = Color(0xFF00FFFF), // 青色发光效果
        effectParticle = Color(0xFFFFFFFF), // 白色粒子
        
        scoreText = Color(0xFF00FFFF),
        levelText = Color(0xFF00FFFF),
        buttonPrimary = Color(0xFF00FFFF),
        buttonSecondary = Color(0xFF40C4FF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF80DEEA),
        
        pausedOverlay = Color(0x80000000), // 半透明黑色
        gameOverOverlay = Color(0x80FF0080), // 半透明洋红色
        
        gridLine = Color(0x4000FFFF), // 淡青色网格线
        border = Color(0xFF00FFFF) // 青色边框
    )
    
    override val materialColorScheme = darkColorScheme(
        primary = Color(0xFF00FFFF),
        secondary = Color(0xFF40C4FF),
        background = Color(0xFF0A0A0A),
        surface = Color(0xFF1A1A1A)
    )
}

/**
 * 简约主题
 * 
 * 黑白极简风格主题
 */
class MinimalTheme : GameTheme {
    override val id = "minimal"
    override val name = "简约主题"
    override val description = "黑白极简风格，专注游戏本身"
    override val isDark = false
    
    override val colorScheme = GameColorScheme(
        background = Color(0xFFFFFFFF), // 纯白背景
        surface = Color(0xFFF8F8F8),
        gameArea = Color(0xFFFAFAFA), // 浅灰色游戏区域
        
        snakeHead = Color(0xFF000000), // 黑色蛇头
        snakeBody = Color(0xFF333333), // 深灰色蛇身
        snakeBodySecondary = Color(0xFF666666), // 灰色蛇身渐变
        
        foodColors = mapOf(
            FoodType.REGULAR to Color(0xFF000000),      // 黑色普通食物
            FoodType.BONUS to Color(0xFF666666),        // 灰色奖励食物
            FoodType.SPEED_UP to Color(0xFF333333),     // 深灰色加速食物
            FoodType.SPEED_DOWN to Color(0xFF999999),   // 浅灰色减速食物
            FoodType.GHOST to Color(0xFFCCCCCC),        // 浅灰色幽灵食物
            FoodType.SHRINK to Color(0xFF555555)        // 中灰色缩小食物
        ),
        
        effectGlow = Color(0xFF000000), // 黑色发光效果
        effectParticle = Color(0xFF666666), // 灰色粒子
        
        scoreText = Color(0xFF000000),
        levelText = Color(0xFF000000),
        buttonPrimary = Color(0xFF000000),
        buttonSecondary = Color(0xFF666666),
        textPrimary = Color(0xFF000000),
        textSecondary = Color(0xFF666666),
        
        pausedOverlay = Color(0x80FFFFFF), // 半透明白色
        gameOverOverlay = Color(0x80000000), // 半透明黑色
        
        gridLine = Color(0x20000000), // 淡黑色网格线
        border = Color(0xFF000000) // 黑色边框
    )
    
    override val materialColorScheme = lightColorScheme(
        primary = Color(0xFF000000),
        secondary = Color(0xFF666666),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF8F8F8)
    )
}

/**
 * 主题管理器
 * 
 * 管理所有可用主题和当前主题状态
 */
class ThemeManager {
    
    // 所有可用主题
    private val availableThemes = listOf(
        ClassicTheme(),
        DarkTheme(),
        NeonTheme(),
        MinimalTheme()
    )
    
    // 当前主题状态
    private val _currentTheme = mutableStateOf<GameTheme>(availableThemes.first())
    val currentTheme: State<GameTheme> = _currentTheme
    
    /**
     * 获取所有可用主题
     */
    fun getAvailableThemes(): List<GameTheme> = availableThemes
    
    /**
     * 根据ID获取主题
     */
    fun getThemeById(id: String): GameTheme? {
        return availableThemes.find { it.id == id }
    }
    
    /**
     * 设置当前主题
     */
    fun setTheme(theme: GameTheme) {
        _currentTheme.value = theme
    }
    
    /**
     * 根据ID设置主题
     */
    fun setThemeById(id: String) {
        getThemeById(id)?.let { theme ->
            setTheme(theme)
        }
    }
    
    /**
     * 切换到下一个主题
     */
    fun switchToNextTheme() {
        val currentIndex = availableThemes.indexOf(_currentTheme.value)
        val nextIndex = (currentIndex + 1) % availableThemes.size
        _currentTheme.value = availableThemes[nextIndex]
    }
    
    /**
     * 切换到上一个主题
     */
    fun switchToPreviousTheme() {
        val currentIndex = availableThemes.indexOf(_currentTheme.value)
        val previousIndex = if (currentIndex == 0) availableThemes.size - 1 else currentIndex - 1
        _currentTheme.value = availableThemes[previousIndex]
    }
    
    /**
     * 检查是否为深色主题
     */
    fun isDarkTheme(): Boolean = _currentTheme.value.isDark
    
    /**
     * 获取当前主题的颜色方案
     */
    fun getCurrentColorScheme(): GameColorScheme = _currentTheme.value.colorScheme
    
    /**
     * 获取当前主题的Material颜色方案
     */
    fun getCurrentMaterialColorScheme(): ColorScheme = _currentTheme.value.materialColorScheme
}

/**
 * 主题提供者Composable
 * 
 * 为整个应用提供主题上下文
 */
@Composable
fun GameThemeProvider(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val currentTheme by themeManager.currentTheme
    
    CompositionLocalProvider(
        LocalGameTheme provides currentTheme,
        LocalGameColorScheme provides currentTheme.colorScheme
    ) {
        androidx.compose.material3.MaterialTheme(
            colorScheme = currentTheme.materialColorScheme,
            content = content
        )
    }
}

/**
 * 本地主题提供者
 */
val LocalGameTheme = compositionLocalOf<GameTheme> {
    error("No GameTheme provided")
}

val LocalGameColorScheme = compositionLocalOf<GameColorScheme> {
    error("No GameColorScheme provided")
}

/**
 * 获取当前游戏主题的便捷函数
 */
@Composable
fun currentGameTheme(): GameTheme = LocalGameTheme.current

/**
 * 获取当前游戏颜色方案的便捷函数
 */
@Composable
fun currentGameColorScheme(): GameColorScheme = LocalGameColorScheme.current
