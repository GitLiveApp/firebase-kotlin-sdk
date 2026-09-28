/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/** Values the server computes on write, as the Android SDK's `ServerValue`; each is written like any other value. */
public expect class ServerValue() {
    public companion object {
        /** The server's time of the write, in milliseconds since the epoch. */
        public val TIMESTAMP: Map<String, String>

        /** Adds [delta] to the current numeric value (or writes it if there is none). */
        public fun increment(delta: Long): Any

        /** Adds [delta] to the current numeric value (or writes it if there is none). */
        public fun increment(delta: Double): Any
    }
}
