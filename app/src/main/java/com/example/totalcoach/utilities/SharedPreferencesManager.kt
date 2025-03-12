package com.example.totalcoach.utilities

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        Constants.SP_FILE,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        @Volatile
        private var instance: SharedPreferencesManager? = null

        fun init(context: Context): SharedPreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferencesManager(context).also { instance = it }
            }
        }

        fun getInstance(): SharedPreferencesManager {
            return instance ?: throw IllegalStateException(
                "SharedPreferencesManager must be initialized by calling init(context) before use."
            )
        }
    }

    // Save String data
    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    // Retrieve String data
    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    // Save Int data
    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    // Retrieve Int data
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    // Save Boolean data
    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    // Retrieve Boolean data
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    // Save Float data
    fun saveFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    // Retrieve Float data
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    // Save Object (Using Gson for serialization)
    fun saveObject(key: String, obj: Any) {
        val json = gson.toJson(obj)
        sharedPreferences.edit().putString(key, json).apply()
    }

    // Remove a single value
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    // Clear all saved preferences
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
