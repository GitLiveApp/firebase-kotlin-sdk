/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsObject
import dev.gitlive.firebase.externals.jsObjectKeys
import dev.gitlive.firebase.externals.jsSet
import dev.gitlive.firebase.externals.jsonStringify
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.externals.toList

/** @property js The signals as the JS SDK takes them: a string, number or null (removes the signal) per key. */
public actual class CustomSignals internal constructor(public val js: JsAny) {
    override fun equals(other: Any?): Boolean = other is CustomSignals && jsonStringify(other.js) == jsonStringify(js)

    override fun hashCode(): Int = jsonStringify(js).hashCode()

    override fun toString(): String = "CustomSignals(${jsonStringify(js)})"

    public actual class Builder actual constructor() {
        private val signals = jsObject()

        public actual fun put(key: String, value: String?): Builder = apply { jsSet(signals, key, value.toJs()) }

        public actual fun put(key: String, value: Long): Builder = apply { jsSet(signals, key, value.toDouble().toJs()) }

        public actual fun put(key: String, value: Double): Builder = apply { jsSet(signals, key, value.toJs()) }

        public actual fun build(): CustomSignals = CustomSignals(
            jsObject().also { copy -> jsObjectKeys(signals).toList().forEach { key -> jsSet(copy, key.toKotlinString(), jsGet(signals, key.toKotlinString())) } },
        )
    }
}
