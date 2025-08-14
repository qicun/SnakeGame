package org.example.project.snake.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.snake.engine.SnakeViewModel
import org.example.project.snake.ui.game.GameScreen
import org.example.project.snake.ui.settings.SettingsScreen
import org.example.project.snake.ui.statistics.StatisticsScreen
import org.example.project.snake.ui.leaderboard.LeaderboardScreen
import org.example.project.snake.data.GameRecord
import org.example.project.snake.data.LeaderboardEntry
import org.example.project.snake.config.GameMode
import org.example.project.snake.config.Difficulty

/**
 * 主界面导航枚举
 */
enum class MainScreenDestination {
    GAME,
    SETTINGS,
    STATISTICS,
    LEADERBOARD
}

/**
 * 主界面
 * 
 * 管理应用程序的主要导航和界面切换
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SnakeViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(MainScreenDestination.GAME) }
    var gameRecords by remember { mutableStateOf<List<GameRecord>>(emptyList()) }
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var selectedGameMode by remember { mutableStateOf<GameMode?>(null) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }
    
    // 协程作用域
    val coroutineScope = rememberCoroutineScope()
    
    // 收集ViewModel状态
    val gameData by viewModel.gameData.collectAsState()
    val gameConfig by viewModel.gameConfig.collectAsState()
    val playerStatistics by viewModel.playerStatistics.collectAsState()
    
    // 加载数据的副作用
    LaunchedEffect(currentDestination) {
        when (currentDestination) {
            MainScreenDestination.STATISTICS -> {
                gameRecords = viewModel.getGameRecords(20)
            }
            MainScreenDestination.LEADERBOARD -> {
                leaderboard = viewModel.getLeaderboard(50)
            }
            else -> { /* 不需要额外数据 */ }
        }
    }
    
    // 当筛选条件改变时重新加载排行榜
    LaunchedEffect(selectedGameMode, selectedDifficulty) {
        if (currentDestination == MainScreenDestination.LEADERBOARD) {
            leaderboard = viewModel.getLeaderboard(50)
        }
    }
    
    when (currentDestination) {
        MainScreenDestination.GAME -> {
            GameScreenWithNavigation(
                viewModel = viewModel,
                gameData = gameData,
                gameConfig = gameConfig,
                onNavigateToSettings = { currentDestination = MainScreenDestination.SETTINGS },
                onNavigateToStatistics = { currentDestination = MainScreenDestination.STATISTICS },
                onNavigateToLeaderboard = { currentDestination = MainScreenDestination.LEADERBOARD }
            )
        }
        
        MainScreenDestination.SETTINGS -> {
        // 创建ThemeManager实例
        val themeManager = remember { org.example.project.snake.theme.ThemeManager() }
        
        SettingsScreen(
            gameConfig = gameConfig,
            themeManager = themeManager,
            onGameConfigChange = viewModel::updateGameConfig,
            onBackClick = { currentDestination = MainScreenDestination.GAME }
        )
        }
        
        MainScreenDestination.STATISTICS -> {
            StatisticsScreen(
                statistics = playerStatistics,
                gameRecords = gameRecords,
                onBackClick = { currentDestination = MainScreenDestination.GAME },
                onClearData = {
                    // 显示确认对话框后清除数据
                    // 这里简化处理，实际应用中应该有确认对话框
                    kotlinx.coroutines.GlobalScope.launch {
                        viewModel.clearAllData()
                        gameRecords = emptyList()
                    }
                }
            )
        }
        
        MainScreenDestination.LEADERBOARD -> {
            LeaderboardScreen(
                leaderboard = leaderboard,
                selectedGameMode = selectedGameMode,
                selectedDifficulty = selectedDifficulty,
                onGameModeSelected = { selectedGameMode = it },
                onDifficultySelected = { selectedDifficulty = it },
                onBackClick = { currentDestination = MainScreenDestination.GAME }
            )
        }
    }
}

/**
 * 带导航的游戏界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenWithNavigation(
    viewModel: SnakeViewModel,
    gameData: org.example.project.snake.engine.SnakeGameEngine.GameData,
    gameConfig: org.example.project.snake.config.GameConfig,
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("贪吃蛇游戏") },
                actions = {
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(Icons.Default.List, contentDescription = "统计")
                    }
                    IconButton(onClick = onNavigateToLeaderboard) {
                        Icon(Icons.Default.List, contentDescription = "排行榜")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { paddingValues ->
        GameScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues)
        )
    }
}