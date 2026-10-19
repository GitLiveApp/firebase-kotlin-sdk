/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

// The FileDownloadTask forms of the Android SDK's StorageKt destructuring operators, as plain code that is a header
// stub on Android and the JVM (verified against the SDK's StorageKt facade).

/** The bytes downloaded so far. */
public operator fun FileDownloadTask.TaskSnapshot.component1(): Long = bytesTransferred

/** The total size of the download. */
public operator fun FileDownloadTask.TaskSnapshot.component2(): Long = totalByteCount
