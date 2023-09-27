/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlin.js.json
import kotlin.time.Duration.Companion.minutes

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = runTest(timeout = 5.minutes) { test() }

actual fun encodedAsMap(encoded: Any?): Map<String, Any?> {
    return (js("Object").entries(encoded) as Array<Array<Any>>).associate {
        it[0] as String to it[1]
    }
}
actual fun Map<String, Any?>.asEncoded(): Any =
    json(*entries.map { (key, value) -> key to value }.toTypedArray())

