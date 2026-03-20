package com.sect.game.data.storage

import java.io.File
import java.io.IOException

actual fun createPlatformStorage(): PlatformGameStorage = DesktopGameStorage()

class DesktopGameStorage(
    private val saveFileName: String = "game_save.json"
) : PlatformGameStorage {
    
    private val saveFile: File
        get() = File(System.getProperty("user.home"), ".sect-game/$saveFileName")

    override fun writeToFile(content: String) {
        try {
            saveFile.parentFile?.mkdirs()
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
