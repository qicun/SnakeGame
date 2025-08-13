package org.example.project.snake.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.snake.data.PlayerStatistics
import org.example.project.snake.data.GameRecord
import org.example.project.snake.config.GameMode
import org.example.project.snake.config.Difficulty

/**
 * 统计界面
 * 
 * 显示玩家的游戏统计信息、历史记录和成就
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    statistics: PlayerStatistics?,
    gameRecords: List<GameRecord>,
    onBackClick: () -> Unit,
    onClearData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("游戏统计") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = onClearData) {
                        Text("清除数据")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 总体统计卡片
            item {
                OverviewCard(statistics)
            }
            
            // 游戏模式统计
            item {
                GameModeStatsCard(statistics)
            }
            
            // 难度统计
            item {
                DifficultyStatsCard(statistics)
            }
            
            // 最近游戏记录
            item {
                Text(
                    text = "最近游戏",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(gameRecords.take(10)) { record ->
                GameRecordCard(record)
            }
        }
    }
}

/**
 * 总体统计卡片
 */
@Composable
fun OverviewCard(statistics: PlayerStatistics?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "总体统计",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (statistics != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem("总游戏数", statistics.totalGames.toString())
                    StatisticItem("最高分", statistics.highestScore.toString())
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem("平均分", "%.1f".format(statistics.averageScore))
                    StatisticItem("胜率", "%.1f%%".format(statistics.winRate * 100))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem("最长蛇", statistics.longestSnake.toString())
                    StatisticItem("总食物", statistics.totalFoodEaten.toString())
                }
                
                StatisticItem(
                    "总游戏时间", 
                    formatPlayTime(statistics.totalPlayTime)
                )
            } else {
                Text("暂无统计数据")
            }
        }
    }
}

/**
 * 游戏模式统计卡片
 */
@Composable
fun GameModeStatsCard(statistics: PlayerStatistics?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "游戏模式统计",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (statistics != null && statistics.gamesByMode.isNotEmpty()) {
                statistics.gamesByMode.forEach { (mode, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(mode.displayName)
                        Text("$count 局")
                    }
                }
            } else {
                Text("暂无数据")
            }
        }
    }
}

/**
 * 难度统计卡片
 */
@Composable
fun DifficultyStatsCard(statistics: PlayerStatistics?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "难度统计",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (statistics != null && statistics.gamesByDifficulty.isNotEmpty()) {
                statistics.gamesByDifficulty.forEach { (difficulty, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(difficulty.displayName)
                        Text("$count 局")
                    }
                }
            } else {
                Text("暂无数据")
            }
        }
    }
}

/**
 * 游戏记录卡片
 */
@Composable
fun GameRecordCard(record: GameRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${record.gameMode.displayName} - ${record.difficulty.displayName}",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "分数: ${record.finalScore}",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "蛇长: ${record.maxSnakeLength}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = formatPlayTime(record.playTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Text(
                text = formatTimestamp(record.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 统计项组件
 */
@Composable
fun StatisticItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 格式化游戏时长
 */
fun formatPlayTime(timeMs: Long): String {
    val seconds = timeMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}时${minutes % 60}分"
        minutes > 0 -> "${minutes}分${seconds % 60}秒"
        else -> "${seconds}秒"
    }
}

/**
 * 格式化时间戳
 */
fun formatTimestamp(timestamp: Long): String {
    // 简化实现，实际项目中应使用适当的日期格式化库
    val date = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
    return date.toString().substring(0, 19).replace('T', ' ')
}