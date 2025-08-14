package org.example.project.snake.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.snake.engine.SnakeGameEngine
import org.example.project.snake.model.*

/**
 * 游戏控制组件
 * 
 * 包含分数显示、游戏状态显示、控制按钮等UI元素
 * 
 * @param gameData 游戏数据
 * @param onDirectionChange 方向改变回调
 * @param onPauseToggle 暂停切换回调
 * @param onReset 重置游戏回调
 * @param modifier Compose修饰符
 */
@Composable
fun GameControls(
    gameData: SnakeGameEngine.GameData,
    onDirectionChange: (Direction) -> Unit,
    onPauseToggle: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 游戏状态和控制按钮
        GameActionButtons(
            gameState = gameData.gameState,
            onPauseToggle = onPauseToggle,
            onReset = onReset
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 方向控制按钮
        DirectionControls(
            onDirectionChange = onDirectionChange,
            enabled = gameData.gameState is GameState.Playing
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 游戏信息显示区域
        GameInfoPanel(gameData = gameData)

    }
}

/**
 * 游戏信息面板
 * 
 * 显示分数、等级、游戏状态等信息
 * 
 * @param gameData 游戏数据
 */
@Composable
private fun GameInfoPanel(gameData: SnakeGameEngine.GameData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 游戏标题
            Text(
                text = "贪吃蛇游戏",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 分数和等级信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 分数显示
                InfoItem(
                    label = "分数",
                    value = gameData.gameState.getCurrentScore().toString()
                )
                
                // 等级显示
                val level = when (val state = gameData.gameState) {
                    is GameState.Playing -> state.level
                    is GameState.Paused -> state.previousState.level
                    is GameState.GameOver -> state.finalLevel
                }
                InfoItem(
                    label = "等级",
                    value = level.toString()
                )
                
                // 蛇长度显示
                InfoItem(
                    label = "长度",
                    value = gameData.snake.length.toString()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 游戏状态显示
            GameStatusText(gameState = gameData.gameState)
        }
    }
}

/**
 * 信息项组件
 * 
 * @param label 标签
 * @param value 值
 */
@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 游戏状态文本
 * 
 * @param gameState 游戏状态
 */
@Composable
private fun GameStatusText(gameState: GameState) {
    val (text, color) = when (gameState) {
        is GameState.Playing -> "游戏进行中" to Color(0xFF4CAF50)
        is GameState.Paused -> "游戏已暂停" to Color(0xFFFF9800)
        is GameState.GameOver -> {
            val reason = when (gameState.reason) {
                GameState.GameOverReason.WALL_COLLISION -> "撞墙了！"
                GameState.GameOverReason.SELF_COLLISION -> "撞到自己了！"
                GameState.GameOverReason.TIME_UP -> "时间到了！"
            }
            "游戏结束 - $reason" to Color(0xFFF44336)
        }
    }
    
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        textAlign = TextAlign.Center
    )
}

/**
 * 游戏操作按钮
 * 
 * @param gameState 游戏状态
 * @param onPauseToggle 暂停切换回调
 * @param onReset 重置回调
 */
@Composable
private fun GameActionButtons(
    gameState: GameState,
    onPauseToggle: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 暂停/恢复/重新开始按钮
        val (buttonText, buttonColor) = when (gameState) {
            is GameState.Playing -> "暂停" to MaterialTheme.colorScheme.primary
            is GameState.Paused -> "继续" to Color(0xFF4CAF50)
            is GameState.GameOver -> "重新开始" to Color(0xFF2196F3)
        }
        
        Button(
            onClick = onPauseToggle,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text(buttonText)
        }
        
        // 重置按钮（只在游戏进行中或暂停时显示）
        if (gameState !is GameState.GameOver) {
            OutlinedButton(onClick = onReset) {
                Text("重置")
            }
        }
    }
}

/**
 * 方向控制按钮
 * 
 * @param onDirectionChange 方向改变回调
 * @param enabled 是否启用
 */
@Composable
private fun DirectionControls(
    onDirectionChange: (Direction) -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "方向控制",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // 上方向按钮
        DirectionButton(
            text = "↑",
            onClick = { onDirectionChange(Direction.UP) },
            enabled = enabled
        )
        
        // 左右方向按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            DirectionButton(
                text = "←",
                onClick = { onDirectionChange(Direction.LEFT) },
                enabled = enabled
            )
            DirectionButton(
                text = "→",
                onClick = { onDirectionChange(Direction.RIGHT) },
                enabled = enabled
            )
        }
        
        // 下方向按钮
        DirectionButton(
            text = "↓",
            onClick = { onDirectionChange(Direction.DOWN) },
            enabled = enabled
        )
    }
}

/**
 * 方向按钮组件
 * 
 * @param text 按钮文本
 * @param onClick 点击回调
 * @param enabled 是否启用
 */
@Composable
private fun DirectionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = Modifier.size(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
