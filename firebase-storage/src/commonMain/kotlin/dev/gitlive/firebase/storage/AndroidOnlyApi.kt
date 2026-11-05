/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

// Deprecation messages of the com.google.firebase.storage members that only Android and the JVM can call; kept out of the
// stripped package, where a constant would be verified against the SDK's own facade.

internal const val GET_FILE_JVM_ONLY = "getFile(java.io.File) is Android/JVM-only; use getFile(Path) (kotlinx-io), on the JVM with Path(destinationFile.path), or getFile(Uri)"
internal const val PUT_STREAM_JVM_ONLY = "putStream(java.io.InputStream) is Android/JVM-only; use putStream(RawSource) (kotlinx-io), on the JVM with stream.asSource()"
