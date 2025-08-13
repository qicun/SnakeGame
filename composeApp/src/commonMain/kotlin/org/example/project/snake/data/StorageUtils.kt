package org.example.project.snake.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.snake.storage.PlatformStorage

/**
 * 存储工具类
 * 
 * 提供通用的数据序列化和存储操作
 */
class StorageUtils(private val storage: PlatformStorage) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * 保存可序列化对象
     */
    suspend inline fun <reified T> saveObject(key: String, obj: T) = withContext(Dispatchers.Default) {
        try {
            val jsonString = json.encodeToString(obj)
            storage.putString(key, jsonString)
        } catch (e: Exception) {
            throw StorageException("Failed to save object for key: $key", e)
        }
    }
    
    /**
     * 读取可序列化对象
     */
    suspend inline fun <reified T> loadObject(key: String, defaultValue: T): T = withContext(Dispatchers.Default) {
        try {
            val jsonString = storage.getString(key, null)
            if (jsonString.isNullOrEmpty()) {
                return@withContext defaultValue
            }
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    /**
     * 保存对象列表
     */
    suspend inline fun <reified T> saveList(key: String, list: List<T>) = withContext(Dispatchers.Default) {
        try {
            val jsonString = json.encodeToString(list)
            storage.putString(key, jsonString)
            storage.putInt("${key}_count", list.size)
        } catch (e: Exception) {
            throw StorageException("Failed to save list for key: $key", e)
        }
    }
    
    /**
     * 读取对象列表
     */
    suspend inline fun <reified T> loadList(key: String): List<T> = withContext(Dispatchers.Default) {
        try {
            val jsonString = storage.getString(key, null)
            if (jsonString.isNullOrEmpty()) {
                return@withContext emptyList()
            }
            json.decodeFromString<List<T>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 添加项目到列表
     */
    suspend inline fun <reified T> addToList(key: String, item: T, maxSize: Int = 1000) = withContext(Dispatchers.Default) {
        try {
            val currentList = loadList<T>(key).toMutableList()
            currentList.add(0, item) // 添加到列表开头
            
            // 限制列表大小
            if (currentList.size > maxSize) {
                currentList.removeAt(currentList.size - 1)
            }
            
            saveList(key, currentList)
        } catch (e: Exception) {
            throw StorageException("Failed to add item to list for key: $key", e)
        }
    }
    
    /**
     * 从列表中移除项目
     */
    suspend inline fun <reified T> removeFromList(key: String, predicate: (T) -> Boolean) = withContext(Dispatchers.Default) {
        try {
            val currentList = loadList<T>(key).toMutableList()
            currentList.removeAll(predicate)
            saveList(key, currentList)
        } catch (e: Exception) {
            throw StorageException("Failed to remove item from list for key: $key", e)
        }
    }
    
    /**
     * 更新列表中的项目
     */
    suspend inline fun <reified T> updateInList(key: String, predicate: (T) -> Boolean, updater: (T) -> T) = withContext(Dispatchers.Default) {
        try {
            val currentList = loadList<T>(key).toMutableList()
            val index = currentList.indexOfFirst(predicate)
            if (index >= 0) {
                currentList[index] = updater(currentList[index])
                saveList(key, currentList)
            }
        } catch (e: Exception) {
            throw StorageException("Failed to update item in list for key: $key", e)
        }
    }
    
    /**
     * 获取列表大小
     */
    suspend fun getListSize(key: String): Int = withContext(Dispatchers.Default) {
        try {
            storage.getInt("${key}_count", 0)
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 检查键是否存在
     */
    suspend fun hasKey(key: String): Boolean = withContext(Dispatchers.Default) {
        try {
            storage.contains(key)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 删除键
     */
    suspend fun removeKey(key: String) = withContext(Dispatchers.Default) {
        try {
            storage.remove(key)
            storage.remove("${key}_count") // 同时删除计数键
        } catch (e: Exception) {
            throw StorageException("Failed to remove key: $key", e)
        }
    }
    
    /**
     * 获取所有键
     */
    suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.Default) {
        try {
            storage.getAllKeys()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    /**
     * 清除所有数据
     */
    suspend fun clearAll() = withContext(Dispatchers.Default) {
        try {
            storage.clear()
        } catch (e: Exception) {
            throw StorageException("Failed to clear all data", e)
        }
    }
    
    /**
     * 获取存储大小（字节）
     */
    suspend fun getStorageSize(): Long = withContext(Dispatchers.Default) {
        try {
            val allKeys = storage.getAllKeys()
            var totalSize = 0L
            
            allKeys.forEach { key ->
                val value = storage.getString(key, "")
                totalSize += value.toByteArray(Charsets.UTF_8).size
            }
            
            totalSize
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 数据备份
     */
    suspend fun createBackup(): String = withContext(Dispatchers.Default) {
        try {
            val allKeys = storage.getAllKeys()
            val backupData = mutableMapOf<String, String>()
            
            allKeys.forEach { key ->
                val value = storage.getString(key, "")
                if (value.isNotEmpty()) {
                    backupData[key] = value
                }
            }
            
            val backupInfo = BackupInfo(
                timestamp = System.currentTimeMillis(),
                version = "1.0",
                dataCount = backupData.size,
                data = backupData
            )
            
            json.encodeToString(backupInfo)
        } catch (e: Exception) {
            throw StorageException("Failed to create backup", e)
        }
    }
    
    /**
     * 数据恢复
     */
    suspend fun restoreFromBackup(backupData: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val backupInfo = json.decodeFromString<BackupInfo>(backupData)
            
            // 验证备份版本
            if (!isBackupVersionCompatible(backupInfo.version)) {
                return@withContext false
            }
            
            // 清除现有数据
            storage.clear()
            
            // 恢复数据
            backupInfo.data.forEach { (key, value) ->
                storage.putString(key, value)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查备份版本兼容性
     */
    private fun isBackupVersionCompatible(version: String): Boolean {
        return version == "1.0"
    }
}

/**
 * 备份信息数据类
 */
@kotlinx.serialization.Serializable
private data class BackupInfo(
    val timestamp: Long,
    val version: String,
    val dataCount: Int,
    val data: Map<String, String>
)

/**
 * 存储异常类
 */
class StorageException(message: String, cause: Throwable? = null) : Exception(message, cause)