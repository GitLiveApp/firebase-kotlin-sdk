/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.json
import kotlin.test.assertEquals
import kotlin.test.assertTrue

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = GlobalScope
    .promise {
        try {
            test()
        } catch (e: dynamic) {
            (e as? Throwable)?.log()
            throw e
        }
    }.asDynamic()

actual fun encodedAsMap(encoded: Any?): Map<String, Any?> {
    return (js("Object").entries(encoded) as Array<Array<Any>>).associate {
        it[0] as String to it[1]
    }
}
actual fun mapAsEncoded(map: Map<String, Any?>): Any = json(
    *(map.map { (key, value) -> key to value}.toTypedArray())
)

internal fun Throwable.log() {
    console.error(this)
    cause?.let {
        console.error("Caused by:")
        it.log()
    }
}
