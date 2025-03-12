package com.example.totalcoach

import android.app.Application
import com.example.totalcoach.utilities.ImageLoader
import com.example.totalcoach.utilities.SharedPreferencesManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize SharedPreferencesManager with context
        ImageLoader.init(this)
        SharedPreferencesManager.init(applicationContext)
    }
}