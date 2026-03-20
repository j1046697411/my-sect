package com.sect.game.domain.attribute.modifier

/**
 * 修饰器来源追踪接口
 */
interface ModifierSource {
    /** 来源类型 */
    val sourceType: SourceType

    /** 来源唯一标识 */
    val sourceId: String
}
