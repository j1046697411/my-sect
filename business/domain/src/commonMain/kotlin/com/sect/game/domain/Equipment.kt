package com.sect.game.domain

import com.sect.game.domain.attribute.modifier.Modifier

/**
 * 装备稀有度枚举
 */
enum class Rarity(val order: Int) {
    Common(1),    // 普通
    Uncommon(2),  // 优秀
    Rare(3),      // 稀有
    Epic(4),      // 史诗
    Legendary(5), // 传说
    ;

    companion object {
        fun fromOrder(order: Int): Rarity? {
            return entries.find { it.order == order }
        }
    }
}

/**
 * 装备栏位枚举
 */
enum class EquipmentSlot(val displayName: String) {
    Weapon("武器"),
    Helmet("头盔"),
    Armor("胸甲"),
    Boots("靴子"),
    Gloves("手套"),
    Ring("戒指"),
    Necklace("项链"),
    Cloak("披风"),
    Belt("腰带"),
    Offhand("副手"),
    ;

    companion object {
        fun fromName(name: String): EquipmentSlot? {
            return entries.find { it.name == name }
        }
    }
}

/**
 * 装备领域模型
 * 代表游戏中的装备物品，可提供属性修饰器
 *
 * @param id 装备唯一标识
 * @param name 装备名称
 * @param slot 装备栏位
 * @param rarity 稀有度
 * @param modifiers 属性修饰器列表
 */
data class Equipment(
    val id: String,
    val name: String,
    val slot: EquipmentSlot,
    val rarity: Rarity,
    val modifiers: List<Modifier>,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(name.isNotBlank()) { "name must not be blank" }
    }

    companion object {
        /**
         * 创建装备实例
         * @param id 装备唯一标识
         * @param name 装备名称
         * @param slot 装备栏位
         * @param rarity 稀有度
         * @param modifiers 属性修饰器列表
         * @return 装备创建结果
         */
        fun create(
            id: String,
            name: String,
            slot: EquipmentSlot,
            rarity: Rarity,
            modifiers: List<Modifier> = emptyList(),
        ): Result<Equipment> {
            return Result.runCatching {
                require(id.isNotBlank()) { "id must not be blank" }
                require(name.isNotBlank()) { "name must not be blank" }

                Equipment(
                    id = id,
                    name = name,
                    slot = slot,
                    rarity = rarity,
                    modifiers = modifiers,
                )
            }
        }
    }
}
