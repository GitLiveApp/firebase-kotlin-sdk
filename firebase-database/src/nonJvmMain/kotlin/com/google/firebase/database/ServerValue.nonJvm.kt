/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/** The server values as the `.sv` maps the platform SDKs and the backend understand. */
public actual class ServerValue actual constructor() {
    public actual companion object {
        public actual val TIMESTAMP: Map<String, String> = mapOf(".sv" to "timestamp")

        public actual fun increment(delta: Long): Any = mapOf(".sv" to mapOf("increment" to delta))

        public actual fun increment(delta: Double): Any = mapOf(".sv" to mapOf("increment" to delta))
    }
}
