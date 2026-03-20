package com.sect.game.domain.exception

sealed class StorageException(
    message: String,
    userMessage: String,
) : DomainExceptionBase(message, userMessage) {
    class SaveFailedException(val fileName: String, cause: String = "") :
        StorageException(
            message = "Failed to save $fileName: $cause",
            userMessage = "保存失败：$fileName，请稍后重试",
        )

    class LoadFailedException(val fileName: String, cause: String = "") :
        StorageException(
            message = "Failed to load $fileName: $cause",
            userMessage = "加载失败：$fileName，请检查文件是否存在",
        )

    class FileCorruptedException(val fileName: String) :
        StorageException(
            message = "File $fileName is corrupted",
            userMessage = "存档文件已损坏，无法加载",
        )
}
