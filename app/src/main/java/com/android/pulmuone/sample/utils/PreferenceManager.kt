package com.android.pulmuone.sample.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 데이터 저장 및 로드 클래스
 */
class PreferenceManager {
    private val PREFERENCES_NAME = "mcfs_preference"

    private val DEFAULT_VALUE_STRING = ""
    private val DEFAULT_VALUE_BOOLEAN = false
    private val DEFAULT_VALUE_INT = -1
    private val DEFAULT_VALUE_LONG = -1L
    private val DEFAULT_VALUE_FLOAT = -1f

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    /*
       String 값 저장
       @param context
       @param key
       @param value
     */
    fun setString(context: Context, key: String?, value: String?) {
        getPreferences(context).edit().putString(key, value).apply()
    }

    /*
       boolean 값 저장
       @param context
       @param key
       @param value
     */
    fun setBoolean(context: Context, key: String?, value: Boolean) {
        getPreferences(context).edit().putBoolean(key, value).apply()
    }

    /*
       int 값 저장
       @param context
       @param key
       @param value
     */
    fun setInt(context: Context, key: String?, value: Int) {
        getPreferences(context).edit().putInt(key, value).apply()
    }

    /*
       long 값 저장
       @param context
       @param key
       @param value
     */
    fun setLong(context: Context, key: String?, value: Long) {
        getPreferences(context).edit().putLong(key, value).apply()
    }

    /*
       float 값 저장
       @param context
       @param key
       @param value
     */
    fun setFloat(context: Context, key: String?, value: Float) {
        getPreferences(context).edit().putFloat(key, value).apply()
    }

    /*
       String 값 로드
       @param context
       @param key
       @return
     */
    fun getString(context: Context, key: String?): String? {
        return getPreferences(context).getString(key, DEFAULT_VALUE_STRING)
    }

    /*
       boolean 값 로드
       @param context
       @param key
       @return
     */
    fun getBoolean(context: Context, key: String?): Boolean {
        return getPreferences(context).getBoolean(key, DEFAULT_VALUE_BOOLEAN)
    }

    /*
       int 값 로드
       @param context
       @param key
       @return
     */
    fun getInt(context: Context, key: String?): Int {
        return getPreferences(context).getInt(key, DEFAULT_VALUE_INT)
    }

    /*
       long 값 로드
       @param context
       @param key
       @return
     */
    fun getLong(context: Context, key: String?): Long {
        return getPreferences(context).getLong(key, DEFAULT_VALUE_LONG)
    }

    /*
       float 값 로드
       @param context
       @param key
       @return
     */
    fun getFloat(context: Context, key: String?): Float {
        return getPreferences(context).getFloat(key, DEFAULT_VALUE_FLOAT)
    }

    /*
       키 값 삭제
       @param context
       @param key
     */
    fun removeKey(context: Context, key: String?) {
        getPreferences(context).edit().remove(key).apply()
    }

    /*
       모든 저장 데이터 삭제
       @param context
     */
    fun clear(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}