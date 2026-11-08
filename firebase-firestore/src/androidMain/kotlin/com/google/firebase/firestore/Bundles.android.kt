/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import kotlinx.io.Source
import kotlinx.io.asInputStream

/** The SDK's own stream form, which reads the source as it parses. Shipped (see keepClasses in build.gradle.kts). */
@Suppress("DEPRECATION_ERROR")
internal actual fun FirebaseFirestore.loadBundleSource(source: Source): LoadBundleTask = loadBundle(source.asInputStream())
