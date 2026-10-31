/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import kotlin.js.Json
import kotlin.js.json

/** @property js The signals as the JS SDK takes them: a string, number or null (removes the signal) per key. */
public actual class CustomSignals internal constructor(public val js: Json) {
    override fun equals(other: Any?): Boolean = other is CustomSignals && JSON.stringify(other.js) == JSON.stringify(js)

    override fun hashCode(): Int = JSON.stringify(js).hashCode()

    override fun toString(): String = "CustomSignals(${JSON.stringify(js)})"

    public actual class Builder actual constructor() {
        private val signals = json()

        public actual fun put(key: String, value: String?): Builder = apply { signals[key] = value }

        public actual fun put(key: String, value: Long): Builder = apply { signals[key] = value.toDouble() }

        public actual fun put(key: String, value: Double): Builder = apply { signals[key] = value }

        public actual fun build(): CustomSignals = CustomSignals(json().apply { js("Object.keys")(signals).unsafeCast<Array<String>>().forEach { this[it] = signals[it] } })
    }
}
