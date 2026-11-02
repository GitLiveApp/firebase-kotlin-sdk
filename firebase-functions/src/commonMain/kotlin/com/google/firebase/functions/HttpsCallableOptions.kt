/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import kotlin.jvm.JvmField

/**
 * Options for calling a callable HTTPS trigger, mirroring `com.google.firebase.functions.HttpsCallableOptions` from the
 * Firebase Android SDK; created with a [Builder]. Plain common code: on Android and the JVM it is a header stub for the
 * SDK's own class.
 */
public class HttpsCallableOptions private constructor(
    /** Whether the calls use limited-use App Check tokens, required by functions with replay protection enabled. */
    @JvmField
    public val limitedUseAppCheckTokens: Boolean,
) {
    /** See [limitedUseAppCheckTokens]. */
    public fun getLimitedUseAppCheckTokens(): Boolean = limitedUseAppCheckTokens

    /** Builds [HttpsCallableOptions]; also the receiver of the `init` block of [getHttpsCallable] and [getHttpsCallableFromUrl]. */
    @Suppress("ktlint:standard:class-signature")
    public class Builder() {
        /** Whether the calls use limited-use App Check tokens; `false` by default. */
        @JvmField
        public var limitedUseAppCheckTokens: Boolean = false

        /** See [limitedUseAppCheckTokens]. */
        public fun getLimitedUseAppCheckTokens(): Boolean = limitedUseAppCheckTokens

        /** Sets [limitedUseAppCheckTokens]. */
        public fun setLimitedUseAppCheckTokens(limitedUse: Boolean): Builder {
            limitedUseAppCheckTokens = limitedUse
            return this
        }

        public fun build(): HttpsCallableOptions = HttpsCallableOptions(limitedUseAppCheckTokens)
    }
}
