/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("StorageDownloadsKt")

package com.google.firebase.storage

import android.net.Uri
import java.io.File

// Shipped (see keepClasses in the build file): the Any forms of the SDK's file download members, for common code.

/** [destination] is an `android.net.Uri`, a `java.io.File` or a `String` path. */
public actual fun StorageReference.getFile(destination: Any): FileDownloadTask = when (destination) {
    is Uri -> getFile(destination)
    is File -> getFile(destination)
    is String -> getFile(File(destination))
    else -> throw IllegalArgumentException("A destination is an android.net.Uri, a java.io.File or a String path on Android and the JVM, not ${destination::class}")
}

public actual val StorageReference.activeDownloadTasks: List<FileDownloadTask> get() = activeDownloadTasks

public actual var FirebaseStorage.maxDownloadRetryTimeMillis: Long
    get() = maxDownloadRetryTimeMillis
    set(value) {
        maxDownloadRetryTimeMillis = value
    }
