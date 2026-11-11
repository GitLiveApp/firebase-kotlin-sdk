/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EmulatorOriginTest {

    @Test
    fun parsesHostAndPort() {
        assertEquals("10.0.2.2" to 5001, parseEmulatorOrigin("http://10.0.2.2:5001"))
        assertEquals("localhost" to 5001, parseEmulatorOrigin("http://localhost:5001/"))
        assertEquals("localhost" to 80, parseEmulatorOrigin("HTTP://localhost"))
    }

    @Test
    fun keepsBracketedIpv6Hosts() {
        assertEquals("[::1]" to 5001, parseEmulatorOrigin("http://[::1]:5001"))
        assertEquals("[::1]" to 80, parseEmulatorOrigin("http://[::1]"))
    }

    @Test
    fun rejectsOtherSchemesAndBadPorts() {
        assertFailsWith<IllegalArgumentException> { parseEmulatorOrigin("https://localhost:5001") }
        assertFailsWith<IllegalArgumentException> { parseEmulatorOrigin("localhost:5001") }
        assertFailsWith<IllegalArgumentException> { parseEmulatorOrigin("http://localhost:port") }
    }
}
