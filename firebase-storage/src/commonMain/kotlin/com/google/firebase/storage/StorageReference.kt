/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import com.google.android.gms.tasks.Task

/**
 * A reference to an object or folder in Cloud Storage, as the Android SDK's `StorageReference`. Uploads from a file and
 * downloads to a file are the `putFile` / `getFile` extensions, which take the platform's file type.
 */
public expect class StorageReference : Comparable<StorageReference> {
    /** The uploads to this reference that have not completed. */
    public val activeUploadTasks: List<UploadTask>

    /** The bucket. */
    public val bucket: String

    /** The last path segment. */
    public val name: String

    /** The parent folder, or null at the root. */
    public val parent: StorageReference?

    /** The full path inside the bucket. */
    public val path: String

    /** The root of the bucket. */
    public val root: StorageReference

    /** The [FirebaseStorage] this reference belongs to. */
    public val storage: FirebaseStorage

    /** A reference to [pathString] under this reference. */
    public fun child(pathString: String): StorageReference

    override fun compareTo(other: StorageReference): Int

    /** Deletes the object. */
    public fun delete(): Task<Nothing?>

    /** Downloads the object into memory, failing if it is larger than [maxDownloadSizeBytes]. */
    public fun getBytes(maxDownloadSizeBytes: Long): Task<ByteArray>

    /**
     * The public download URL of the object. The Android SDK's task carries an `android.net.Uri`; here the result is
     * typed as `Any` so that common code can call `toString()` on it (the Uri on Android, the URL string elsewhere).
     */
    public fun getDownloadUrl(): Task<Any>

    /** The object's metadata. */
    public fun getMetadata(): Task<StorageMetadata>

    /** Lists up to [maxResults] items and prefixes under this reference. */
    public fun list(maxResults: Int): Task<ListResult>

    /** Lists the next page of up to [maxResults] items and prefixes, continuing from [pageToken]. */
    public fun list(maxResults: Int, pageToken: String): Task<ListResult>

    /** Lists every item and prefix under this reference. */
    public fun listAll(): Task<ListResult>

    /** Uploads [bytes] to this reference. */
    public fun putBytes(bytes: ByteArray): UploadTask

    /** Uploads [bytes] to this reference with [metadata]. */
    public fun putBytes(bytes: ByteArray, metadata: StorageMetadata): UploadTask

    /** Updates the object's mutable metadata (the settable fields of [metadata]). */
    public fun updateMetadata(metadata: StorageMetadata): Task<StorageMetadata>
}
