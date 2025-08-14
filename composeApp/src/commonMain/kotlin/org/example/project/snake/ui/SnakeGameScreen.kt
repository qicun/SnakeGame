package org.example.project.snake.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

import org.example.project.snake.engine.SnakeViewModel
import org.example.project.snake.input.GestureHandler
import org.example.project.snake.model.Direction

/**
 * 贪吃蛇游戏主界面
 * 
 * 组合所有UI组件，包括游戏画布、控制面板、手势处理等
 * 
 * @param modifier Compose修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeGameScreen(
    dataRepository: org.example.project.snake.data.GameDataRepository,
    modifier: Modifier = Modifier
) {
    // 创建ViewModel实例
    val viewModel = remember { SnakeViewModel(dataRepository) }
    
    // 观察游戏数据状态
    val gameData by viewModel.gameData.collectAsState()
    
    // 创建手势处理器
    val gestureHandler = remember { GestureHandler() }
    
    // 获取密度信息用于手势检测
    val density = LocalDensity.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("贪吃蛇游戏") 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 游戏画布区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    GameCanvas(
                        gameData = gameData,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // 游戏控制面板
            GameControls(
                gameData = gameData,
                onDirectionChange = { direction ->
                    gestureHandler.handleDirectionInput(direction) { dir ->
                        viewModel.changeDirection(dir)
                    }
                },
                onPauseToggle = {
                    viewModel.togglePause()
                },
                onReset = {
                    viewModel.resetGame()
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 游戏说明
            GameInstructions()
        }
    }
}



/**
 * 游戏说明组件
 * 
 * 显示游戏规则和操作说明
 */
@Composable
private fun GameInstructions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "游戏说明",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val instructions = listOf(
                "🐍 控制蛇移动吃食物，避免撞墙和撞到自己",
                "🎮 使用方向按钮或滑动手势控制方向",
                "🍎 普通食物(红色) +10分",
                "🍊 奖励食物(橙色) +25分", 
                "⭐ 超级食物(黄色) +50分",
                "📈 每100分升一级，游戏速度会加快",
                "⏸️ 可以随时暂停和恢复游戏"
            )
            
            instructions.forEach { instruction ->
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * 游戏结束对话框
 * 
 * 当游戏结束时显示最终分数和重新开始选项
 * 
 * @param gameData 游戏数据
 * @param onRestart 重新开始回调
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun GameOverDialog(
    gameData: org.example.project.snake.engine.SnakeGameEngine.GameData,
    onRestart: () -> Unit,
    onDismiss: () -> Unit
) {
    if (gameData.gameState is org.example.project.snake.model.GameState.GameOver) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("游戏结束")
            },
            text = {
                Column {
                    val reason = when (gameData.gameState.reason) {
                        org.example.project.snake.model.GameState.GameOverReason.WALL_COLLISION -> "撞墙了！"
                        org.example.project.snake.model.GameState.GameOverReason.SELF_COLLISION -> "撞到自己了！"
                        org.example.project.snake.model.GameState.GameOverReason.TIME_UP -> "时间到了！"
                    }
                    Text("$reason")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("最终分数: ${gameData.gameState.finalScore}")
                    Text("最终等级: ${gameData.gameState.finalLevel}")
                    Text("蛇的长度: ${gameData.snake.length}")
                }
            },
            confirmButton = {
                TextButton(onClick = onRestart) {
                    Text("重新开始")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }
        )
    }
}
