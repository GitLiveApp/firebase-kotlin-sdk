/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import dev.gitlive.firebase.externals.database.getDatabase
import dev.gitlive.firebase.externals.database.goOffline
import dev.gitlive.firebase.externals.database.goOnline
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = GlobalScope
    .promise {
        try {
            val db = getDatabase()
            goOnline(db)
            test()
            goOffline(db) // infinitely running test task workaround
        } catch (e: Throwable) {
            e.log()
            throw e
        }
    }.asDynamic()

internal fun Throwable.log() {
    console.error(this)
    cause?.let {
        console.error("Caused by:")
        it.log()
    }
}
