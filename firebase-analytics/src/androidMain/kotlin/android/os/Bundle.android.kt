/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.os

import dev.gitlive.firebase.analytics.stub

/*
 * Header stub for android.os.Bundle (see buildSrc utils/HeaderStubs.kt): it only satisfies the `expect` declaration at
 * compile time and is deleted from the compilation output, so the platform class binds at runtime. Every member must
 * exist on the real class with the same JVM signature (verified by the build against the Android platform jar).
 */
public actual class Bundle {
    public actual constructor()

    public actual constructor(bundle: Bundle)

    public actual fun putString(key: String?, value: String?): Unit = stub()
    public actual fun putInt(key: String?, value: Int): Unit = stub()
    public actual fun putLong(key: String?, value: Long): Unit = stub()
    public actual fun putDouble(key: String?, value: Double): Unit = stub()
    public actual fun putBoolean(key: String?, value: Boolean): Unit = stub()
    public actual fun putBundle(key: String?, value: Bundle?): Unit = stub()
    public actual fun putAll(bundle: Bundle): Unit = stub()

    public actual fun getString(key: String?): String? = stub()
    public actual fun getString(key: String?, defaultValue: String): String = stub()
    public actual fun getInt(key: String?): Int = stub()
    public actual fun getInt(key: String?, defaultValue: Int): Int = stub()
    public actual fun getLong(key: String?): Long = stub()
    public actual fun getLong(key: String?, defaultValue: Long): Long = stub()
    public actual fun getDouble(key: String?): Double = stub()
    public actual fun getDouble(key: String?, defaultValue: Double): Double = stub()
    public actual fun getBoolean(key: String?): Boolean = stub()
    public actual fun getBoolean(key: String?, defaultValue: Boolean): Boolean = stub()
    public actual fun getBundle(key: String?): Bundle? = stub()

    public actual fun containsKey(key: String?): Boolean = stub()
    public actual fun keySet(): Set<String> = stub()
    public actual fun size(): Int = stub()
    public actual fun isEmpty(): Boolean = stub()
    public actual fun remove(key: String?): Unit = stub()
    public actual fun clear(): Unit = stub()
}
