/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

// Deprecation messages of the com.google.firebase.storage members that only Android and the JVM can call; kept out of the
// stripped package, where a constant would be verified against the SDK's own facade.

internal const val GET_FILE_JVM_ONLY = "getFile(java.io.File) is Android/JVM-only; use getFile(Path) (kotlinx-io), on the JVM with Path(destinationFile.path), or getFile(Uri)"
internal const val MAX_CHUNK_UPLOAD_RETRY_ANDROID_ONLY =
    "The time limit for retrying a failed chunk of an upload is Android-only: the Firebase iOS and JS SDKs have no such setting " +
        "(maxUploadRetryTimeMillis, the limit for the whole upload, is available on every platform); a call belongs in androidMain"
internal const val RESUME_UPLOAD_ANDROID_ONLY =
    "Resuming an upload session by its URL is Android-only: the Firebase iOS and JS SDKs cannot resume a session; " +
        "putFile(uri, metadata) or putBytes start a new upload"
internal const val PUT_STREAM_JVM_ONLY = "putStream(java.io.InputStream) is Android/JVM-only; use putStream(RawSource) (kotlinx-io), on the JVM with stream.asSource()"

internal const val STREAM_DOWNLOAD_JVM_ONLY =
    "StreamDownloadTask streams the object to a java.io.InputStream, which is JVM-only; common code downloads with getBytes(maxDownloadSizeBytes), " +
        "getFile(Path) (kotlinx-io) or getFile(Uri)"

// The Activity and Executor overloads of the task listeners, which only Android and the JVM can call (android.app.Activity and
// java.util.concurrent.Executor are error-deprecated stand-ins in common code).
internal const val TASK_LISTENER_EXECUTOR_JVM_ONLY =
    "The Executor overloads of the task listeners are JVM-only; the multiplatform task calls its listeners on the thread that progresses it: " +
        "use the overload without an Executor and dispatch from the listener"
internal const val TASK_LISTENER_ACTIVITY_ANDROID_ONLY =
    "The Activity overloads of the task listeners are Android-only, where the listener is dropped when the Activity stops; " +
        "use the overload without an Activity and remove the listener yourself"

internal const val FROM_ERROR_STATUS_ANDROID_ONLY =
    "fromErrorStatus(Status) takes a Play Services Status, which is Android-only; use fromException or fromExceptionAndHttpCode"
