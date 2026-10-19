/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import dev.gitlive.firebase.storage.externals.FullMetadata
import dev.gitlive.firebase.storage.externals.ListOptions
import dev.gitlive.firebase.storage.externals.SettableMetadata
import dev.gitlive.firebase.storage.externals.StorageError
import dev.gitlive.firebase.storage.externals.deleteObject
import dev.gitlive.firebase.storage.externals.getBytes
import dev.gitlive.firebase.storage.externals.getDownloadURL
import dev.gitlive.firebase.storage.externals.getMetadata
import dev.gitlive.firebase.storage.externals.list
import dev.gitlive.firebase.storage.externals.listAll
import dev.gitlive.firebase.storage.externals.ref
import dev.gitlive.firebase.storage.externals.updateMetadata
import dev.gitlive.firebase.storage.externals.uploadBytesResumable
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import dev.gitlive.firebase.storage.externals.ListResult as JsListResult
import dev.gitlive.firebase.storage.externals.StorageReference as JsStorageReference
import dev.gitlive.firebase.storage.externals.UploadTask as JsUploadTask

/** @property js The underlying Firebase JS SDK object. */
public actual class StorageReference internal constructor(public val js: JsStorageReference) : Comparable<StorageReference> {
    public actual val activeUploadTasks: List<UploadTask> get() = ActiveTasks.uploads.filter { it.reference == this }
    public actual val bucket: String get() = js.bucket
    public actual val name: String get() = js.name
    public actual val parent: StorageReference? get() = js.parent?.let { StorageReference(it) }

    /** With a leading slash, as the Android SDK's `getPath()`. */
    public actual val path: String get() = "/" + js.fullPath

    public actual val root: StorageReference get() = StorageReference(js.root)
    public actual val storage: FirebaseStorage get() = FirebaseStorage(js.storage)

    public actual fun child(pathString: String): StorageReference = rethrow { StorageReference(ref(js, pathString)) }

    actual override fun compareTo(other: StorageReference): Int = toString().compareTo(other.toString())

    public actual fun delete(): Task<Nothing?> = task { deleteObject(js).then { null } }

    public actual fun getBytes(maxDownloadSizeBytes: Long): Task<ByteArray> = task { getBytes(js, maxDownloadSizeBytes.toDouble()).then { Int8Array(it).unsafeCast<ByteArray>() } }

    public actual fun getDownloadUrl(): Task<Any> = task { getDownloadURL(js).then { it } }

    public actual fun getMetadata(): Task<StorageMetadata> = task { getMetadata(js).then { it.toCompat() } }

    public actual fun list(maxResults: Int): Task<ListResult> = task { list(js, json("maxResults" to maxResults).unsafeCast<ListOptions>()).then { it.toCompat() } }

    public actual fun list(maxResults: Int, pageToken: String): Task<ListResult> = task { list(js, json("maxResults" to maxResults, "pageToken" to pageToken).unsafeCast<ListOptions>()).then { it.toCompat() } }

    public actual fun listAll(): Task<ListResult> = task { listAll(js).then { it.toCompat() } }

    public actual fun putBytes(bytes: ByteArray): UploadTask = upload(bytes.toUint8Array(), null)

    public actual fun putBytes(bytes: ByteArray, metadata: StorageMetadata): UploadTask = upload(bytes.toUint8Array(), metadata)

    internal fun upload(data: dynamic, metadata: StorageMetadata?): UploadTask = rethrow {
        UploadTaskImpl(this, JsUploadController(uploadBytesResumable(js, data, metadata?.toJs())))
    }

    public actual fun updateMetadata(metadata: StorageMetadata): Task<StorageMetadata> = task { updateMetadata(js, metadata.toJs().unsafeCast<SettableMetadata>()).then { it.toCompat() } }

    override fun equals(other: Any?): Boolean = other is StorageReference && other.bucket == bucket && other.path == path

    override fun hashCode(): Int = toString().hashCode()

    override fun toString(): String = "gs://$bucket/$path"
}

/** An [UploadController] over the JS SDK's `UploadTask`. */
internal class JsUploadController(private val js: JsUploadTask) : UploadController {
    override val bytesTransferred: Long get() = js.snapshot.bytesTransferred.toLong()
    override val totalByteCount: Long get() = js.snapshot.totalBytes.toLong()
    override val metadata: StorageMetadata? get() = js.snapshot.metadata?.toCompat()

    override fun start(task: UploadTask) {
        js.on(
            "state_changed",
            { snapshot ->
                when (snapshot.state) {
                    "paused" -> task.reportPaused()
                    "running" -> task.reportProgress()
                    "canceled" -> task.reportCanceled()
                    else -> Unit
                }
            },
            { error -> if (error.code == "storage/canceled") task.reportCanceled() else task.reportFailure(error.toStorageException()) },
            { task.reportSuccess() },
        )
    }

    override fun pause(): Boolean = js.pause()

    override fun resume(): Boolean = js.resume()

    override fun cancel(): Boolean = js.cancel()
}

private inline fun <T> task(start: () -> Promise<T>): Task<T> {
    val source = TaskCompletionSource<T>()
    try {
        start().then({ source.setResult(it) }, { source.setException(it.toStorageException()) })
    } catch (e: Throwable) {
        source.setException(e.toStorageException())
    }
    return source.task
}

internal inline fun <R> rethrow(function: () -> R): R = try {
    function()
} catch (e: Throwable) {
    throw e.toStorageException()
}

/** The JS SDK reports `storage/<code>` errors; the codes map onto the Android SDK's `ERROR_*` constants. */
internal fun Throwable.toStorageException(): StorageException {
    if (this is StorageException) return this
    val code = asDynamic().code.unsafeCast<String?>() ?: ""
    return StorageException.of(
        errorCode = code.toErrorCode(),
        cause = this,
        httpResultCode = (asDynamic().status as? Number)?.toInt() ?: -1,
        message = message ?: code,
    )
}

internal fun StorageError.toStorageException(): StorageException = unsafeCast<Throwable>().toStorageException()

private fun String.toErrorCode(): Int = when (substringAfter('/')) {
    "object-not-found" -> StorageException.ERROR_OBJECT_NOT_FOUND
    "bucket-not-found" -> StorageException.ERROR_BUCKET_NOT_FOUND
    "project-not-found" -> StorageException.ERROR_PROJECT_NOT_FOUND
    "quota-exceeded" -> StorageException.ERROR_QUOTA_EXCEEDED
    "unauthenticated" -> StorageException.ERROR_NOT_AUTHENTICATED
    "unauthorized" -> StorageException.ERROR_NOT_AUTHORIZED
    "retry-limit-exceeded" -> StorageException.ERROR_RETRY_LIMIT_EXCEEDED
    "invalid-checksum" -> StorageException.ERROR_INVALID_CHECKSUM
    "canceled" -> StorageException.ERROR_CANCELED
    else -> StorageException.ERROR_UNKNOWN
}

internal fun ByteArray.toUint8Array(): Uint8Array {
    val int8 = unsafeCast<Int8Array>()
    return Uint8Array(int8.buffer, int8.byteOffset, int8.length)
}

internal fun JsListResult.toCompat(): ListResult = ListResult(
    items = items.map { StorageReference(it) },
    prefixes = prefixes.map { StorageReference(it) },
    pageToken = nextPageToken.takeUnless { it.isNullOrEmpty() },
)

internal fun StorageMetadata.toJs(): Json {
    val custom = json()
    customMetadata.forEach { (key, value) -> custom[key] = value }
    return json(
        "cacheControl" to cacheControl,
        "contentDisposition" to contentDisposition,
        "contentEncoding" to contentEncoding,
        "contentLanguage" to contentLanguage,
        "contentType" to contentType,
        "customMetadata" to custom,
    )
}

internal fun FullMetadata.toCompat(): StorageMetadata = StorageMetadata().also { compat ->
    compat.bucketValue = bucket
    compat.cacheControlValue = cacheControl
    compat.contentDispositionValue = contentDisposition
    compat.contentEncodingValue = contentEncoding
    compat.contentLanguageValue = contentLanguage
    compat.contentTypeValue = contentType
    compat.creationTimeMillisValue = timeCreated.toEpochMillis()
    customMetadata?.let { metadata ->
        val objectKeys = js("Object.keys")
        objectKeys(metadata).unsafeCast<Array<String>>().forEach { key -> compat.customMetadata[key] = metadata[key].toString() }
    }
    compat.generationValue = generation
    compat.md5HashValue = md5Hash
    compat.metadataGenerationValue = metageneration
    compat.nameValue = name
    compat.pathValue = fullPath
    compat.referenceValue = ref?.let { StorageReference(it) }
    compat.sizeBytesValue = size.toLong()
    compat.updatedTimeMillisValue = updated.toEpochMillis()
}

private fun String?.toEpochMillis(): Long = this?.let { Date.parse(it).toLong() }?.takeIf { it >= 0 } ?: 0
