/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.os

/**
 * The subset of `android.os.Bundle` that Firebase Analytics events and default parameters are built from, so that
 * Android code passing a `Bundle` to [com.google.firebase.analytics.FirebaseAnalytics.logEvent] compiles in common code.
 *
 * On Android and the JVM this is a header stub that binds to the real class; on the other platforms it is a map.
 * Values are strings, integral and floating-point numbers, booleans, nested bundles and arrays of bundles (the
 * `items` parameter); the array is added with [com.google.firebase.analytics.ParametersBuilder.param].
 */
public expect class Bundle {
    public constructor()

    /** A copy of [bundle]. */
    public constructor(bundle: Bundle)

    public fun putString(key: String?, value: String?)
    public fun putInt(key: String?, value: Int)
    public fun putLong(key: String?, value: Long)
    public fun putDouble(key: String?, value: Double)
    public fun putBoolean(key: String?, value: Boolean)
    public fun putBundle(key: String?, value: Bundle?)

    /** Adds every mapping of [bundle]. */
    public fun putAll(bundle: Bundle)

    public fun getString(key: String?): String?
    public fun getString(key: String?, defaultValue: String): String
    public fun getInt(key: String?): Int
    public fun getInt(key: String?, defaultValue: Int): Int
    public fun getLong(key: String?): Long
    public fun getLong(key: String?, defaultValue: Long): Long
    public fun getDouble(key: String?): Double
    public fun getDouble(key: String?, defaultValue: Double): Double
    public fun getBoolean(key: String?): Boolean
    public fun getBoolean(key: String?, defaultValue: Boolean): Boolean
    public fun getBundle(key: String?): Bundle?

    public fun containsKey(key: String?): Boolean
    public fun keySet(): Set<String>
    public fun size(): Int
    public fun isEmpty(): Boolean
    public fun remove(key: String?)
    public fun clear()
}
