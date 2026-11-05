/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.buffered

// The multiplatform counterpart of the Android SDK's loadBundle(InputStream), shipped on every platform (see
// keepClasses in build.gradle.kts): a kotlinx-io source in place of the JDK stream.

/**
 * Loads a Firestore bundle into the local cache as [bundleData] is read, closing it at the end; the kotlinx-io form of
 * `loadBundle(InputStream)`. On Android and the JVM the SDK reads the source as a stream, on Apple platforms as an
 * `NSInputStream` and on JS through a `ReadableStream`, so a bundle streams on every platform.
 */
public fun FirebaseFirestore.loadBundle(bundleData: RawSource): LoadBundleTask = loadBundleSource(bundleData.buffered())

internal expect fun FirebaseFirestore.loadBundleSource(source: Source): LoadBundleTask
