/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/*
 * The Kotlin extensions of the Android SDK's firebase-storage (StorageKt), as plain common code: on Android and the
 * JVM the facade is a header stub that is stripped, so the SDK's own facade binds. The FileDownloadTask forms are in
 * nonJsMain (StorageDownloads.kt).
 */

/** The [FirebaseStorage] of the default [FirebaseApp]; the Android SDK's `Firebase.storage`. */
public val Firebase.storage: FirebaseStorage
    get() = FirebaseStorage.getInstance()

/** The [FirebaseStorage] of the default [FirebaseApp] for the bucket of [url]. */
public fun Firebase.storage(url: String): FirebaseStorage = FirebaseStorage.getInstance(url)

/** The [FirebaseStorage] of [app]. */
public fun Firebase.storage(app: FirebaseApp): FirebaseStorage = FirebaseStorage.getInstance(app)

/** The [FirebaseStorage] of [app] for the bucket of [url]. */
public fun Firebase.storage(app: FirebaseApp, url: String): FirebaseStorage = FirebaseStorage.getInstance(app, url)

/** Builds [StorageMetadata] with [init] applied to a [StorageMetadata.Builder]. */
public fun storageMetadata(init: StorageMetadata.Builder.() -> Unit): StorageMetadata = StorageMetadata.Builder().apply(init).build()

/** The bytes uploaded so far. */
public operator fun UploadTask.TaskSnapshot.component1(): Long = bytesTransferred

/** The total size of the upload. */
public operator fun UploadTask.TaskSnapshot.component2(): Long = totalByteCount

/** The metadata of the uploaded object, once complete. */
public operator fun UploadTask.TaskSnapshot.component3(): StorageMetadata? = metadata

/** The items of the page. */
public operator fun ListResult.component1(): List<StorageReference> = items

/** The prefixes of the page. */
public operator fun ListResult.component2(): List<StorageReference> = prefixes

/** The token of the next page. */
public operator fun ListResult.component3(): String? = pageToken

/** The progress and pauses of this task as a [Flow], which completes with the task. */
public val <T : StorageTask.ProvideError> StorageTask<T>.taskState: Flow<TaskState<T>>
    get() = callbackFlow {
        val progressListener = OnProgressListener<T> { trySend(TaskState.InProgress(it)) }
        val pausedListener = OnPausedListener<T> { trySend(TaskState.Paused(it)) }
        addOnProgressListener(progressListener)
        addOnPausedListener(pausedListener)
        addOnCompleteListener { close(it.exception) }
        awaitClose {
            removeOnProgressListener(progressListener)
            removeOnPausedListener(pausedListener)
        }
    }
