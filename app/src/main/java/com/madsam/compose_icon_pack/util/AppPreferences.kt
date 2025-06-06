package com.madsam.compose_icon_pack.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// 单例模式，为应用创建一个DataStore实例
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore工具类，替代SP库
 */
class AppPreferences(private val context: Context) {

    /**
     * 保存字符串值
     */
    suspend fun saveString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    /**
     * 保存布尔值
     */
    suspend fun saveBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    /**
     * 保存整数值
     */
    suspend fun saveInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    /**
     * 获取字符串值
     */
    fun getString(key: String, defaultValue: String = ""): Flow<String> {
        val preferencesKey = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: defaultValue
        }
    }

    /**
     * 获取布尔值
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean> {
        val preferencesKey = booleanPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: defaultValue
        }
    }

    /**
     * 获取整数值
     */
    fun getInt(key: String, defaultValue: Int = 0): Flow<Int> {
        val preferencesKey = intPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: defaultValue
        }
    }

    /**
     * 获取字符串值（阻塞）
     * 仅在必要时使用，一般应使用Flow版本
     */
    fun getStringBlocking(key: String, defaultValue: String = ""): String {
        return runBlocking {
            getString(key, defaultValue).first()
        }
    }

    /**
     * 获取布尔值（阻塞）
     * 仅在必要时使用，一般应使用Flow版本
     */
    fun getBooleanBlocking(key: String, defaultValue: Boolean = false): Boolean {
        return runBlocking {
            getBoolean(key, defaultValue).first()
        }
    }

    /**
     * 获取整数值（阻塞）
     * 仅在必要时使用，一般应使用Flow版本
     */
    fun getIntBlocking(key: String, defaultValue: Int = 0): Int {
        return runBlocking {
            getInt(key, defaultValue).first()
        }
    }
}