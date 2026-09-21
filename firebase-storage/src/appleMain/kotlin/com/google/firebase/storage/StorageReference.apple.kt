/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import cocoapods.FirebaseStorage.FIRStorageMetadata
import cocoapods.FirebaseStorage.FIRStorageReference
import cocoapods.FirebaseStorage.FIRStorageTaskSnapshot
import cocoapods.FirebaseStorage.FIRStorageTaskStatusFailure
import cocoapods.FirebaseStorage.FIRStorageTaskStatusPause
import cocoapods.FirebaseStorage.FIRStorageTaskStatusProgress
import cocoapods.FirebaseStorage.FIRStorageTaskStatusResume
import cocoapods.FirebaseStorage.FIRStorageTaskStatusSuccess
import cocoapods.FirebaseStorage.FIRStorageUploadTask
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970
import platform.posix.memcpy

private const val MILLIS_PER_SECOND = 1000.0

/** @property ios The underlying Firebase iOS SDK object. */
public actual class StorageReference internal constructor(public val ios: FIRStorageReference) : Comparable<StorageReference> {
    public actual val activeUploadTasks: List<UploadTask> get() = ActiveTasks.uploads.filter { it.reference == this }
    public actual val bucket: String get() = ios.bucket()
    public actual val name: String get() = ios.name()
    public actual val parent: StorageReference? get() = ios.parent()?.let { StorageReference(it) }
    public actual val path: String get() = ios.fullPath()
    public actual val root: StorageReference get() = StorageReference(ios.root())
    public actual val storage: FirebaseStorage get() = FirebaseStorage(ios.storage())

    public actual fun child(pathString: String): StorageReference = StorageReference(ios.child(pathString))

    actual override fun compareTo(other: StorageReference): Int = toString().compareTo(other.toString())

    public actual fun delete(): Task<Nothing?> = task { completion -> ios.deleteWithCompletion { error -> completion(null, error) } }

    public actual fun getBytes(maxDownloadSizeBytes: Long): Task<ByteArray> = task { completion ->
        ios.dataWithMaxSize(maxDownloadSizeBytes) { data, error -> completion(data?.toByteArray() ?: ByteArray(0), error) }
    }

    public actual fun getDownloadUrl(): Task<Any> = task { completion ->
        ios.downloadURLWithCompletion { url, error -> completion(url?.absoluteString ?: "", error) }
    }

    public actual fun getMetadata(): Task<StorageMetadata> = task { completion ->
        ios.metadataWithCompletion { metadata, error -> completion(metadata?.toCompat() ?: StorageMetadata(), error) }
    }

    public actual fun list(maxResults: Int): Task<ListResult> = task { completion ->
        ios.listWithMaxResults(maxResults.toLong()) { result, error -> completion(result?.toCompat() ?: ListResult(emptyList(), emptyList(), null), error) }
    }

    public actual fun list(maxResults: Int, pageToken: String): Task<ListResult> = task { completion ->
        ios.listWithMaxResults(maxResults.toLong(), pageToken) { result, error -> completion(result?.toCompat() ?: ListResult(emptyList(), emptyList(), null), error) }
    }

    public actual fun listAll(): Task<ListResult> = task { completion ->
        ios.listAllWithCompletion { result, error -> completion(result?.toCompat() ?: ListResult(emptyList(), emptyList(), null), error) }
    }

    public actual fun putBytes(bytes: ByteArray): UploadTask = UploadTaskImpl(this, IosUploadController(ios.putData(bytes.toNSData(), null)))

    public actual fun putBytes(bytes: ByteArray, metadata: StorageMetadata): UploadTask = UploadTaskImpl(this, IosUploadController(ios.putData(bytes.toNSData(), metadata.toIos())))

    internal fun putFileUrl(url: NSURL, metadata: StorageMetadata?): UploadTask = UploadTaskImpl(this, IosUploadController(ios.putFile(url, metadata?.toIos())))

    public actual fun updateMetadata(metadata: StorageMetadata): Task<StorageMetadata> = task { completion ->
        ios.updateMetadata(metadata.toIos()) { updated, error -> completion(updated?.toCompat() ?: StorageMetadata(), error) }
    }

    override fun equals(other: Any?): Boolean = other is StorageReference && other.bucket == bucket && other.path == path

    override fun hashCode(): Int = toString().hashCode()

    override fun toString(): String = "gs://$bucket/$path"
}

/** An [UploadController] over a [FIRStorageUploadTask]. */
internal class IosUploadController(private val ios: FIRStorageUploadTask) : UploadController {
    override var bytesTransferred: Long = 0
    override var totalByteCount: Long = 0
    override var metadata: StorageMetadata? = null

    override fun start(task: UploadTask) {
        ios.observeStatus(FIRStorageTaskStatusProgress) { snapshot ->
            update(snapshot)
            task.reportProgress()
        }
        ios.observeStatus(FIRStorageTaskStatusResume) { snapshot ->
            update(snapshot)
            task.reportProgress()
        }
        ios.observeStatus(FIRStorageTaskStatusPause) { snapshot ->
            update(snapshot)
            task.reportPaused()
        }
        ios.observeStatus(FIRStorageTaskStatusSuccess) { snapshot ->
            update(snapshot)
            metadata = snapshot?.metadata()?.toCompat()
            ios.removeAllObservers()
            task.reportSuccess()
        }
        ios.observeStatus(FIRStorageTaskStatusFailure) { snapshot ->
            update(snapshot)
            ios.removeAllObservers()
            val error = snapshot?.error()
            if (error == null || error.code == StorageException.ERROR_CANCELED.toLong()) task.reportCanceled() else task.reportFailure(error.toStorageException())
        }
    }

    private fun update(snapshot: FIRStorageTaskSnapshot?) {
        snapshot?.progress()?.let {
            bytesTransferred = it.completedUnitCount
            totalByteCount = it.totalUnitCount
        }
    }

    override fun pause(): Boolean {
        ios.pause()
        return true
    }

    override fun resume(): Boolean {
        ios.resume()
        return true
    }

    override fun cancel(): Boolean {
        ios.cancel()
        return true
    }
}

internal inline fun <T> task(crossinline start: ((T, NSError?) -> Unit) -> Unit): Task<T> {
    val source = TaskCompletionSource<T>()
    start { result, error -> if (error == null) source.setResult(result) else source.setException(error.toStorageException()) }
    return source.task
}

/** The iOS SDK uses the same error codes as the Android SDK. */
internal fun NSError.toStorageException(): StorageException = StorageException.of(
    errorCode = code.toInt().takeIf { it <= StorageException.ERROR_UNKNOWN } ?: StorageException.ERROR_UNKNOWN,
    message = localizedDescription,
)

internal fun ByteArray.toNSData(): NSData = if (isEmpty()) NSData() else usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }

internal fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    if (isNotEmpty()) usePinned { memcpy(it.addressOf(0), bytes, length) }
}

internal fun cocoapods.FirebaseStorage.FIRStorageListResult.toCompat(): ListResult = ListResult(
    items = items().map { StorageReference(it as FIRStorageReference) },
    prefixes = prefixes().map { StorageReference(it as FIRStorageReference) },
    pageToken = pageToken(),
)

internal fun StorageMetadata.toIos(): FIRStorageMetadata {
    // The Apple SDK only sends deletions for custom keys present in the metadata it was created from,
    // so removed keys are seeded as the initial state and left out of the customMetadata set below.
    val removed = customMetadata.filterValues { it == null }.keys
    val ios = if (removed.isEmpty()) FIRStorageMetadata() else FIRStorageMetadata(dictionary = mapOf("metadata" to removed.associateWith { "" }))
    ios.setCacheControl(cacheControl)
    ios.setContentDisposition(contentDisposition)
    ios.setContentEncoding(contentEncoding)
    ios.setContentLanguage(contentLanguage)
    ios.setContentType(contentType)
    ios.setCustomMetadata(customMetadata.filterValues { it != null }.toMap<Any?, String?>())
    return ios
}

@Suppress("UNCHECKED_CAST")
internal fun FIRStorageMetadata.toCompat(): StorageMetadata = StorageMetadata().also { compat ->
    compat.bucketValue = bucket()
    compat.cacheControlValue = cacheControl()
    compat.contentDispositionValue = contentDisposition()
    compat.contentEncodingValue = contentEncoding()
    compat.contentLanguageValue = contentLanguage()
    compat.contentTypeValue = contentType()
    compat.creationTimeMillisValue = timeCreated()?.timeIntervalSince1970?.let { (it * MILLIS_PER_SECOND).toLong() } ?: 0
    (customMetadata() as Map<Any?, Any?>?)?.forEach { (key, value) -> compat.customMetadata[key.toString()] = value.toString() }
    compat.generationValue = generation().toString()
    compat.md5HashValue = md5Hash()
    compat.metadataGenerationValue = metageneration().toString()
    compat.nameValue = name()
    compat.pathValue = path() ?: ""
    compat.referenceValue = storageReference()?.let { StorageReference(it) }
    compat.sizeBytesValue = size()
    compat.updatedTimeMillisValue = updated()?.timeIntervalSince1970?.let { (it * MILLIS_PER_SECOND).toLong() } ?: 0
}
