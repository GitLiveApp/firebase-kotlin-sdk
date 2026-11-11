/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The deprecated cache API of the Android SDK, mirrored as a view of the cache settings, keeps its state through copies. */
@Suppress("DEPRECATION")
class FirebaseFirestoreSettingsTest {

    @Test
    fun deprecatedCacheApiIsKeptThroughACopy() {
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).setCacheSizeBytes(50L * 1024 * 1024).build()
        assertFalse(settings.isPersistenceEnabled)
        assertEquals(50L * 1024 * 1024, settings.cacheSizeBytes)

        val copy = FirebaseFirestoreSettings.Builder(settings).setCacheSizeBytes(60L * 1024 * 1024).build()
        assertFalse(copy.isPersistenceEnabled)
        assertEquals(60L * 1024 * 1024, copy.cacheSizeBytes)
    }

    @Test
    fun theTwoCacheApisCannotBeMixed() {
        assertFailsWith<IllegalStateException> {
            FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
        }
        assertFailsWith<IllegalStateException> {
            FirebaseFirestoreSettings.Builder().setLocalCacheSettings(PersistentCacheSettings.newBuilder().build()).setCacheSizeBytes(50L * 1024 * 1024)
        }
        val settings = FirebaseFirestoreSettings.Builder().setLocalCacheSettings(PersistentCacheSettings.newBuilder().build()).build()
        assertTrue(settings.isPersistenceEnabled)
        assertFailsWith<IllegalStateException> { FirebaseFirestoreSettings.Builder(settings).setPersistenceEnabled(false) }
    }
}
