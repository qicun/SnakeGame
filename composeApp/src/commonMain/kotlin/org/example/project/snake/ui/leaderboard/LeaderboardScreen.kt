package org.example.project.snake.ui.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.snake.data.LeaderboardEntry
import org.example.project.snake.config.GameMode
import org.example.project.snake.config.Difficulty

/**
 * 排行榜界面
 * 
 * 显示游戏排行榜，支持按游戏模式和难度筛选
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    leaderboard: List<LeaderboardEntry>,
    selectedGameMode: GameMode?,
    selectedDifficulty: Difficulty?,
    onGameModeSelected: (GameMode?) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("排行榜") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 筛选器
            FilterSection(
                selectedGameMode = selectedGameMode,
                selectedDifficulty = selectedDifficulty,
                onGameModeSelected = onGameModeSelected,
                onDifficultySelected = onDifficultySelected
            )
            
            // 排行榜列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (leaderboard.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无排行榜数据")
                        }
                    }
                } else {
                    itemsIndexed(leaderboard) { index, entry ->
                        LeaderboardEntryCard(
                            entry = entry,
                            rank = index + 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * 筛选器部分
 */
@Composable
fun FilterSection(
    selectedGameMode: GameMode?,
    selectedDifficulty: Difficulty?,
    onGameModeSelected: (GameMode?) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "筛选条件",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 游戏模式筛选
            Text("游戏模式", style = MaterialTheme.typography.bodyMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedGameMode == null,
                        onClick = { onGameModeSelected(null) },
                        label = { Text("全部") }
                    )
                }
                items(GameMode.values().size) { index ->
                    val mode = GameMode.values()[index]
                    FilterChip(
                        selected = selectedGameMode == mode,
                        onClick = { onGameModeSelected(mode) },
                        label = { Text(mode.displayName) }
                    )
                }
            }
            
            // 难度筛选
            Text("难度", style = MaterialTheme.typography.bodyMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedDifficulty == null,
                        onClick = { onDifficultySelected(null) },
                        label = { Text("全部") }
                    )
                }
                items(Difficulty.values().size) { index ->
                    val difficulty = Difficulty.values()[index]
                    FilterChip(
                        selected = selectedDifficulty == difficulty,
                        onClick = { onDifficultySelected(difficulty) },
                        label = { Text(difficulty.displayName) }
                    )
                }
            }
        }
    }
}

/**
 * 排行榜条目卡片
 */
@Composable
fun LeaderboardEntryCard(
    entry: LeaderboardEntry,
    rank: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (rank) {
                1 -> Color(0xFFFFD700).copy(alpha = 0.1f) // 金色
                2 -> Color(0xFFC0C0C0).copy(alpha = 0.1f) // 银色
                3 -> Color(0xFFCD7F32).copy(alpha = 0.1f) // 铜色
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 排名
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                when (rank) {
                    1, 2, 3 -> {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "奖杯",
                            tint = when (rank) {
                                1 -> Color(0xFFFFD700) // 金色
                                2 -> Color(0xFFC0C0C0) // 银色
                                else -> Color(0xFFCD7F32) // 铜色
                            }
                        )
                    }
                    else -> {
                        Text(
                            text = rank.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 玩家信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.playerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${entry.gameMode.displayName} - ${entry.difficulty.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "蛇长: ${entry.snakeLength} | 时长: ${entry.formattedPlayTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 分数
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = entry.score.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "效率: ${String.format("%.1f", entry.efficiency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}