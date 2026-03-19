package com.sect.game.data.storage

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

lateinit var androidAppContext: Context

fun initAndroidStorage(context: Context) {
    androidAppContext = context.applicationContext
}

actual fun createPlatformStorage(): PlatformGameStorage = AndroidGameStorage(androidAppContext)

class AndroidGameStorage(
    private val context: Context,
    private val prefsName: String = "game_save"
) : PlatformGameStorage {
    
    private val sharedPrefs: SharedPreferences
        get() = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    
    override fun writeToFile(content: String) {
        sharedPrefs.edit().putString("save_data", content).apply()
    }

    override fun readFromFile(): String? {
        return sharedPrefs.getString("save_data", null)
    }

    override fun deleteFile() {
        sharedPrefs.edit().remove("save_data").apply()
    }

    override fun fileExists(): Boolean {
        return sharedPrefs.contains("save_data")
    }
}
