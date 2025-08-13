package org.example.project.snake.storage

import platform.Foundation.NSUserDefaults

/**
 * iOS平台的存储实现
 * 
 * 使用NSUserDefaults实现数据持久化存储
 */
actual class PlatformStorage {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual fun saveString(key: String, value: String) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            userDefaults.setObject(value, key)
            userDefaults.synchronize()
        } catch (e: Exception) {
            throw StorageException("Failed to save string for key: $key", e)
        }
    }
    
    actual fun getString(key: String, defaultValue: String): String {
        return try {
            userDefaults.stringForKey(key) ?: defaultValue
        } catch (e: Exception) {
            throw StorageException("Failed to get string for key: $key", e)
        }
    }
    
    actual fun saveInt(key: String, value: Int) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            userDefaults.setInteger(value.toLong(), key)
            userDefaults.synchronize()
        } catch (e: Exception) {
            throw StorageException("Failed to save int for key: $key", e)
        }
    }
    
    actual fun getInt(key: String, defaultValue: Int): Int {
        return try {
            userDefaults.integerForKey(key).toInt()
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    actual fun saveLong(key: String, value: Long) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            userDefaults.setObject(value, key)
            userDefaults.synchronize()
        } catch (e: Exception) {
            throw StorageException("Failed to save long for key: $key", e)
        }
    }
    
    actual fun getLong(key: String, defaultValue: Long): Long {
        return try {
            val value = userDefaults.objectForKey(key)
            (value as? Long) ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    actual fun saveFloat(key: String, value: Float) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            userDefaults.setFloat(value, key)
            userDefaults.synchronize()
        } catch (e: Exception) {
            throw StorageException("Failed to save float for key: $key", e)
        }
    }
    
    actual fun getFloat(key: String, defaultValue: Float): Float {
        return try {
            userDefaults.floatForKey(key)
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    actual fun saveBoolean(key: String, value: Boolean) {
        try {
            if (!StorageUtils.isValidKey(key)) {
                throw StorageException("Invalid key: $key")
            }
            
            userDefaults.setBool(value, key)
            userDefaults.synchronize()
        } catch (e: Exception) {
            throw StorageException("Failed to save boolean for key: $key", e)
        }
    }
    
    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return try {
            userDefaults.boolForKey(key)
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    actual fun remove(key: String) {
        try {
            userDefaults.removeObjectForKey(key)
            userDefaults.synchronize()
        } catch (e: Exception) {
            throw StorageException("Failed to remove key: $key", e)
        }
    }
    
    actual fun clear() {
        try {
            val keys = getAllKeys()
            keys.forEach { key ->
                userDefaults.removeObjectForKey(key)
            }
            userDefaults.synchronize()
        } catch (e: Exception) {
            throw StorageException("Failed to clear storage", e)
        }
    }
    
    actual fun contains(key: String): Boolean {
        return try {
            userDefaults.objectForKey(key) != null
        } catch (e: Exception) {
            false
        }
    }
    
    actual fun getAllKeys(): Set<String> {
        return try {
            val dictionary = userDefaults.dictionaryRepresentation()
            dictionary.keys.mapNotNull { it as? String }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}