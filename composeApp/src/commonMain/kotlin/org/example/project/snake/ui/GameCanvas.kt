package org.example.project.snake.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.example.project.snake.engine.SnakeGameEngine
import org.example.project.snake.model.*
import org.example.project.snake.model.FoodType

/**
 * 游戏画布组件
 * 
 * 使用Canvas绘制游戏区域，包括网格、蛇身、食物等元素
 * 
 * @param gameData 游戏数据
 * @param modifier Compose修饰符
 */
@Composable
fun GameCanvas(
    gameData: SnakeGameEngine.GameData,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 保持正方形比例
    ) {
        // 计算每个网格单元的大小
        val cellSize = minOf(size.width, size.height) / maxOf(20, 20) // 假设20x20网格
        val gameWidth = 20
        val gameHeight = 20
        
        // 计算游戏区域的偏移量（居中显示）
        val offsetX = (size.width - cellSize * gameWidth) / 2
        val offsetY = (size.height - cellSize * gameHeight) / 2
        
        // 绘制背景
        drawBackground(cellSize, gameWidth, gameHeight, offsetX, offsetY)
        
        // 绘制网格线
        drawGrid(cellSize, gameWidth, gameHeight, offsetX, offsetY)
        
        // 绘制食物
        drawFood(gameData.food, cellSize, offsetX, offsetY)
        
        // 绘制蛇
        drawSnake(gameData.snake, cellSize, offsetX, offsetY)
        
        // 如果游戏结束，绘制游戏结束覆盖层
        if (gameData.gameState is GameState.GameOver) {
            drawGameOverOverlay()
        }
        
        // 如果游戏暂停，绘制暂停覆盖层
        if (gameData.gameState is GameState.Paused) {
            drawPausedOverlay()
        }
    }
}

/**
 * 绘制背景
 * 
 * @param cellSize 单元格大小
 * @param gameWidth 游戏宽度
 * @param gameHeight 游戏高度
 * @param offsetX X轴偏移
 * @param offsetY Y轴偏移
 */
private fun DrawScope.drawBackground(
    cellSize: Float,
    gameWidth: Int,
    gameHeight: Int,
    offsetX: Float,
    offsetY: Float
) {
    // 绘制游戏区域背景
    drawRect(
        color = Color(0xFF2E7D32), // 深绿色背景
        topLeft = Offset(offsetX, offsetY),
        size = Size(cellSize * gameWidth, cellSize * gameHeight)
    )
}

/**
 * 绘制网格线
 * 
 * @param cellSize 单元格大小
 * @param gameWidth 游戏宽度
 * @param gameHeight 游戏高度
 * @param offsetX X轴偏移
 * @param offsetY Y轴偏移
 */
private fun DrawScope.drawGrid(
    cellSize: Float,
    gameWidth: Int,
    gameHeight: Int,
    offsetX: Float,
    offsetY: Float
) {
    val gridColor = Color(0xFF4CAF50) // 浅绿色网格线
    val strokeWidth = 1f
    
    // 绘制垂直线
    for (i in 0..gameWidth) {
        val x = offsetX + i * cellSize
        drawLine(
            color = gridColor,
            start = Offset(x, offsetY),
            end = Offset(x, offsetY + gameHeight * cellSize),
            strokeWidth = strokeWidth
        )
    }
    
    // 绘制水平线
    for (i in 0..gameHeight) {
        val y = offsetY + i * cellSize
        drawLine(
            color = gridColor,
            start = Offset(offsetX, y),
            end = Offset(offsetX + gameWidth * cellSize, y),
            strokeWidth = strokeWidth
        )
    }
}

/**
 * 绘制蛇
 * 
 * @param snake 蛇对象
 * @param cellSize 单元格大小
 * @param offsetX X轴偏移
 * @param offsetY Y轴偏移
 */
private fun DrawScope.drawSnake(
    snake: Snake,
    cellSize: Float,
    offsetX: Float,
    offsetY: Float
) {
    snake.body.forEachIndexed { index, position ->
        val x = offsetX + position.x * cellSize
        val y = offsetY + position.y * cellSize
        
        // 蛇头使用不同颜色
        val color = if (index == 0) {
            Color(0xFF1B5E20) // 深绿色蛇头
        } else {
            Color(0xFF388E3C) // 中绿色蛇身
        }
        
        // 绘制蛇身段落（留一点边距）
        val margin = cellSize * 0.1f
        drawRect(
            color = color,
            topLeft = Offset(x + margin, y + margin),
            size = Size(cellSize - 2 * margin, cellSize - 2 * margin)
        )
        
        // 为蛇头添加眼睛
        if (index == 0) {
            drawSnakeEyes(snake.direction, x, y, cellSize)
        }
    }
}

/**
 * 绘制蛇的眼睛
 * 
 * @param direction 蛇的方向
 * @param x 蛇头X坐标
 * @param y 蛇头Y坐标
 * @param cellSize 单元格大小
 */
private fun DrawScope.drawSnakeEyes(
    direction: Direction,
    x: Float,
    y: Float,
    cellSize: Float
) {
    val eyeSize = cellSize * 0.15f
    val eyeColor = Color.White
    
    // 根据方向确定眼睛位置
    val (eye1Offset, eye2Offset) = when (direction) {
        Direction.UP -> Pair(
            Offset(x + cellSize * 0.3f, y + cellSize * 0.2f),
            Offset(x + cellSize * 0.7f, y + cellSize * 0.2f)
        )
        Direction.DOWN -> Pair(
            Offset(x + cellSize * 0.3f, y + cellSize * 0.8f),
            Offset(x + cellSize * 0.7f, y + cellSize * 0.8f)
        )
        Direction.LEFT -> Pair(
            Offset(x + cellSize * 0.2f, y + cellSize * 0.3f),
            Offset(x + cellSize * 0.2f, y + cellSize * 0.7f)
        )
        Direction.RIGHT -> Pair(
            Offset(x + cellSize * 0.8f, y + cellSize * 0.3f),
            Offset(x + cellSize * 0.8f, y + cellSize * 0.7f)
        )
    }
    
    // 绘制眼睛
    drawCircle(
        color = eyeColor,
        radius = eyeSize,
        center = eye1Offset
    )
    drawCircle(
        color = eyeColor,
        radius = eyeSize,
        center = eye2Offset
    )
}

/**
 * 绘制食物
 * 
 * @param food 食物对象
 * @param cellSize 单元格大小
 * @param offsetX X轴偏移
 * @param offsetY Y轴偏移
 */
private fun DrawScope.drawFood(
    food: Food,
    cellSize: Float,
    offsetX: Float,
    offsetY: Float
) {
    val x = offsetX + food.position.x * cellSize
    val y = offsetY + food.position.y * cellSize
    
    // 根据食物类型选择颜色
    val color = when (food.type) {
        FoodType.REGULAR -> Color.Red
        FoodType.BONUS -> Color(0xFFFFD700) // 金色
        FoodType.SPEED_UP -> Color.Blue
        FoodType.SPEED_DOWN -> Color(0xFF800080) // 紫色
        FoodType.GHOST -> Color.Gray
        FoodType.SHRINK -> Color(0xFFFF4500) // 橙红色
        else -> Color.Red // 默认为红色
    }
    
    // 绘制食物（圆形）
    val radius = cellSize * 0.4f
    val center = Offset(x + cellSize / 2, y + cellSize / 2)
    
    drawCircle(
        color = color,
        radius = radius,
        center = center
    )
    
    // 为特殊食物添加光晕效果
    if (food.type != FoodType.REGULAR) {
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius * 1.5f,
            center = center
        )
    }
}

/**
 * 绘制游戏结束覆盖层
 */
private fun DrawScope.drawGameOverOverlay() {
    // 半透明黑色覆盖层
    drawRect(
        color = Color.Black.copy(alpha = 0.7f),
        size = size
    )
}

/**
 * 绘制暂停覆盖层
 */
private fun DrawScope.drawPausedOverlay() {
    // 半透明灰色覆盖层
    drawRect(
        color = Color.Gray.copy(alpha = 0.5f),
        size = size
    )
}
