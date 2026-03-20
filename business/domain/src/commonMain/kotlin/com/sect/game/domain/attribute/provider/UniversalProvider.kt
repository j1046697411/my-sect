package com.sect.game.domain.attribute.provider

import com.sect.game.domain.attribute.modifier.Modifier

/**
 * 通用属性提供者实现
 * 可快速创建简单的属性提供者
 */
class UniversalProvider(
    override val id: String,
    override val name: String,
    override val providerType: ProviderType,
    private val modifiers: List<Modifier> = emptyList(),
    private val skillSlots: List<Any> = emptyList(),
    private val metaAttributes: Map<String, String> = emptyMap(),
) : AttributeProvider {

    override fun getModifiers(): List<Modifier> = modifiers

    override fun getSkillSlots(): List<Any> = skillSlots

    override fun getMetaAttributes(): Map<String, String> = metaAttributes

    companion object {
        fun create(
            id: String,
            name: String,
            providerType: ProviderType,
            modifiers: List<Modifier> = emptyList(),
            skillSlots: List<Any> = emptyList(),
            metaAttributes: Map<String, String> = emptyMap(),
        ): UniversalProvider = UniversalProvider(id, name, providerType, modifiers, skillSlots, metaAttributes)
    }
}
