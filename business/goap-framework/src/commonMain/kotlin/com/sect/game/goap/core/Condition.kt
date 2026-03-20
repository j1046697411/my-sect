package com.sect.game.goap.core

sealed class Condition {
    abstract fun isSatisfiedBy(state: WorldState): Boolean

    data class GreaterThan(val key: String, val threshold: Int) : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean {
            return (state.getValue(key) ?: 0) > threshold
        }
    }

    data class LessThan(val key: String, val threshold: Int) : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean {
            return (state.getValue(key) ?: 0) < threshold
        }
    }

    data class Equals(val key: String, val value: Int) : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean {
            return state.getValue(key) == value
        }
    }

    data class GreaterThanOrEqual(val key: String, val threshold: Int) : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean {
            return (state.getValue(key) ?: 0) >= threshold
        }
    }

    data class LessThanOrEqual(val key: String, val threshold: Int) : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean {
            return (state.getValue(key) ?: 0) <= threshold
        }
    }

    data class Not(val condition: Condition) : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean = !condition.isSatisfiedBy(state)
    }

    data class And(val left: Condition, val right: Condition) : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean {
            return left.isSatisfiedBy(state) && right.isSatisfiedBy(state)
        }
    }

    data class Or(val left: Condition, val right: Condition) : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean {
            return left.isSatisfiedBy(state) || right.isSatisfiedBy(state)
        }
    }

    object Always : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean = true
    }

    object Never : Condition() {
        override fun isSatisfiedBy(state: WorldState): Boolean = false
    }

    companion object {
        fun greaterThan(key: String, threshold: Int) = GreaterThan(key, threshold)
        fun lessThan(key: String, threshold: Int) = LessThan(key, threshold)
        fun greaterThanOrEqual(key: String, threshold: Int) = GreaterThanOrEqual(key, threshold)
        fun lessThanOrEqual(key: String, threshold: Int) = LessThanOrEqual(key, threshold)
        fun equals(key: String, value: Int) = Equals(key, value)
        fun and(left: Condition, right: Condition) = And(left, right)
        fun or(left: Condition, right: Condition) = Or(left, right)
    }
}
