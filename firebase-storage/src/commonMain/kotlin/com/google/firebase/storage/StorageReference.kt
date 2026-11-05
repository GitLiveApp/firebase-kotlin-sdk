/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import android.net.Uri
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.storage.PUT_STREAM_JVM_ONLY

/**
 * A reference to an object or folder in Cloud Storage, as the Android SDK's `StorageReference`. Files are given as
 * [android.net.Uri] or [java.io.File] (the firebase-app stand-ins off Android): a `file:` URL, or a path on Apple
 * platforms. The `putStream` overloads taking a JVM stream are declared through the error-deprecated `java.io.InputStream`
 * stand-in, pointing common code at the kotlinx-io `putStream(RawSource)` extensions (see `StorageSources.kt`).
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

    /** Android/JVM only: uploads a `java.io.InputStream`; common code uploads a kotlinx-io source through `putStream(RawSource)`. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(PUT_STREAM_JVM_ONLY, ReplaceWith("putStream(stream.asSource())", "kotlinx.io.asSource"), DeprecationLevel.ERROR)
    public fun putStream(stream: java.io.InputStream): UploadTask

    /** Android/JVM only: uploads a `java.io.InputStream` with [metadata]; common code uses `putStream(RawSource, StorageMetadata)`. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(PUT_STREAM_JVM_ONLY, ReplaceWith("putStream(stream.asSource(), metadata)", "kotlinx.io.asSource"), DeprecationLevel.ERROR)
    public fun putStream(stream: java.io.InputStream, metadata: StorageMetadata): UploadTask

    /** Updates the object's mutable metadata (the settable fields of [metadata]). */
    public fun updateMetadata(metadata: StorageMetadata): Task<StorageMetadata>

    /** Uploads the file at [uri] to this reference. Fails on JS, whose SDK cannot read a file by its URL. */
    public fun putFile(uri: Uri): UploadTask

    /** Uploads the file at [uri] to this reference with [metadata]. Fails on JS, whose SDK cannot read a file by its URL. */
    public fun putFile(uri: Uri, metadata: StorageMetadata): UploadTask

    /** Downloads the object to the file at [destinationUri]. Fails on JS, whose SDK has no file downloads. */
    public fun getFile(destinationUri: Uri): FileDownloadTask

    /** Downloads the object to [destinationFile]. Fails on JS, whose SDK has no file downloads. */
    public fun getFile(destinationFile: java.io.File): FileDownloadTask

    /** The downloads from this reference that have not completed; always empty on JS. */
    public val activeDownloadTasks: List<FileDownloadTask>
}
