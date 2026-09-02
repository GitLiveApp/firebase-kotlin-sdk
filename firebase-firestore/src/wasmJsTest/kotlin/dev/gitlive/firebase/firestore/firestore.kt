/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.externals.asJsAny
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.jsObjectEntries
import dev.gitlive.firebase.externals.toKotlin

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun encodedAsMap(encoded: Any?): Map<String, Any?> {
    val entries = jsObjectEntries(encoded!!.asJsAny())
    return buildMap {
        for (index in 0 until entries.length) {
            val entry = entries[index]!!
            put(entry[0].toKotlin() as String, entry[1].toKotlin())
        }
    }
}

actual fun Map<String, Any?>.asEncoded(): Any = json(*entries.map { (key, value) -> key to value }.toTypedArray())
