/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import com.google.firebase.FirebaseApp

/**
 * Cloud Storage for Firebase, as the Android SDK's `com.google.firebase.storage.FirebaseStorage`. The download retry
 * time, file downloads and `FileDownloadTask` exist on Android, the JVM and Apple platforms (nonJsMain); the JS SDK
 * downloads through URLs only.
 */
public expect class FirebaseStorage {
    /** The [FirebaseApp] this instance belongs to. */
    public val app: FirebaseApp

    /** The maximum time to retry operations other than uploads and downloads, in milliseconds. */
    public var maxOperationRetryTimeMillis: Long

    /** The maximum time to retry uploads, in milliseconds. */
    public var maxUploadRetryTimeMillis: Long

    /** A reference to the root of the default bucket. */
    public val reference: StorageReference

    /** A reference to [location], a path in the default bucket. */
    public fun getReference(location: String): StorageReference

    /** A reference for a `gs://` or `https://` URL of an object in this instance's bucket. */
    public fun getReferenceFromUrl(fullUrl: String): StorageReference

    /** Routes requests to the Storage emulator at [host]:[port]. */
    public fun useEmulator(host: String, port: Int)

    public companion object {
        /** The [FirebaseStorage] of the default [FirebaseApp] and its default bucket. */
        public fun getInstance(): FirebaseStorage

        /** The [FirebaseStorage] of the default [FirebaseApp] for the bucket of [url] (`gs://bucket`). */
        public fun getInstance(url: String): FirebaseStorage

        /** The [FirebaseStorage] of [app] and its default bucket. */
        public fun getInstance(app: FirebaseApp): FirebaseStorage

        /** The [FirebaseStorage] of [app] for the bucket of [url] (`gs://bucket`). */
        public fun getInstance(app: FirebaseApp, url: String): FirebaseStorage
    }
}
