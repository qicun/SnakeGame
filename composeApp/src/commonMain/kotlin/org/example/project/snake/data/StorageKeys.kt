package org.example.project.snake.data

/**
 * 存储键常量
 * 
 * 定义所有用于数据持久化的键名
 */
object StorageKeys {
    // 游戏记录相关
    const val GAME_RECORDS = "game_records"
    const val GAME_RECORD_COUNT = "game_record_count"
    
    // 玩家统计相关
    const val PLAYER_STATISTICS = "player_statistics"
    
    // 游戏配置相关
    const val GAME_CONFIG = "game_config"
    
    // 排行榜相关
    const val LEADERBOARD_ENTRIES = "leaderboard_entries"
    const val LEADERBOARD_COUNT = "leaderboard_count"
    
    // 回放数据相关
    const val REPLAY_DATA = "replay_data"
    const val REPLAY_INDEX = "replay_index"
    
    // 应用设置相关
    const val APP_SETTINGS = "app_settings"
    const val FIRST_LAUNCH = "first_launch"
    const val LAST_BACKUP_TIME = "last_backup_time"
    
    // 缓存相关
    const val CACHE_VERSION = "cache_version"
    const val CACHE_EXPIRY = "cache_expiry"
    
    // 用户偏好相关
    const val USER_PREFERENCES = "user_preferences"
    const val THEME_PREFERENCE = "theme_preference"
    const val SOUND_ENABLED = "sound_enabled"
    const val VIBRATION_ENABLED = "vibration_enabled"
    
    // 成就系统相关
    const val ACHIEVEMENTS = "achievements"
    const val ACHIEVEMENT_PROGRESS = "achievement_progress"
    
    // 数据版本控制
    const val DATA_VERSION = "data_version"
    const val MIGRATION_STATUS = "migration_status"
}