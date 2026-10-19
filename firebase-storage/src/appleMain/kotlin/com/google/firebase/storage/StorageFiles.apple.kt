/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import platform.Foundation.NSURL

/** [file] is an `NSURL` or a `String` path. */
public actual fun StorageReference.putFile(file: Any): UploadTask = putFileUrl(file.toFileUrl(), null)

public actual fun StorageReference.putFile(file: Any, metadata: StorageMetadata): UploadTask = putFileUrl(file.toFileUrl(), metadata)

internal fun Any.toFileUrl(): NSURL = when (this) {
    is NSURL -> this
    is String -> NSURL.fileURLWithPath(this)
    else -> throw IllegalArgumentException("A file is an NSURL or a String path on Apple platforms, not ${this::class}")
}
