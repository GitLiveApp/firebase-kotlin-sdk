/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import dev.gitlive.firebase.UnsupportedOnJs
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsObjectKeys
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.parseDateStringToMillis
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.externals.toList
import dev.gitlive.firebase.storage.externals.FullMetadata
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
import org.khronos.webgl.toByteArray
import org.khronos.webgl.toInt8Array
import kotlin.js.JsException
import kotlin.js.Promise
import dev.gitlive.firebase.storage.externals.ListResult as JsListResult
import dev.gitlive.firebase.storage.externals.StorageReference as JsStorageReference
import dev.gitlive.firebase.storage.externals.UploadTask as JsUploadTask
import kotlinx.io.Source
import dev.gitlive.firebase.storage.PUT_STREAM_JVM_ONLY

/** @property js The underlying Firebase JS SDK object. */
public actual class StorageReference internal constructor(public val js: JsStorageReference) : Comparable<StorageReference> {
    public actual val activeUploadTasks: List<UploadTask> get() = ActiveTasks.uploads.filter { it.reference.isUnder(this) }
    public actual val bucket: String get() = js.bucket
    public actual val name: String get() = js.name
    public actual val parent: StorageReference? get() = js.parent?.let { StorageReference(it) }

    /** With a leading slash, as the Android SDK's `getPath()`. */
    public actual val path: String get() = "/" + js.fullPath
    public actual val root: StorageReference get() = StorageReference(js.root)
    public actual val storage: FirebaseStorage get() = FirebaseStorage(js.storage)

    public actual fun child(pathString: String): StorageReference = rethrow { StorageReference(ref(js, pathString)) }

    actual override fun compareTo(other: StorageReference): Int = toString().compareTo(other.toString())

    public actual fun delete(): Task<Nothing?> = task({ deleteObject(js) }) { null }

    public actual fun getBytes(maxDownloadSizeBytes: Long): Task<ByteArray> = task({ getBytes(js, maxDownloadSizeBytes.toDouble()) }) { Int8Array(it).toByteArray() }

    public actual fun getDownloadUrl(): Task<Any> = task({ getDownloadURL(js) }) { it.toKotlinString() }

    public actual fun getMetadata(): Task<StorageMetadata> = task({ getMetadata(js) }) { it.toCompat() }

    public actual fun list(maxResults: Int): Task<ListResult> = task({ list(js, json("maxResults" to maxResults)) }) { it.toCompat() }

    public actual fun list(maxResults: Int, pageToken: String): Task<ListResult> = task({ list(js, json("maxResults" to maxResults, "pageToken" to pageToken)) }) { it.toCompat() }

    public actual fun listAll(): Task<ListResult> = task({ listAll(js) }) { it.toCompat() }

    public actual fun putBytes(bytes: ByteArray): UploadTask = upload(bytes.toUint8Array(), null)

    public actual fun putBytes(bytes: ByteArray, metadata: StorageMetadata): UploadTask = upload(bytes.toUint8Array(), metadata)

    internal fun upload(data: JsAny?, metadata: StorageMetadata?): UploadTask = rethrow { UploadTaskImpl(this, uploadController(data, metadata)) }

    private fun uploadController(data: JsAny?, metadata: StorageMetadata?): UploadController = WasmUploadController(uploadBytesResumable(js, data, metadata?.toJs()))

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(PUT_STREAM_JVM_ONLY, ReplaceWith("putStream(stream.asSource())", "kotlinx.io.asSource"), DeprecationLevel.ERROR)
    public actual fun putStream(stream: java.io.InputStream): UploadTask = throw UnsupportedOperationException(PUT_STREAM_JVM_ONLY)

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(PUT_STREAM_JVM_ONLY, ReplaceWith("putStream(stream.asSource(), metadata)", "kotlinx.io.asSource"), DeprecationLevel.ERROR)
    public actual fun putStream(stream: java.io.InputStream, metadata: StorageMetadata): UploadTask = throw UnsupportedOperationException(PUT_STREAM_JVM_ONLY)

    /** The source is read to its end at once (JS has no other thread to read it on), then uploaded as bytes. */
    internal fun putSourceBytes(source: Source, metadata: StorageMetadata?): UploadTask = UploadTaskImpl(this, SourceUploadController(source) { bytes -> rethrow { uploadController(bytes.toUint8Array(), metadata) } })

    public actual fun updateMetadata(metadata: StorageMetadata): Task<StorageMetadata> = task({ updateMetadata(js, metadata.toJs()) }) { it.toCompat() }

    /** Fails: the JS SDK cannot read a file by its URL; use [putBytes], or the dev.gitlive wrapper's `putFile` with a browser `File`. */
    @UnsupportedOnJs
    public actual fun putFile(uri: Uri): UploadTask = failedFileUpload()

    /** Fails: the JS SDK cannot read a file by its URL; use [putBytes], or the dev.gitlive wrapper's `putFile` with a browser `File`. */
    @UnsupportedOnJs
    public actual fun putFile(uri: Uri, metadata: StorageMetadata): UploadTask = failedFileUpload()

    /** Fails: the JS SDK downloads through URLs, not to files; use [getBytes] or [getDownloadUrl]. */
    @UnsupportedOnJs
    public actual fun getFile(destinationUri: Uri): FileDownloadTask = FileDownloadTaskImpl(this)

    /** Fails: the JS SDK downloads through URLs, not to files; use [getBytes] or [getDownloadUrl]. */
    @UnsupportedOnJs
    public actual fun getFile(destinationFile: java.io.File): FileDownloadTask = FileDownloadTaskImpl(this)

    /** Always empty: the JS SDK has no file downloads. */
    @UnsupportedOnJs
    public actual val activeDownloadTasks: List<FileDownloadTask> get() = emptyList()

    override fun equals(other: Any?): Boolean = other is StorageReference && other.bucket == bucket && other.path == path

    override fun hashCode(): Int = toString().hashCode()

    override fun toString(): String = "gs://$bucket$path"
}

/** An [UploadController] over the JS SDK's `UploadTask`. */
internal class WasmUploadController(private val js: JsUploadTask) : UploadController {
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
            { error -> if (error.code() == "storage/canceled") task.reportCanceled() else task.reportFailure(error.toStorageException()) },
            { task.reportSuccess() },
        )
    }

    override fun pause(): Boolean = js.pause()

    override fun resume(): Boolean = js.resume()

    override fun cancel(): Boolean = js.cancel()
}

private inline fun <T : JsAny?, R> task(start: () -> Promise<T>, crossinline result: (T) -> R): Task<R> {
    val source = TaskCompletionSource<R>()
    try {
        start().then(
            onFulfilled = {
                source.setResult(result(it))
                null
            },
            onRejected = {
                source.setException(it.toStorageException())
                null
            },
        )
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

private fun JsAny.code(): String? = jsGet(this, "code").toKotlin()?.toString()

/** The JS SDK reports `storage/<code>` errors; the codes map onto the Android SDK's `ERROR_*` constants. */
internal fun Throwable.toStorageException(): StorageException = when (this) {
    is StorageException -> this
    is JsException -> thrownValue?.toStorageException(this) ?: StorageException.of(StorageException.ERROR_UNKNOWN, cause = this, message = message)
    else -> StorageException.of(StorageException.ERROR_UNKNOWN, cause = this, message = message)
}

/** A promise rejection or an `on` error callback carries the JS error itself rather than a [Throwable]. */
internal fun JsAny.toStorageException(cause: Throwable? = null): StorageException {
    val code = code() ?: ""
    val message = jsGet(this, "message").toKotlin()?.toString() ?: code
    return StorageException.of(
        errorCode = code.toErrorCode(),
        cause = cause ?: toThrowableOrNull() ?: Exception(message),
        httpResultCode = (jsGet(this, "status").toKotlin() as? Number)?.toInt() ?: -1,
        message = message,
    )
}

internal fun StorageError.toStorageException(): StorageException = (this as JsAny).toStorageException()

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
    val int8 = toInt8Array()
    return Uint8Array(int8.buffer, int8.byteOffset, int8.length)
}

internal fun JsListResult.toCompat(): ListResult = ListResult(
    items = items.toList().map { StorageReference(it) },
    prefixes = prefixes.toList().map { StorageReference(it) },
    pageToken = nextPageToken.takeUnless { it.isNullOrEmpty() },
)

internal fun StorageMetadata.toJs(): JsAny {
    val fields: List<Pair<String, Any?>> = providedHeaders().toList() + listOfNotNull(customMetadata.takeIf { it.isNotEmpty() }?.let { "customMetadata" to it.toMap() })
    return json(*fields.toTypedArray())
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
        jsObjectKeys(metadata).toList().forEach { keyJs ->
            val key = keyJs.toKotlinString()
            compat.customMetadata[key] = jsGet(metadata, key).toKotlin()?.toString().orEmpty()
        }
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

private fun String?.toEpochMillis(): Long = this?.let { parseDateStringToMillis(it) }?.takeIf { !it.isNaN() && it >= 0 }?.toLong() ?: 0

internal actual fun StorageReference.putSource(source: Source, metadata: StorageMetadata?): UploadTask = putSourceBytes(source, metadata)
