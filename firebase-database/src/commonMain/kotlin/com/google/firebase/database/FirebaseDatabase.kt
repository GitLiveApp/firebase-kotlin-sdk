/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.firebase.FirebaseApp

/**
 * The entry point of the Realtime Database, as the Android SDK's `com.google.firebase.database.FirebaseDatabase`.
 * Persistence settings are accepted on every platform (the JS SDK has no disk persistence, so they are ignored there).
 */
public expect class FirebaseDatabase {
    /** The [FirebaseApp] this instance belongs to. */
    public val app: FirebaseApp

    /** A reference to the root of the database. */
    public val reference: DatabaseReference

    /** A reference to [path]. */
    public fun getReference(path: String): DatabaseReference

    /** A reference to the location of [url], which must be in this database. */
    public fun getReferenceFromUrl(url: String): DatabaseReference

    /** Disconnects from the backend until [goOnline]. */
    public fun goOffline()

    /** Reconnects to the backend after [goOffline]. */
    public fun goOnline()

    /** Rolls back every outstanding write, raising the events of the reverted data. */
    public fun purgeOutstandingWrites()

    /** Sets the log level of the SDK. */
    public fun setLogLevel(logLevel: Logger.Level)

    /** Sets the size of the on-disk cache in bytes; must be called before the first reference is created. */
    public fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long)

    /** Enables or disables persisting synchronized data to disk; must be called before the first reference is created. */
    public fun setPersistenceEnabled(isEnabled: Boolean)

    /** Routes requests to the Realtime Database emulator at [host]:[port]; must be called before any other operation. */
    public fun useEmulator(host: String, port: Int)

    public companion object {
        /** The [FirebaseDatabase] of the default [FirebaseApp]. */
        public fun getInstance(): FirebaseDatabase

        /** The [FirebaseDatabase] of [app]. */
        public fun getInstance(app: FirebaseApp): FirebaseDatabase

        /** The [FirebaseDatabase] of [app] at [url]. */
        public fun getInstance(app: FirebaseApp, url: String): FirebaseDatabase

        /** The [FirebaseDatabase] of the default [FirebaseApp] at [url]. */
        public fun getInstance(url: String): FirebaseDatabase

        /** The version of the SDK. */
        public fun getSdkVersion(): String
    }
}
