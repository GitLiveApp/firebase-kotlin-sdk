package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Returns the [FirebaseStorage] instance of the default [FirebaseApp]. */
expect val Firebase.storage: FirebaseStorage

/** Returns the [FirebaseStorage] instance of the default [FirebaseApp]. */
expect fun Firebase.storage(url: String): FirebaseStorage

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
expect fun Firebase.storage(app: FirebaseApp): FirebaseStorage

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
expect fun Firebase.storage(app: FirebaseApp, url: String): FirebaseStorage

/**
 * FirebaseStorage is a service that supports uploading and downloading large objects to Google
 * Cloud Storage. Pass a custom instance of [FirebaseApp] to [Firebase.storage]
 * which will initialize it with a storage location.
 *
 * Otherwise, if you call [Firebase.storage] without a [FirebaseApp], the
 * [FirebaseStorage] instance will initialize with the default [FirebaseApp] obtainable from
 * [Firebase.storage]. The storage location in this case will come the JSON
 * configuration file downloaded from the web.
 */
expect class FirebaseStorage {
    /**
     * Returns the maximum time to retry operations other than upload and download if a failure
     * occurs.
     *
     * @return the maximum time in milliseconds. Defaults to 2 minutes (120,000 milliseconds).
     */
    val maxOperationRetryTimeMillis: Long

    /**
     * Returns the maximum time to retry an upload if a failure occurs.
     *
     * @return the maximum time in milliseconds. Defaults to 10 minutes (600,000 milliseconds).
     */
    val maxUploadRetryTimeMillis: Long

    /**
     * Sets the maximum time to retry operations other than upload and download if a failure occurs.
     *
     * @param maxTransferRetryMillis the maximum time in milliseconds. Defaults to 2 minutes (120,000
     *     milliseconds).
     */
    fun setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long)

    /**
     * Sets the maximum time to retry an upload if a failure occurs.
     *
     * @param maxTransferRetryMillis the maximum time in milliseconds. Defaults to 10 minutes (600,000
     *     milliseconds).
     */
    fun setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long)

    /**
     * Modifies this FirebaseStorage instance to communicate with the Storage emulator.
     *
     * Note: Call this method before using the instance to do any storage operations.
     *
     * @param host the emulator host (for example, 10.0.2.2)
     * @param port the emulator port (for example, 9000)
     */
    fun useEmulator(host: String, port: Int)

    /**
     * Creates a new [StorageReference] initialized at the root Firebase Storage location.
     *
     * @return An instance of [StorageReference].
     */
    val reference: StorageReference

    /**
     * Creates a new [StorageReference] initialized with a child Firebase Storage location.
     *
     * @param location A relative path from the root to initialize the reference with, for instance
     *     "path/to/object"
     * @return An instance of [StorageReference] at the given child path.
     */
    fun reference(location: String): StorageReference

}

/**
 * Represents a reference to a Google Cloud Storage object. Developers can upload and download
 * objects, get/set object metadata, and delete an object at a specified path.
 */
expect class StorageReference {
    /**
     * Returns the short name of this object.
     *
     * @return the name.
     */
    val name: String

    /**
     * Returns the full path to this object, not including the Google Cloud Storage bucket.
     *
     * @return the path.
     */
    val path: String

    /**
     * Return the Google Cloud Storage bucket that holds this object.
     *
     * @return the bucket.
     */
    val bucket: String

    /**
     * Returns a new instance of [StorageReference] pointing to the parent location or null if
     * this instance references the root location. For example:
     *
     * ```
     * path = foo/bar/baz   parent = foo/bar
     * path = foo           parent = (root)
     * path = (root)        parent = (null)
     * ```
     *
     * @return the parent [StorageReference].
     */
    val parent: StorageReference?

    /**
     * Returns a new instance of {@link StorageReference} pointing to the root location.
     *
     * @return the root {@link StorageReference}.
     */
    val root: StorageReference

    /**
     * Returns the [FirebaseStorage] service which created this reference.
     *
     * @return The [FirebaseStorage] service.
     */
    val storage: FirebaseStorage

    /**
     * Retrieves metadata associated with an object at this [StorageReference].
     *
     * @return the metadata.
     */
    suspend fun getMetadata(): FirebaseStorageMetadata?

    /**
     * Returns a new instance of [StorageReference] pointing to a child location of the current
     * reference. All leading and trailing slashes will be removed, and consecutive slashes will be
     * compressed to single slashes. For example:
     *
     * ```
     * child = /foo/bar     path = foo/bar
     * child = foo/bar/     path = foo/bar
     * child = foo///bar    path = foo/bar
     * ```
     *
     * @param pathString The relative path from this reference.
     * @return the child [StorageReference].
     */
    fun child(path: String): StorageReference

    /**
     * Deletes the object at this {@link StorageReference}.
     *
     * @return A {@link Task} that indicates whether the operation succeeded or failed.
     */
    suspend fun delete()

    /**
     * Asynchronously retrieves a long lived download URL with a revokable token. This can be used to
     * share the file with others, but can be revoked by a developer in the Firebase Console if
     * desired.
     *
     * @return The [Uri] representing the download URL. You can feed this URL into a [URL]
     *     and download the object via URL.openStream().
     */
    suspend fun getDownloadUrl(): String

    /**
     * List all items (files) and prefixes (folders) under this StorageReference.
     *
     * This is a helper method for calling list() repeatedly until there are no more
     * results. Consistency of the result is not guaranteed if objects are inserted or removed while
     * this operation is executing.
     *
     * [listAll] is only available for projects using Firebase Rules Version 2.
     *
     * @throws OutOfMemoryError If there are too many items at this location.
     * @return A [ListResult] that returns all items and prefixes under the current StorageReference.
     */
    suspend fun listAll(): ListResult

    /**
     * Asynchronously uploads from a content URI to this [StorageReference].
     *
     * @param file The source of the upload. This is a [File]. A content
     *     resolver will be used to load the data.
     * @param metadata [FirebaseStorageMetadata] containing additional information (MIME type, etc.)
     *     about the object being uploaded.
     */
    suspend fun putFile(file: File, metadata: FirebaseStorageMetadata? = null)

    /**
     * Asynchronously uploads byte data to this [StorageReference]. This is not recommended for
     * large files. Instead upload a file via [putFile].
     *
     * @param data The [Data] to upload.
     * @param metadata [FirebaseStorageMetadata] containing additional information (MIME type, etc.)
     *     about the object being uploaded.
     */
    suspend fun putData(data: Data, metadata: FirebaseStorageMetadata? = null)

    /**
     * Asynchronously uploads from a content URI to this [StorageReference].
     *
     * @param file The source of the upload. This is a [File]. A content
     *     resolver will be used to load the data.
     * @param metadata [FirebaseStorageMetadata] containing additional information (MIME type, etc.)
     *     about the object being uploaded.
     * @return A [ProgressFlow] that can be used to monitor and manage the upload.
     */
    fun putFileResumable(file: File, metadata: FirebaseStorageMetadata? = null): ProgressFlow
}

expect class ListResult {
    val prefixes: List<StorageReference>
    val items: List<StorageReference>
    val pageToken: String?
}

/**
 * Represents a reference to a local file for all platforms. Every platform has its own constructor.
 */
expect class File

/**
 * Represents a reference to data for all platforms. Every platform has its own constructor.
 */
expect class Data

/**
 * Represents the progress of an operation.
 */
sealed class Progress(val bytesTransferred: Number, val totalByteCount: Number) {
    /** Represents the progress of an operation that is still running. */
    class Running internal constructor(bytesTransferred: Number, totalByteCount: Number): Progress(bytesTransferred, totalByteCount)
    /** Represents the progress of an operation that is paused. */
    class Paused internal constructor(bytesTransferred: Number, totalByteCount: Number): Progress(bytesTransferred, totalByteCount)
}

/**
 * A flow that emits [Progress] objects containing the state of an upload.
 */
interface ProgressFlow : Flow<Progress> {
    fun pause()
    fun resume()
    fun cancel()
}

/**
 * Exception that gets thrown when an operation on Firebase Storage fails.
 */
expect class FirebaseStorageException : FirebaseException

/**
 * Metadata for a [StorageReference]. Metadata stores default attributes such as size and
 * content type. You may also store custom metadata key value pairs. Metadata values may be used to
 * authorize operations using declarative validation rules.
 */
data class FirebaseStorageMetadata(
    /**
     * Returns the path of the [StorageReference] object.
     *
     * @return the MD5Hash of the [StorageReference] object
     */
    var md5Hash: String? = null,

    /**
     * Returns the size of the [StorageReference] object in bytes.
     *
     * @return the Cache Control header for the [StorageReference]
     */
    var cacheControl: String? = null,

    /**
     * Returns the content disposition of the [StorageReference]
     *
     * @return the content disposition of the [StorageReference]
     */
    var contentDisposition: String? = null,

    /**
     * Returns the content encoding for the [StorageReference]
     *
     * @return the content encoding for the [StorageReference]
     */
    var contentEncoding: String? = null,

    /**
     * Returns the content language for the [StorageReference]
     *
     * @return the content language for the [StorageReference]
     */
    var contentLanguage: String? = null,

    /**
     * Returns the Content Type of this associated [StorageReference]
     *
     * @return the Content Type of this associated [StorageReference]
     */
    var contentType: String? = null,

    /**
     * Returns custom metadata for a StorageReference
     *
     * @return the metadata stored in the object.
     */
    var customMetadata: MutableMap<String, String> = mutableMapOf()
) {
    /**
     * Sets custom metadata
     *
     * @param key the key of the new value
     * @param value the value to set.
     */
    fun setCustomMetadata(key: String, value: String?) {
        value?.let {
            customMetadata[key] = it
        }
    }
}

/** Returns a [FirebaseStorageMetadata] object initialized using the [init] function. */
fun storageMetadata(init: FirebaseStorageMetadata.() -> Unit): FirebaseStorageMetadata {
    val metadata = FirebaseStorageMetadata()
    metadata.init()
    return metadata
}