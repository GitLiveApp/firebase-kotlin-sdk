/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

/** [file] is a `File` or `Blob`, or a `String` file URL is not supported (the browser cannot read files by path). */
public actual fun StorageReference.putFile(file: Any): UploadTask = upload(file.asDynamic(), null)

public actual fun StorageReference.putFile(file: Any, metadata: StorageMetadata): UploadTask = upload(file.asDynamic(), metadata)
