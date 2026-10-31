/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.os

/** A map-backed `android.os.Bundle` for the platforms without one. */
public actual class Bundle internal constructor(internal val map: MutableMap<String, Any?>) {
    public actual constructor() : this(linkedMapOf())

    public actual constructor(bundle: Bundle) : this(LinkedHashMap(bundle.map))

    public actual fun putString(key: String?, value: String?) {
        map[key.orEmpty()] = value
    }

    public actual fun putInt(key: String?, value: Int) {
        map[key.orEmpty()] = value
    }

    public actual fun putLong(key: String?, value: Long) {
        map[key.orEmpty()] = value
    }

    public actual fun putDouble(key: String?, value: Double) {
        map[key.orEmpty()] = value
    }

    public actual fun putBoolean(key: String?, value: Boolean) {
        map[key.orEmpty()] = value
    }

    public actual fun putBundle(key: String?, value: Bundle?) {
        map[key.orEmpty()] = value
    }

    public actual fun putAll(bundle: Bundle) {
        map.putAll(bundle.map)
    }

    public actual fun getString(key: String?): String? = map[key.orEmpty()] as? String

    public actual fun getString(key: String?, defaultValue: String): String = getString(key) ?: defaultValue

    public actual fun getInt(key: String?): Int = getInt(key, 0)

    public actual fun getInt(key: String?, defaultValue: Int): Int = (map[key.orEmpty()] as? Int) ?: defaultValue

    public actual fun getLong(key: String?): Long = getLong(key, 0L)

    public actual fun getLong(key: String?, defaultValue: Long): Long = (map[key.orEmpty()] as? Long) ?: defaultValue

    public actual fun getDouble(key: String?): Double = getDouble(key, 0.0)

    public actual fun getDouble(key: String?, defaultValue: Double): Double = (map[key.orEmpty()] as? Double) ?: defaultValue

    public actual fun getBoolean(key: String?): Boolean = getBoolean(key, false)

    public actual fun getBoolean(key: String?, defaultValue: Boolean): Boolean = (map[key.orEmpty()] as? Boolean) ?: defaultValue

    public actual fun getBundle(key: String?): Bundle? = map[key.orEmpty()] as? Bundle

    public actual fun containsKey(key: String?): Boolean = map.containsKey(key.orEmpty())

    public actual fun keySet(): Set<String> = map.keys

    public actual fun size(): Int = map.size

    public actual fun isEmpty(): Boolean = map.isEmpty()

    public actual fun remove(key: String?) {
        map.remove(key.orEmpty())
    }

    public actual fun clear() {
        map.clear()
    }

    override fun equals(other: Any?): Boolean = other is Bundle && other.map == map

    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String = "Bundle$map"
}
