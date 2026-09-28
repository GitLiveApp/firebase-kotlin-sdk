/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect

import com.google.firebase.dataconnect.ConnectorConfig

/** The host the Data Connect emulator is reached at from the platform under test. */
expect val emulatorHost: String

expect val context: Any

/** The emulator tests need a Firebase app, which Android unit tests cannot initialise. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class IgnoreForAndroidUnitTest()

/** The connector of the test project under `test/dataconnect`. */
val testConnector = ConnectorConfig(connector = "kotlin", location = "us-central1", serviceId = "kotlin-sdk-test")
