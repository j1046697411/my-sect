package com.sect.game.domain

import com.sect.game.domain.attribute.PredefinedAttributes
import com.sect.game.domain.attribute.key.AttributeKey
import com.sect.game.domain.attribute.value.IntValue

/**
 * 弟子相关属性键定义
 * 通过扩展函数与 PredefinedAttributes 映射，提供旧系统到新系统的桥接
 */
object DiscipleAttributes {

    /** 灵根属性键 */
    val SPIRIT_ROOT: AttributeKey<IntValue> = PredefinedAttributes.SPIRIT_ROOT

    /** 资质属性键 */
    val TALENT: AttributeKey<IntValue> = PredefinedAttributes.TALENT

    /** 气运属性键 */
    val LUCK: AttributeKey<IntValue> = PredefinedAttributes.LUCK
}
