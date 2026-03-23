package com.sect.game.domain.attribute.condition

/**
 * 比较操作符枚举
 * 用于属性阈值条件的比较运算
 */
enum class ComparisonOperator {
    /** 大于 */
    GREATER_THAN,

    /** 小于 */
    LESS_THAN,

    /** 等于 */
    EQUAL,

    /** 大于或等于 */
    GREATER_THAN_OR_EQUAL,

    /** 小于或等于 */
    LESS_THAN_OR_EQUAL,
}
