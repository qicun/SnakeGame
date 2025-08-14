package org.example.project.snake.storage

import android.content.Context
import android.content.SharedPreferences

/**
 * Android平台的存储实现
 * 
 * 使用SharedPreferences实现数据持久化存储
 * 
 * @param context Android上下文
 */
actual class PlatformStorage(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "snake_game_prefs"
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private val editor: SharedPreferences.Editor
        get() = sharedPreferences.edit()
    
    actual fun putString(key: String, value: String) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            editor.putString(key, value).apply()
        } catch (e: Exception) {
            throw StorageException("Failed to save string for key: $key", e)
        }
    }
    
    actual fun getString(key: String, defaultValue: String?): String? {
        return try {
            sharedPreferences.getString(key, defaultValue)
        } catch (e: Exception) {
            throw StorageException("Failed to get string for key: $key", e)
        }
    }
    
    actual fun putInt(key: String, value: Int) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            editor.putInt(key, value).apply()
        } catch (e: Exception) {
            throw StorageException("Failed to save int for key: $key", e)
        }
    }
    
    actual fun getInt(key: String, defaultValue: Int): Int {
        return try {
            sharedPreferences.getInt(key, defaultValue)
        } catch (e: Exception) {
            throw StorageException("Failed to get int for key: $key", e)
        }
    }
    
    actual fun putLong(key: String, value: Long) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            editor.putLong(key, value).apply()
        } catch (e: Exception) {
            throw StorageException("Failed to save long for key: $key", e)
        }
    }
    
    actual fun getLong(key: String, defaultValue: Long): Long {
        return try {
            sharedPreferences.getLong(key, defaultValue)
        } catch (e: Exception) {
            throw StorageException("Failed to get long for key: $key", e)
        }
    }
    
    actual fun putFloat(key: String, value: Float) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            editor.putFloat(key, value).apply()
        } catch (e: Exception) {
            throw StorageException("Failed to save float for key: $key", e)
        }
    }
    
    actual fun getFloat(key: String, defaultValue: Float): Float {
        return try {
            sharedPreferences.getFloat(key, defaultValue)
        } catch (e: Exception) {
            throw StorageException("Failed to get float for key: $key", e)
        }
    }
    
    actual fun putBoolean(key: String, value: Boolean) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            editor.putBoolean(key, value).apply()
        } catch (e: Exception) {
            throw StorageException("Failed to save boolean for key: $key", e)
        }
    }
    
    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return try {
            sharedPreferences.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            throw StorageException("Failed to get boolean for key: $key", e)
        }
    }
    
    actual fun remove(key: String) {
        try {
            editor.remove(key).apply()
        } catch (e: Exception) {
            throw StorageException("Failed to remove key: $key", e)
        }
    }
    
    actual fun clear() {
        try {
            editor.clear().apply()
        } catch (e: Exception) {
            throw StorageException("Failed to clear storage", e)
        }
    }
    
    actual fun contains(key: String): Boolean {
        return try {
            sharedPreferences.contains(key)
        } catch (e: Exception) {
            throw StorageException("Failed to check if key exists: $key", e)
        }
    }
    
    actual fun getAllKeys(): Set<String> {
        return try {
            sharedPreferences.all.keys
        } catch (e: Exception) {
            throw StorageException("Failed to get all keys", e)
        }
    }
}