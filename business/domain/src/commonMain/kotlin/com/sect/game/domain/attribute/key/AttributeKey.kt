package com.sect.game.domain.attribute.key

import com.sect.game.domain.attribute.value.AttributeValue

/**
 * 类型安全的属性键
 * 使用 @JvmInline value class 确保编译期类型安全
 *
 * @param T 属性值的具体类型
 * @property name 属性键名称
 */
@JvmInline
value class AttributeKey<T : AttributeValue>(val name: String) {

    override fun toString(): String = name
}
