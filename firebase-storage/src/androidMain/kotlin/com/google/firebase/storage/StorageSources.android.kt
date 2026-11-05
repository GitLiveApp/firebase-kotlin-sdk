/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import kotlinx.io.Source
import kotlinx.io.asInputStream
import java.io.File
import kotlinx.io.files.Path

/** The SDK's own stream forms, which upload the source as they read it. Shipped (see keepClasses in build.gradle.kts). */
@Suppress("DEPRECATION_ERROR")
internal actual fun StorageReference.putSource(source: Source, metadata: StorageMetadata?): UploadTask = if (metadata == null) putStream(source.asInputStream()) else putStream(source.asInputStream(), metadata)

/** The SDK's own file form. Shipped (see keepClasses in build.gradle.kts). */
@Suppress("DEPRECATION_ERROR")
public actual fun StorageReference.getFile(destination: Path): FileDownloadTask = getFile(File(destination.toString()))
