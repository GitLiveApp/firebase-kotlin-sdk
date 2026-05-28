/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")

package dev.gitlive.firebase.database

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import org.junit.Ignore

actual val emulatorHost: String = "10.0.2.2"

actual val context: Any = InstrumentationRegistry.getInstrumentation().targetContext

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest

actual typealias IgnoreForAndroidTest = Ignore

actual suspend fun FirebaseDatabase.verifyPurgeOutstandingWrites() {
    goOffline()
    purgeOutstandingWrites()
    goOnline()
    // Android SDK only re-establishes the connection once there is an active server listen
    // Attach one to force reconnection, otherwise the subsequent ensureDatabaseConnected() hangs
    reference("testPurgeOutstandingWrites").valueEvents.first()
}
