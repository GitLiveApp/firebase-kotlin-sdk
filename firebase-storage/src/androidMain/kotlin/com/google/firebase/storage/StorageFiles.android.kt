/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("StorageFilesKt")

package com.google.firebase.storage

import android.net.Uri
import java.io.File

// Shipped (see keepClasses in the build file): the Any forms of the SDK's Uri members, for common code.

/** [file] is an `android.net.Uri`, a `java.io.File` or a `String` URI. */
public actual fun StorageReference.putFile(file: Any): UploadTask = putFile(file.toUri())

public actual fun StorageReference.putFile(file: Any, metadata: StorageMetadata): UploadTask = putFile(file.toUri(), metadata)

internal fun Any.toUri(): Uri = when (this) {
    is Uri -> this
    is File -> Uri.fromFile(this)
    is String -> Uri.parse(this)
    else -> throw IllegalArgumentException("A file is an android.net.Uri, a java.io.File or a String URI on Android and the JVM, not ${this::class}")
}
