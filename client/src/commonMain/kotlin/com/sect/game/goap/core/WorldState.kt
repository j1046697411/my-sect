package com.sect.game.goap.core

/**
 * Represents the world state as an immutable map of key-value pairs.
 * Used by GOAP system to evaluate conditions and plan actions.
 * Supports Boolean, Int, and Float values.
 */
class WorldState(
    private val booleans: Map<String, Boolean> = mapOf(),
    private val ints: Map<String, Int> = mapOf(),
    private val floats: Map<String, Float> = mapOf(),
) {
    fun getBoolean(key: String): Boolean = booleans[key] ?: false

    fun getInt(key: String): Int = ints[key] ?: 0

    fun getFloat(key: String): Float? = floats[key]

    fun getValue(key: String): Int = ints[key] ?: 0

    fun withBoolean(
        key: String,
        value: Boolean,
    ): WorldState {
        return WorldState(booleans + (key to value), ints, floats)
    }

    fun withInt(
        key: String,
        value: Int,
    ): WorldState {
        return WorldState(booleans, ints + (key to value), floats)
    }

    fun withFloat(
        key: String,
        value: Float,
    ): WorldState {
        return WorldState(booleans, ints, floats + (key to value))
    }

    fun withValue(
        key: String,
        value: Int,
    ): WorldState {
        return withInt(key, value)
    }

    fun toMap(): Map<String, Int> = ints

    /**
     * Heuristic distance to target state for A* planning.
     * Returns the number of differing keys as a float.
     */
    fun distanceTo(other: WorldState): Float {
        val allKeys =
            (
                booleans.keys + ints.keys + floats.keys +
                    other.booleans.keys + other.ints.keys + other.floats.keys
            ).toSet()
        var differences = 0f
        for (key in allKeys) {
            when {
                booleans[key] != other.booleans[key] -> differences += 1f
                ints[key] != other.ints[key] -> differences += 1f
                floats[key] != other.floats[key] -> differences += 1f
            }
        }
        return differences
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WorldState) return false
        return booleans == other.booleans && ints == other.ints && floats == other.floats
    }

    override fun hashCode(): Int = 31 * (31 * booleans.hashCode() + ints.hashCode()) + floats.hashCode()

    companion object {
        fun fromMap(map: Map<String, Int>): WorldState = WorldState(ints = map)
    }
}
