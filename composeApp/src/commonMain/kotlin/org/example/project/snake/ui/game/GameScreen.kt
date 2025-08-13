package org.example.project.snake.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.snake.engine.SnakeViewModel
import org.example.project.snake.model.Direction
import org.example.project.snake.model.GameState
import org.example.project.snake.model.Position
import kotlin.math.abs

/**
 * 游戏界面
 * 
 * 显示游戏画面、分数、控制按钮等
 */
@Composable
fun GameScreen(
    viewModel: SnakeViewModel,
    modifier: Modifier = Modifier
) {
    val gameData by viewModel.gameData.collectAsState()
    val gameConfig by viewModel.gameConfig.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 游戏信息栏
        GameInfoBar(
            score = gameData.gameState.getCurrentScore(),
            level = viewModel.getCurrentLevel(),
            gameMode = gameConfig.gameMode.displayName,
            difficulty = gameConfig.difficulty.displayName
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 游戏画布
        GameCanvas(
            gameData = gameData,
            onDirectionChange = viewModel::changeDirection,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2E7D32))
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 控制按钮
        GameControls(
            isPlaying = viewModel.isGamePlaying(),
            isPaused = viewModel.isGamePaused(),
            isGameOver = viewModel.isGameOver(),
            onPauseResume = viewModel::togglePause,
            onReset = viewModel::resetGame
        )
        
        // 游戏结束信息
        if (gameData.gameState is GameState.GameOver) {
            Spacer(modifier = Modifier.height(16.dp))
            GameOverCard(
                finalScore = gameData.gameState.finalScore,
                reason = gameData.gameState.reason,
                onRestart = viewModel::resetGame
            )
        }
    }
}

/**
 * 游戏信息栏
 */
@Composable
fun GameInfoBar(
    score: Int,
    level: Int,
    gameMode: String,
    difficulty: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoItem("分数", score.toString())
            InfoItem("等级", level.toString())
            InfoItem("模式", gameMode)
            InfoItem("难度", difficulty)
        }
    }
}

/**
 * 信息项组件
 */
@Composable
fun InfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 游戏画布
 */
@Composable
fun GameCanvas(
    gameData: org.example.project.snake.engine.SnakeGameEngine.GameData,
    onDirectionChange: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val dragAmountPx = with(density) {
                        Offset(dragAmount.x, dragAmount.y)
                    }
                    
                    // 根据拖拽方向确定移动方向
                    val direction = when {
                        abs(dragAmountPx.x) > abs(dragAmountPx.y) -> {
                            if (dragAmountPx.x > 0) Direction.RIGHT else Direction.LEFT
                        }
                        else -> {
                            if (dragAmountPx.y > 0) Direction.DOWN else Direction.UP
                        }
                    }
                    
                    onDirectionChange(direction)
                }
            }
    ) {
        val cellSize = size.width / 20f // 假设20x20的网格
        
        // 绘制蛇身
        gameData.snake.body.forEach { position ->
            drawRect(
                color = Color.Green,
                topLeft = Offset(position.x * cellSize, position.y * cellSize),
                size = Size(cellSize * 0.9f, cellSize * 0.9f)
            )
        }
        
        // 绘制蛇头
        val head = gameData.snake.body.firstOrNull()
        head?.let { position ->
            drawRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(position.x * cellSize, position.y * cellSize),
                size = Size(cellSize * 0.9f, cellSize * 0.9f)
            )
        }
        
        // 绘制食物
        gameData.food?.let { food ->
            drawRect(
                color = when (food.type.name) {
                    "NORMAL" -> Color.Red
                    "BONUS" -> Color.Yellow
                    "SPEED" -> Color.Blue
                    "SLOW" -> Color.Cyan
                    else -> Color.Red
                },
                topLeft = Offset(food.position.x * cellSize, food.position.y * cellSize),
                size = Size(cellSize * 0.8f, cellSize * 0.8f)
            )
        }
    }
}

/**
 * 游戏控制按钮
 */
@Composable
fun GameControls(
    isPlaying: Boolean,
    isPaused: Boolean,
    isGameOver: Boolean,
    onPauseResume: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onPauseResume,
            enabled = !isGameOver
        ) {
            Text(
                when {
                    isPaused -> "继续"
                    isPlaying -> "暂停"
                    else -> "开始"
                }
            )
        }
        
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("重新开始")
        }
    }
}

/**
 * 游戏结束卡片
 */
@Composable
fun GameOverCard(
    finalScore: Int,
    reason: org.example.project.snake.model.GameState.GameOverReason,
    onRestart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "游戏结束",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Text(
                text = "最终分数: $finalScore",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Text(
                text = when (reason) {
                    org.example.project.snake.model.GameState.GameOverReason.WALL_COLLISION -> "撞墙了"
                    org.example.project.snake.model.GameState.GameOverReason.SELF_COLLISION -> "撞到自己了"
                    org.example.project.snake.model.GameState.GameOverReason.TIME_UP -> "时间到了"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("再来一局")
            }
        }
    }
}