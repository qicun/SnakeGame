package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.example.project.snake.ui.SnakeGameScreen

/**
 * 应用程序主入口
 *
 * 集成贪吃蛇游戏作为主界面
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        // 直接显示贪吃蛇游戏界面
        SnakeGameScreen()
    }
}