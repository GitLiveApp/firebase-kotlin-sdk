/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.database.externals.Database
import dev.gitlive.firebase.database.externals.SDK_VERSION
import dev.gitlive.firebase.database.externals.connectDatabaseEmulator
import dev.gitlive.firebase.database.externals.enableLogging
import dev.gitlive.firebase.database.externals.getDatabase
import dev.gitlive.firebase.database.externals.ref
import dev.gitlive.firebase.database.externals.refFromURL
import dev.gitlive.firebase.database.externals.goOffline as jsGoOffline
import dev.gitlive.firebase.database.externals.goOnline as jsGoOnline

/** @property js The underlying Firebase JS SDK object. */
public actual class FirebaseDatabase internal constructor(public val js: Database) {
    public actual val app: FirebaseApp get() = FirebaseApp.getInstance(js.app.name)

    public actual val reference: DatabaseReference get() = rethrow { DatabaseReference(ref(js), js) }

    public actual fun getReference(path: String): DatabaseReference = rethrow { DatabaseReference(ref(js, path), js) }

    public actual fun getReferenceFromUrl(url: String): DatabaseReference = rethrow { DatabaseReference(refFromURL(js, url), js) }

    public actual fun goOffline() {
        rethrow { jsGoOffline(js) }
    }

    public actual fun goOnline() {
        rethrow { jsGoOnline(js) }
    }

    /** No-op: the JS SDK has no write queue to purge. */
    public actual fun purgeOutstandingWrites() {}

    /** The JS SDK only has a switch: [Logger.Level.DEBUG] enables its logging, every other level disables it. */
    public actual fun setLogLevel(logLevel: Logger.Level) {
        rethrow { enableLogging(logLevel == Logger.Level.DEBUG) }
    }

    /** No-op: the JS SDK has no disk persistence. */
    public actual fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long) {}

    /** No-op: the JS SDK has no disk persistence. */
    public actual fun setPersistenceEnabled(isEnabled: Boolean) {}

    public actual fun useEmulator(host: String, port: Int) {
        rethrow { connectDatabaseEmulator(js, host, port) }
    }

    override fun equals(other: Any?): Boolean = other is FirebaseDatabase && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "FirebaseDatabase($js)"

    public actual companion object {
        public actual fun getInstance(): FirebaseDatabase = rethrow { FirebaseDatabase(getDatabase()) }

        public actual fun getInstance(app: FirebaseApp): FirebaseDatabase = rethrow { FirebaseDatabase(getDatabase(app.js)) }

        public actual fun getInstance(app: FirebaseApp, url: String): FirebaseDatabase = rethrow { FirebaseDatabase(getDatabase(app.js, url)) }

        public actual fun getInstance(url: String): FirebaseDatabase = rethrow { FirebaseDatabase(getDatabase(url = url)) }

        public actual fun getSdkVersion(): String = SDK_VERSION
    }
}
