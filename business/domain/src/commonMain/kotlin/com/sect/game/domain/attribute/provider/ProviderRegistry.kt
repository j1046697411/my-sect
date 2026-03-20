package com.sect.game.domain.attribute.provider

/**
 * 属性提供者全局注册表
 * 使用单例模式管理所有属性提供者
 */
object ProviderRegistry {
    private val providers = mutableMapOf<String, AttributeProvider>()
    private val providersByType = mutableMapOf<ProviderType, MutableList<AttributeProvider>>()

    /**
     * 注册属性提供者
     * @param provider 要注册的属性提供者
     */
    fun register(provider: AttributeProvider) {
        providers[provider.id] = provider
        providersByType.getOrPut(provider.providerType) { mutableListOf() }.add(provider)
    }

    /**
     * 根据ID获取提供者
     * @param id 提供者ID
     * @return 提供者实例，不存在则返回null
     */
    fun get(id: String): AttributeProvider? = providers[id]

    /**
     * 根据类型获取所有该类型的提供者
     * @param type 提供者类型
     * @return 该类型的所有提供者列表
     */
    fun getByType(type: ProviderType): List<AttributeProvider> {
        return providersByType[type]?.toList() ?: emptyList()
    }

    /**
     * 获取所有已注册的提供者
     * @return 所有提供者列表
     */
    fun getAll(): List<AttributeProvider> = providers.values.toList()

    /**
     * 重置注册表（仅用于测试）
     */
    fun resetForTest() {
        providers.clear()
        providersByType.clear()
    }
}
