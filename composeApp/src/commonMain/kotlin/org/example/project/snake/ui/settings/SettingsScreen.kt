package org.example.project.snake.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.snake.config.GameConfig
import org.example.project.snake.config.GameMode
import org.example.project.snake.config.Difficulty
import org.example.project.snake.theme.GameTheme
import org.example.project.snake.theme.ThemeManager
import org.example.project.snake.theme.currentGameColorScheme

/**
 * 设置界面
 * 
 * 提供完整的游戏设置功能，包括游戏模式、难度、主题等选项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    gameConfig: GameConfig,
    themeManager: ThemeManager,
    onGameConfigChange: (GameConfig) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = currentGameColorScheme()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = "游戏设置",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.surface,
                titleContentColor = colorScheme.textPrimary
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 设置内容
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 游戏模式设置
            item {
                GameModeSelector(
                    currentMode = gameConfig.gameMode,
                    onModeChange = { newMode ->
                        onGameConfigChange(gameConfig.copy(gameMode = newMode))
                    }
                )
            }
            
            // 难度设置
            item {
                DifficultySelector(
                    currentDifficulty = gameConfig.difficulty,
                    onDifficultyChange = { newDifficulty ->
                        onGameConfigChange(gameConfig.copy(difficulty = newDifficulty))
                    }
                )
            }
            
            // 主题设置
            item {
                ThemeSelector(
                    themeManager = themeManager
                )
            }
            
            // 游戏功能设置
            item {
                GameFeatureSettings(
                    gameConfig = gameConfig,
                    onConfigChange = onGameConfigChange
                )
            }
            
            // 显示设置
            item {
                DisplaySettings(
                    gameConfig = gameConfig,
                    onConfigChange = onGameConfigChange
                )
            }
            
            // 音效设置
            item {
                AudioSettings(
                    gameConfig = gameConfig,
                    onConfigChange = onGameConfigChange
                )
            }
            
            // 重置设置按钮
            item {
                ResetSettingsButton(
                    onReset = {
                        onGameConfigChange(GameConfig())
                        themeManager.setThemeById("classic")
                    }
                )
            }
        }
    }
}

/**
 * 游戏模式选择器
 */
@Composable
fun GameModeSelector(
    currentMode: GameMode,
    onModeChange: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = currentGameColorScheme()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "游戏模式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            GameMode.values().forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentMode == mode,
                        onClick = { onModeChange(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorScheme.buttonPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = mode.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.textPrimary
                        )
                        Text(
                            text = mode.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 难度选择器
 */
@Composable
fun DifficultySelector(
    currentDifficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = currentGameColorScheme()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "游戏难度",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Difficulty.values().forEach { difficulty ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentDifficulty == difficulty,
                        onClick = { onDifficultyChange(difficulty) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorScheme.buttonPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = difficulty.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.textPrimary
                        )
                        Text(
                            text = difficulty.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 主题选择器
 */
@Composable
fun ThemeSelector(
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val colorScheme = currentGameColorScheme()
    val currentTheme by themeManager.currentTheme
    val availableThemes = themeManager.getAvailableThemes()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "主题设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            availableThemes.forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentTheme.id == theme.id,
                        onClick = { themeManager.setTheme(theme) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorScheme.buttonPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = theme.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.textPrimary
                        )
                        Text(
                            text = theme.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 游戏功能设置
 */
@Composable
fun GameFeatureSettings(
    gameConfig: GameConfig,
    onConfigChange: (GameConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = currentGameColorScheme()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "游戏功能",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 特效开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "特效系统",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.textPrimary
                    )
                    Text(
                        text = "启用食物特效和视觉效果",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.textSecondary
                    )
                }
                
                Switch(
                    checked = gameConfig.enableEffects,
                    onCheckedChange = { enabled ->
                        onConfigChange(gameConfig.copy(enableEffects = enabled))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.buttonPrimary
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 网格显示开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "显示网格",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.textPrimary
                    )
                    Text(
                        text = "在游戏区域显示网格线",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.textSecondary
                    )
                }
                
                Switch(
                    checked = gameConfig.showGrid,
                    onCheckedChange = { enabled ->
                        onConfigChange(gameConfig.copy(showGrid = enabled))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.buttonPrimary
                    )
                )
            }
        }
    }
}

/**
 * 显示设置
 */
@Composable
fun DisplaySettings(
    gameConfig: GameConfig,
    onConfigChange: (GameConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = currentGameColorScheme()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "显示设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 动画开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "动画效果",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.textPrimary
                    )
                    Text(
                        text = "启用游戏动画和过渡效果",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.textSecondary
                    )
                }
                
                Switch(
                    checked = gameConfig.enableAnimations,
                    onCheckedChange = { enabled ->
                        onConfigChange(gameConfig.copy(enableAnimations = enabled))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.buttonPrimary
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 粒子效果开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "粒子效果",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.textPrimary
                    )
                    Text(
                        text = "启用粒子特效和发光效果",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.textSecondary
                    )
                }
                
                Switch(
                    checked = gameConfig.enableParticles,
                    onCheckedChange = { enabled ->
                        onConfigChange(gameConfig.copy(enableParticles = enabled))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.buttonPrimary
                    )
                )
            }
        }
    }
}

/**
 * 音效设置
 */
@Composable
fun AudioSettings(
    gameConfig: GameConfig,
    onConfigChange: (GameConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = currentGameColorScheme()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "音效设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 音效开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "游戏音效",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.textPrimary
                    )
                    Text(
                        text = "启用游戏音效和提示音",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.textSecondary
                    )
                }
                
                Switch(
                    checked = gameConfig.enableSound,
                    onCheckedChange = { enabled ->
                        onConfigChange(gameConfig.copy(enableSound = enabled))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.buttonPrimary
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 震动开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "震动反馈",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.textPrimary
                    )
                    Text(
                        text = "启用触觉反馈和震动",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.textSecondary
                    )
                }
                
                Switch(
                    checked = gameConfig.enableVibration,
                    onCheckedChange = { enabled ->
                        onConfigChange(gameConfig.copy(enableVibration = enabled))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.buttonPrimary
                    )
                )
            }
        }
    }
}

/**
 * 重置设置按钮
 */
@Composable
fun ResetSettingsButton(
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = currentGameColorScheme()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "重置设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "将所有设置恢复为默认值",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.textSecondary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.buttonSecondary,
                    contentColor = colorScheme.textPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("重置为默认")
            }
        }
    }
}