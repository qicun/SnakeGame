package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.example.project.snake.ui.main.MainScreen
import org.example.project.snake.di.ViewModelFactory

/**
 * 应用程序主入口
 *
 * 集成完整的贪吃蛇游戏系统，包括统计、排行榜等功能
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        // 获取ViewModel实例
        val viewModel = ViewModelFactory.getSnakeViewModel()
        
        // 显示主界面，包含完整的功能导航
        MainScreen(viewModel = viewModel)
    }
}
