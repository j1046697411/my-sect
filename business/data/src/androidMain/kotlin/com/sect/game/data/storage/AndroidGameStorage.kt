package com.sect.game.data.storage

import android.content.Context
import java.io.File
import java.io.IOException

private lateinit var appContext: Context

fun initializeAndroidContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createPlatformStorage(): PlatformGameStorage = AndroidGameStorage()

class AndroidGameStorage(
    private val fileName: String = "game_save.json",
) : PlatformGameStorage {
    private val saveFile: File
        get() = File(appContext.filesDir, fileName)

    override fun writeToFile(content: String) {
        try {
            saveFile.writeText(content)
        } catch (e: IOException) {
            throw GameStorageException.SaveWriteException(e)
        }
    }

    override fun readFromFile(): String? {
        return try {
            if (saveFile.exists()) {
                saveFile.readText()
            } else {
                null
            }
        } catch (e: IOException) {
            throw GameStorageException.SaveReadException(e)
        }
    }

    override fun deleteFile() {
        try {
            if (saveFile.exists()) {
                saveFile.delete()
            }
        } catch (e: IOException) {
            throw GameStorageException.SaveDeleteException(e)
        }
    }

    override fun fileExists(): Boolean {
        return saveFile.exists()
    }
}
