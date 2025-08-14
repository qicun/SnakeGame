package org.example.project.snake.storage

/**
 * 跨平台存储抽象
 * 
 * 提供统一的数据存储接口，支持Android和iOS平台
 * 使用expect/actual机制实现平台特定的存储逻辑
 */
expect class PlatformStorage {
    
    /**
     * 保存字符串值
     * 
     * @param key 存储键
     * @param value 要保存的字符串值
     */
    fun putString(key: String, value: String)
    
    /**
     * 获取字符串值
     * 
     * @param key 存储键
     * @param defaultValue 默认值
     * @return 存储的字符串值或默认值
     */
    fun getString(key: String, defaultValue: String? = null): String?
    
    /**
     * 保存整数值
     * 
     * @param key 存储键
     * @param value 要保存的整数值
     */
    fun putInt(key: String, value: Int)
    
    /**
     * 获取整数值
     * 
     * @param key 存储键
     * @param defaultValue 默认值
     * @return 存储的整数值或默认值
     */
    fun getInt(key: String, defaultValue: Int = 0): Int
    
    /**
     * 保存长整数值
     * 
     * @param key 存储键
     * @param value 要保存的长整数值
     */
    fun putLong(key: String, value: Long)
    
    /**
     * 获取长整数值
     * 
     * @param key 存储键
     * @param defaultValue 默认值
     * @return 存储的长整数值或默认值
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long
    
    /**
     * 保存浮点数值
     * 
     * @param key 存储键
     * @param value 要保存的浮点数值
     */
    fun putFloat(key: String, value: Float)
    
    /**
     * 获取浮点数值
     * 
     * @param key 存储键
     * @param defaultValue 默认值
     * @return 存储的浮点数值或默认值
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float
    
    /**
     * 保存布尔值
     * 
     * @param key 存储键
     * @param value 要保存的布尔值
     */
    fun putBoolean(key: String, value: Boolean)
    
    /**
     * 获取布尔值
     * 
     * @param key 存储键
     * @param defaultValue 默认值
     * @return 存储的布尔值或默认值
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * 删除指定键的数据
     * 
     * @param key 要删除的存储键
     */
    fun remove(key: String)
    
    /**
     * 清除所有存储的数据
     */
    fun clear()
    
    /**
     * 检查是否包含指定键
     * 
     * @param key 要检查的存储键
     * @return 如果包含该键返回true，否则返回false
     */
    fun contains(key: String): Boolean
    
    /**
     * 获取所有存储的键
     * 
     * @return 所有存储键的集合
     */
    fun getAllKeys(): Set<String>
}

/**
 * 存储键常量
 * 
 * 定义所有用于数据存储的键名常量，避免硬编码字符串
 */
object StorageKeys {
    
    // 游戏配置相关
    const val GAME_CONFIG = "game_config"
    const val CURRENT_THEME = "current_theme"
    
    // 游戏记录相关
    const val GAME_RECORDS = "game_records"
    const val PLAYER_STATISTICS = "player_statistics"
    const val HIGH_SCORES = "high_scores"
    
    // 成就相关
    const val ACHIEVEMENTS = "achievements"
    const val ACHIEVEMENT_PROGRESS = "achievement_progress"
    
    // 排行榜相关
    const val LEADERBOARD_ENTRIES = "leaderboard_entries"
    const val PLAYER_NAME = "player_name"
    
    // 回放相关
    const val REPLAY_DATA = "replay_data"
    const val AUTO_SAVE_REPLAY = "auto_save_replay"
    
    // 应用设置相关
    const val FIRST_LAUNCH = "first_launch"
    const val APP_VERSION = "app_version"
    const val LAST_BACKUP_TIME = "last_backup_time"
    
    // 统计相关
    const val DAILY_STATS = "daily_stats"
    const val WEEKLY_STATS = "weekly_stats"
    const val MONTHLY_STATS = "monthly_stats"
}

/**
 * 存储异常类
 * 
 * 用于处理存储操作中可能出现的异常情况
 */
class StorageException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 存储工具类
 * 
 * 提供一些常用的存储操作辅助方法
 */
object StorageUtils {
    
    /**
     * 生成带时间戳的键名
     * 
     * @param baseKey 基础键名
     * @return 带时间戳的键名
     */
    fun generateTimestampKey(baseKey: String): String {
        return "${baseKey}_${System.currentTimeMillis()}"
    }
    
    /**
     * 生成日期键名
     * 
     * @param baseKey 基础键名
     * @param timestamp 时间戳，默认为当前时间
     * @return 带日期的键名
     */
    fun generateDateKey(baseKey: String, timestamp: Long = System.currentTimeMillis()): String {
        val date = formatDate(timestamp)
        return "${baseKey}_$date"
    }
    
    /**
     * 格式化时间戳为日期字符串
     * 
     * @param timestamp 时间戳
     * @return 格式化的日期字符串 (YYYY-MM-DD)
     */
    private fun formatDate(timestamp: Long): String {
        // 这里使用简单的日期格式化，实际项目中可能需要使用更完善的日期库
        val date = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
        return date.toString().substring(0, 10) // 取YYYY-MM-DD部分
    }
    
    /**
     * 验证键名是否有效
     * 
     * @param key 要验证的键名
     * @return 如果键名有效返回true，否则返回false
     */
    fun isValidKey(key: String): Boolean {
        return key.isNotBlank() && 
               key.length <= 100 && 
               key.matches(Regex("^[a-zA-Z0-9_.-]+$"))
    }
    
    /**
     * 计算数据大小（字节）
     * 
     * @param data 要计算大小的字符串数据
     * @return 数据大小（字节）
     */
    fun calculateDataSize(data: String): Int {
        return data.toByteArray(Charsets.UTF_8).size
    }
    
    /**
     * 压缩字符串数据
     * 
     * @param data 要压缩的字符串
     * @return 压缩后的字符串
     */
    fun compressData(data: String): String {
        // 简单的压缩实现，实际项目中可能需要使用更高效的压缩算法
        return if (data.length > 1000) {
            // 对于大数据进行简单的重复字符压缩
            compressRepeatedChars(data)
        } else {
            data
        }
    }
    
    /**
     * 解压缩字符串数据
     * 
     * @param compressedData 压缩的字符串
     * @return 解压缩后的字符串
     */
    fun decompressData(compressedData: String): String {
        return decompressRepeatedChars(compressedData)
    }
    
    /**
     * 简单的重复字符压缩
     */
    private fun compressRepeatedChars(data: String): String {
        if (data.isEmpty()) return data
        
        val result = StringBuilder()
        var currentChar = data[0]
        var count = 1
        
        for (i in 1 until data.length) {
            if (data[i] == currentChar) {
                count++
            } else {
                if (count > 3) {
                    result.append("${currentChar}#$count#")
                } else {
                    repeat(count) { result.append(currentChar) }
                }
                currentChar = data[i]
                count = 1
            }
        }
        
        // 处理最后一组字符
        if (count > 3) {
            result.append("${currentChar}#$count#")
        } else {
            repeat(count) { result.append(currentChar) }
        }
        
        return result.toString()
    }
    
    /**
     * 简单的重复字符解压缩
     */
    private fun decompressRepeatedChars(compressedData: String): String {
        val result = StringBuilder()
        var i = 0
        
        while (i < compressedData.length) {
            val char = compressedData[i]
            
            if (i + 2 < compressedData.length && compressedData[i + 1] == '#') {
                // 查找结束的#
                val endIndex = compressedData.indexOf('#', i + 2)
                if (endIndex != -1) {
                    val countStr = compressedData.substring(i + 2, endIndex)
                    val count = countStr.toIntOrNull()
                    if (count != null) {
                        repeat(count) { result.append(char) }
                        i = endIndex + 1
                        continue
                    }
                }
            }
            
            result.append(char)
            i++
        }
        
        return result.toString()
    }
}