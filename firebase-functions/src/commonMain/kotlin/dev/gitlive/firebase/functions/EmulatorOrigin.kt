/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

/** The host and port of an emulator origin such as `http://10.0.2.2:5001`, as the deprecated `useFunctionsEmulator` takes it. */
internal fun parseEmulatorOrigin(origin: String): Pair<String, Int> {
    val authority = origin.substringAfter("://").substringBefore('/')
    val host = authority.substringBeforeLast(':')
    val port = authority.substringAfterLast(':', "").toIntOrNull() ?: if (origin.startsWith("https")) 443 else 80
    return host to port
}
