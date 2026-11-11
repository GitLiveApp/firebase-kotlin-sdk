/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

/**
 * The host and port of an emulator origin such as `http://10.0.2.2:5001` or `http://[::1]:5001`, as the deprecated
 * `useFunctionsEmulator` takes it. Only `http` origins are accepted: `useEmulator(host, port)`, which the platforms configure
 * from, speaks plain HTTP to the emulator, so an `https` origin would silently change scheme.
 */
internal fun parseEmulatorOrigin(origin: String): Pair<String, Int> {
    require(origin.startsWith("http://", ignoreCase = true)) { "useFunctionsEmulator takes an http:// origin ($origin); the emulator serves plain HTTP, which useEmulator(host, port) configures" }
    val authority = origin.substring("http://".length).substringBefore('/')
    // an IPv6 host is bracketed, and its colons are not a port separator
    val hostEnd = if (authority.startsWith("[")) authority.indexOf(']') + 1 else authority.indexOf(':').let { if (it < 0) authority.length else it }
    require(hostEnd > 0) { "useFunctionsEmulator: no host in $origin" }
    val host = authority.substring(0, hostEnd)
    val port = authority.substring(hostEnd).removePrefix(":").ifEmpty { "80" }.toIntOrNull()
    require(port != null) { "useFunctionsEmulator: invalid port in $origin" }
    return host to port
}
