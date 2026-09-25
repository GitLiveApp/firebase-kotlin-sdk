/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

/*
 * The listener interfaces and enums of the Android SDK's firebase-firestore, as plain common code: on Android and the
 * JVM they are header stubs that are stripped, so the SDK's own bind.
 */

/** Receives the snapshots of a document or query, or the error that ended the listener. */
public fun interface EventListener<T> {
    public fun onEvent(value: T?, error: FirebaseFirestoreException?)
}

/** A registered snapshot listener; [remove] stops it. */
public fun interface ListenerRegistration {
    public fun remove()
}

/** Receives the progress of a [LoadBundleTask]. */
public fun interface OnProgressListener<ProgressT> {
    public fun onProgress(progress: ProgressT)
}

/** Whether snapshot listeners are notified of metadata-only changes. */
public enum class MetadataChanges {
    EXCLUDE,
    INCLUDE,
}

/** Where a snapshot listener listens: the default (cache then backend) or the local cache only. */
public enum class ListenSource {
    DEFAULT,
    CACHE,
}

/** Where a one-time read reads from. */
public enum class Source {
    DEFAULT,
    SERVER,
    CACHE,
}

/** Where an aggregation is computed. */
public enum class AggregateSource {
    SERVER,
}
