/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path

// The multiplatform counterparts of the Android SDK's members taking JDK types, shipped on every platform (see
// keepClasses in build.gradle.kts): a kotlinx-io source in place of the JDK stream, a kotlinx-io path in place of the file.

/**
 * Uploads the bytes of [source] to this reference, closing it; the kotlinx-io form of `putStream(InputStream)`. On
 * Android and the JVM the SDK streams the source as it uploads; the Apple and JS SDKs upload whole buffers, so there
 * the source is read to its end first (off the caller's thread on Apple platforms, at once on JS).
 */
public fun StorageReference.putStream(source: RawSource): UploadTask = putSource(source.buffered(), null)

/** Uploads the bytes of [source] to this reference with [metadata]; see [putStream]. */
public fun StorageReference.putStream(source: RawSource, metadata: StorageMetadata): UploadTask = putSource(source.buffered(), metadata)

internal expect fun StorageReference.putSource(source: Source, metadata: StorageMetadata?): UploadTask

/**
 * Downloads the object to the file at [destination]; the kotlinx-io form of `getFile(File)`. Fails on JS, whose SDK has
 * no file downloads (behind `@UnsupportedOnJs` there).
 */
public expect fun StorageReference.getFile(destination: Path): FileDownloadTask
