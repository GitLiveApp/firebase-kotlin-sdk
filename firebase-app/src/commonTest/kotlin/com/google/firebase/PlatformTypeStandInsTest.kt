/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import android.net.Uri
import dev.gitlive.firebase.IgnoreForAndroidUnitTest
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The `java.net.URL` and `android.net.Uri` stand-ins carry their string: on Android and the JVM these are the platform's
 * own classes, elsewhere the library's, and the same code must read the same on each. Android unit tests run against the
 * mock `android.jar`, whose `Uri.parse` throws, so the `Uri` test runs there as an instrumented test only.
 */
class PlatformTypeStandInsTest {

    @Test
    fun urlCarriesItsString() {
        val url = URL("https://example.com/path?q=1#top")
        assertEquals("https://example.com/path?q=1#top", url.toString())
        assertEquals("https://example.com/path?q=1#top", url.toExternalForm())
    }

    @Test
    @IgnoreForAndroidUnitTest
    fun uriCarriesItsString() {
        val uri = Uri.parse("content://media/external/images/1")
        assertEquals("content://media/external/images/1", uri.toString())
        assertEquals(Uri.parse("content://media/external/images/1"), uri)
        assertEquals(Uri.parse("content://media/external/images/1").hashCode(), uri.hashCode())
    }
}
