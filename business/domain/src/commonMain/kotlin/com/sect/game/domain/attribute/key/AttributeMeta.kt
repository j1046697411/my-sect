package com.sect.game.domain.attribute.key

import com.sect.game.domain.attribute.value.AttributeValue
import com.sect.game.domain.attribute.value.IntValue

/**
 * 属性元信息
 * 描述属性的基本配置，包括默认值、取值范围和标签
 *
 * @param T 属性值的具体类型
 * @property defaultValue 默认属性值
 * @property range 属性值的有效范围（闭区间）
 * @property tag 属性标签，用于分类展示
 * @property description 属性的中文描述
 */
data class AttributeMeta<T : AttributeValue>(
    val defaultValue: T,
    val range: IntRange,
    val tag: AttributeTag,
    val description: String = "",
) {

    init {
        // 校验默认值是否在范围内
        val defaultInt = defaultValue.toInt()
        require(defaultInt in range) {
            "defaultValue $defaultInt must be within range $range"
        }
    }

    companion object {
        /**
         * 创建整型属性的元信息
         */
        fun int(
            defaultValue: Int,
            range: IntRange,
            tag: AttributeTag,
            description: String = "",
        ): AttributeMeta<IntValue> = AttributeMeta(
            defaultValue = IntValue(defaultValue),
            range = range,
            tag = tag,
            description = description,
        )
    }
}
