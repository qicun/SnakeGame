package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.snake.di.ViewModelFactory
import org.example.project.snake.storage.PlatformStorage

fun MainViewController() = ComposeUIViewController { 
    // 初始化ViewModelFactory
    val platformStorage = PlatformStorage()
    ViewModelFactory.initialize(platformStorage)
    
    App() 
}
