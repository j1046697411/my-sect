package com.sect.game.domain.attribute.provider

import com.sect.game.domain.attribute.modifier.Modifier

/**
 * 属性提供者接口
 * 所有可提供属性的对象需实现此接口
 */
interface AttributeProvider {
    /** 提供者唯一标识 */
    val id: String

    /** 提供者名称 */
    val name: String

    /** 提供者类型 */
    val providerType: ProviderType

    /**
     * 获取该提供者提供的所有修饰器
     * @return 修饰器列表
     */
    fun getModifiers(): List<Modifier>

    /**
     * 获取该提供者提供的技能槽位
     * @return 技能槽位列表（延期功能，目前返回空列表）
     */
    fun getSkillSlots(): List<Any> = emptyList()

    /**
     * 获取该提供者的元属性
     * @return 元属性映射（键值对）
     */
    fun getMetaAttributes(): Map<String, String>
}
