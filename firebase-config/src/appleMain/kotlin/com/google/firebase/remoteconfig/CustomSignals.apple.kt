/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

/** @property signals The signals as the iOS SDK takes them. */
public actual class CustomSignals internal constructor(public val signals: Map<String, Any?>) {
    override fun equals(other: Any?): Boolean = other is CustomSignals && other.signals == signals

    override fun hashCode(): Int = signals.hashCode()

    override fun toString(): String = "CustomSignals($signals)"

    public actual class Builder actual constructor() {
        private val signals = mutableMapOf<String, Any?>()

        public actual fun put(key: String, value: String?): Builder = apply { signals[key] = value }

        public actual fun put(key: String, value: Long): Builder = apply { signals[key] = value }

        public actual fun put(key: String, value: Double): Builder = apply { signals[key] = value }

        public actual fun build(): CustomSignals = CustomSignals(signals.toMap())
    }
}
