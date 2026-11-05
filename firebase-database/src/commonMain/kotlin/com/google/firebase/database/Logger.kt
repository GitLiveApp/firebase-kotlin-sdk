/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/** The logging of the Realtime Database, as the Android SDK's `Logger`; [Level] is passed to [FirebaseDatabase.setLogLevel]. */
public interface Logger {
    /** The log levels, most verbose first. */
    public enum class Level {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        NONE,
    }
}
