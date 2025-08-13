package org.example.project.snake.di

import org.example.project.snake.engine.SnakeViewModel
import org.example.project.snake.data.GameDataRepository
import org.example.project.snake.data.GameDataRepositoryImpl
import org.example.project.snake.storage.PlatformStorage

/**
 * ViewModel工厂
 * 
 * 负责创建和管理ViewModel实例及其依赖
 */
object ViewModelFactory {
    
    private var dataRepository: GameDataRepository? = null
    private var snakeViewModel: SnakeViewModel? = null
    
    /**
     * 初始化工厂
     */
    fun initialize(platformStorage: PlatformStorage) {
        dataRepository = GameDataRepositoryImpl(platformStorage)
    }
    
    /**
     * 获取SnakeViewModel实例
     */
    fun getSnakeViewModel(): SnakeViewModel {
        val repository = dataRepository ?: throw IllegalStateException("ViewModelFactory not initialized")
        
        if (snakeViewModel == null) {
            snakeViewModel = SnakeViewModel(repository)
        }
        
        return snakeViewModel!!
    }
    
    /**
     * 获取数据仓库实例
     */
    fun getDataRepository(): GameDataRepository {
        return dataRepository ?: throw IllegalStateException("ViewModelFactory not initialized")
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        snakeViewModel = null
        dataRepository = null
    }
}