/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION_ERROR")

package com.google.firebase.storage

import dev.gitlive.firebase.storage.STREAM_DOWNLOAD_JVM_ONLY

/**
 * Android/JVM only: the Android SDK's download to a `java.io.InputStream`, returned by `StorageReference.getStream`. Declared
 * so that common code referring to it fails with a message naming the multiplatform downloads, rather than with an unresolved
 * reference; on Android and the JVM the SDK's class is used (see buildSrc utils/HeaderStubs.kt). Common code downloads with
 * `getBytes`, `getFile(Path)` or `getFile(Uri)`.
 */
@Deprecated(STREAM_DOWNLOAD_JVM_ONLY, level = DeprecationLevel.ERROR)
public abstract class StreamDownloadTask private constructor() : StorageTask<StreamDownloadTask.TaskSnapshot>() {
    /** Processes the stream of a download on a background thread. */
    public fun interface StreamProcessor {
        public fun doInBackground(state: TaskSnapshot, stream: java.io.InputStream)
    }

    /** The state of a stream download. */
    public inner class TaskSnapshot private constructor() : StorageTask<TaskSnapshot>.SnapshotBase(null) {
        public val bytesTransferred: Long get() = throw UnsupportedOperationException(STREAM_DOWNLOAD_JVM_ONLY)
        public val totalByteCount: Long get() = throw UnsupportedOperationException(STREAM_DOWNLOAD_JVM_ONLY)
        public val stream: java.io.InputStream get() = throw UnsupportedOperationException(STREAM_DOWNLOAD_JVM_ONLY)
    }
}
