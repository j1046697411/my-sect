package com.sect.game.data.storage

import com.sect.game.domain.entity.Sect

/**
 * 游戏存档存储接口
 * 定义游戏数据的持久化操作
 */
interface GameStorage {
    /**
     * 保存游戏存档
     * @param sect 宗门数据
     * @param version 存档版本号，用于迁移支持
     * @return 保存结果
     */
    fun saveGame(sect: Sect, version: Int): Result<Unit>

    /**
     * 加载游戏存档
     * @return 加载的宗门数据
     */
    fun loadGame(): Result<Sect>

    /**
     * 删除游戏存档
     * @return 删除结果
     */
    fun deleteGame(): Result<Unit>

    /**
     * 检查存档是否存在
     * @return 是否存在
     */
    fun gameExists(): Boolean
}
