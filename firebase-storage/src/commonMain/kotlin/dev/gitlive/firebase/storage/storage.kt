/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.storage.OnPausedListener
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.putFile
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import com.google.firebase.storage.FirebaseStorage as CompatFirebaseStorage
import com.google.firebase.storage.ListResult as CompatListResult
import com.google.firebase.storage.StorageMetadata as CompatStorageMetadata
import com.google.firebase.storage.StorageReference as CompatStorageReference

/**
 * Entry point for Cloud Storage for Firebase.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.storage.FirebaseStorage] this wraps.
 */
public class FirebaseStorage internal constructor(public val compat: CompatFirebaseStorage) {
    /** The maximum time to retry operations other than uploads and downloads. */
    public val maxOperationRetryTime: Duration get() = compat.maxOperationRetryTimeMillis.milliseconds

    /** The maximum time to retry uploads. */
    public val maxUploadRetryTime: Duration get() = compat.maxUploadRetryTimeMillis.milliseconds

    /** Sets the maximum time to retry operations other than uploads and downloads. */
    public fun setMaxOperationRetryTime(maxOperationRetryTime: Duration) {
        compat.maxOperationRetryTimeMillis = maxOperationRetryTime.inWholeMilliseconds
    }

    /** Sets the maximum time to retry uploads. */
    public fun setMaxUploadRetryTime(maxUploadRetryTime: Duration) {
        compat.maxUploadRetryTimeMillis = maxUploadRetryTime.inWholeMilliseconds
    }

    /** Routes requests to the Storage emulator at [host]:[port]. */
    public fun useEmulator(host: String, port: Int) {
        compat.useEmulator(host, port)
    }

    /** A reference to the root of the bucket. */
    public val reference: StorageReference get() = StorageReference(compat.reference)

    /** A reference to [location]. */
    public fun reference(location: String): StorageReference = StorageReference(compat.getReference(location))

    /** A reference for a `gs://` or `https://` URL. */
    public fun getReferenceFromUrl(fullUrl: String): StorageReference = StorageReference(compat.getReferenceFromUrl(fullUrl))

    override fun equals(other: Any?): Boolean = other is FirebaseStorage && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseStorage($compat)"
}

@Deprecated("Deprecated to use Kotlin Duration", replaceWith = ReplaceWith("maxOperationRetryTime"))
public val FirebaseStorage.maxOperationRetryTimeMillis: Long get() = maxOperationRetryTime.inWholeMilliseconds

@Deprecated("Deprecated to use Kotlin Duration", replaceWith = ReplaceWith("maxUploadRetryTime"))
public val FirebaseStorage.maxUploadRetryTimeMillis: Long get() = maxUploadRetryTime.inWholeMilliseconds

@Deprecated("Deprecated to use Kotlin Duration", replaceWith = ReplaceWith("setMaxOperationRetryTime(maxOperationRetryTimeMillis.milliseconds)"))
public fun FirebaseStorage.setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long) {
    setMaxOperationRetryTime(maxOperationRetryTimeMillis.milliseconds)
}

@Deprecated("Deprecated to use Kotlin Duration", replaceWith = ReplaceWith("setMaxUploadRetryTime(maxUploadRetryTimeMillis.milliseconds)"))
public fun FirebaseStorage.setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long) {
    setMaxUploadRetryTime(maxUploadRetryTimeMillis.milliseconds)
}

/**
 * A reference to an object or folder in Cloud Storage.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.storage.StorageReference] this wraps.
 */
public class StorageReference internal constructor(public val compat: CompatStorageReference) {
    public val name: String get() = compat.name
    public val path: String get() = compat.path
    public val bucket: String get() = compat.bucket
    public val parent: StorageReference? get() = compat.parent?.let { StorageReference(it) }
    public val root: StorageReference get() = StorageReference(compat.root)
    public val storage: FirebaseStorage get() = FirebaseStorage(compat.storage)

    /** The object's metadata. */
    public suspend fun getMetadata(): FirebaseStorageMetadata? = compat.getMetadata().await().toFirebaseStorageMetadata()

    /** Downloads the object into memory, failing if it is larger than [maxDownloadSizeBytes]. */
    public suspend fun getData(maxDownloadSizeBytes: Long): Data = compat.getBytes(maxDownloadSizeBytes).await().toData()

    /** Updates the object's settable metadata. */
    public suspend fun updateMetadata(metadata: FirebaseStorageMetadata): FirebaseStorageMetadata? = compat.updateMetadata(metadata.toCompat()).await().toFirebaseStorageMetadata()

    public fun child(path: String): StorageReference = StorageReference(compat.child(path))

    /** Deletes the object. */
    public suspend fun delete() {
        compat.delete().await()
    }

    /** The public download URL of the object. */
    public suspend fun getDownloadUrl(): String = compat.getDownloadUrl().await().toString()

    /** Lists up to [maxResults] items and prefixes, continuing from [pageToken] if given. */
    public suspend fun list(maxResults: Int, pageToken: String? = null): ListResult = ListResult(
        if (pageToken == null) compat.list(maxResults).await() else compat.list(maxResults, pageToken).await(),
    )

    /** Lists every item and prefix under this reference. */
    public suspend fun listAll(): ListResult = ListResult(compat.listAll().await())

    /** Uploads [file] and suspends until the upload completes. */
    public suspend fun putFile(file: File, metadata: FirebaseStorageMetadata? = null) {
        uploadFile(file, metadata).await()
    }

    /** Uploads [data] and suspends until the upload completes. */
    public suspend fun putData(data: Data, metadata: FirebaseStorageMetadata? = null) {
        uploadData(data, metadata).await()
    }

    /** Uploads [data], reporting progress through the returned [ProgressFlow], which can pause, resume and cancel it. */
    public fun putDataResumable(data: Data, metadata: FirebaseStorageMetadata? = null): ProgressFlow = uploadData(data, metadata).asProgressFlow()

    /** Uploads [file], reporting progress through the returned [ProgressFlow], which can pause, resume and cancel it. */
    public fun putFileResumable(file: File, metadata: FirebaseStorageMetadata? = null): ProgressFlow = uploadFile(file, metadata).asProgressFlow()

    private fun uploadData(data: Data, metadata: FirebaseStorageMetadata?): UploadTask = if (metadata != null) compat.putBytes(data.toByteArray(), metadata.toCompat()) else compat.putBytes(data.toByteArray())

    private fun uploadFile(file: File, metadata: FirebaseStorageMetadata?): UploadTask = if (metadata != null) compat.putFile(file.platformFile, metadata.toCompat()) else compat.putFile(file.platformFile)

    override fun equals(other: Any?): Boolean = other is StorageReference && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "StorageReference($compat)"
}

private fun UploadTask.asProgressFlow(): ProgressFlow {
    val task = this
    val flow: Flow<Progress> = callbackFlow {
        val onCanceledListener = OnCanceledListener { cancel() }
        val onCompleteListener = OnCompleteListener<UploadTask.TaskSnapshot> { close(it.exception) }
        val onPausedListener = OnPausedListener<UploadTask.TaskSnapshot> { trySend(Progress.Paused(it.bytesTransferred, it.totalByteCount)) }
        val onProgressListener = OnProgressListener<UploadTask.TaskSnapshot> { trySend(Progress.Running(it.bytesTransferred, it.totalByteCount)) }
        task.addOnCanceledListener(onCanceledListener)
        task.addOnCompleteListener(onCompleteListener)
        task.addOnPausedListener(onPausedListener)
        task.addOnProgressListener(onProgressListener)
        awaitClose {
            task.removeOnCanceledListener(onCanceledListener)
            task.removeOnCompleteListener(onCompleteListener)
            task.removeOnPausedListener(onPausedListener)
            task.removeOnProgressListener(onProgressListener)
        }
    }
    return object : ProgressFlow {
        override suspend fun collect(collector: FlowCollector<Progress>) = collector.emitAll(flow)

        override fun pause() {
            task.pause()
        }

        override fun resume() {
            task.resume()
        }

        override fun cancel() {
            task.cancel()
        }
    }
}

/**
 * A page of the items and prefixes under a reference.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.storage.ListResult] this wraps.
 */
public class ListResult internal constructor(public val compat: CompatListResult) {
    public val prefixes: List<StorageReference> get() = compat.prefixes.map { StorageReference(it) }
    public val items: List<StorageReference> get() = compat.items.map { StorageReference(it) }
    public val pageToken: String? get() = compat.pageToken
}

/** A file on the platform: an `android.net.Uri` on Android and the JVM, an `NSURL` on Apple platforms, a `File` on JS. */
public expect class File

/** Bytes on the platform: a `ByteArray` on Android and the JVM, an `NSData` on Apple platforms, a `Uint8Array` on JS. */
public expect class Data

internal expect fun Data.toByteArray(): ByteArray

internal expect fun ByteArray.toData(): Data

/** The platform file object the `com.google.firebase.storage` layer's `putFile` takes. */
internal expect val File.platformFile: Any

public sealed class Progress(public val bytesTransferred: Number, public val totalByteCount: Number) {
    public class Running internal constructor(bytesTransferred: Number, totalByteCount: Number) : Progress(bytesTransferred, totalByteCount)
    public class Paused internal constructor(bytesTransferred: Number, totalByteCount: Number) : Progress(bytesTransferred, totalByteCount)
}

public interface ProgressFlow : Flow<Progress> {
    public fun pause()
    public fun resume()
    public fun cancel()
}

public typealias FirebaseStorageException = com.google.firebase.storage.StorageException

public data class FirebaseStorageMetadata(
    var md5Hash: String? = null,
    var cacheControl: String? = null,
    var contentDisposition: String? = null,
    var contentEncoding: String? = null,
    var contentLanguage: String? = null,
    var contentType: String? = null,
    var customMetadata: MutableMap<String, String> = mutableMapOf(),
) {
    public fun setCustomMetadata(key: String, value: String?) {
        value?.let {
            customMetadata[key] = it
        }
    }
}

public fun storageMetadata(init: FirebaseStorageMetadata.() -> Unit): FirebaseStorageMetadata {
    val metadata = FirebaseStorageMetadata()
    metadata.init()
    return metadata
}

internal fun FirebaseStorageMetadata.toCompat(): CompatStorageMetadata = CompatStorageMetadata.Builder()
    .setCacheControl(cacheControl)
    .setContentDisposition(contentDisposition)
    .setContentEncoding(contentEncoding)
    .setContentLanguage(contentLanguage)
    .setContentType(contentType)
    .apply { customMetadata.forEach { (key, value) -> setCustomMetadata(key, value) } }
    .build()

internal fun CompatStorageMetadata.toFirebaseStorageMetadata(): FirebaseStorageMetadata {
    val sdkMetadata = this
    return storageMetadata {
        md5Hash = sdkMetadata.md5Hash
        cacheControl = sdkMetadata.cacheControl
        contentDisposition = sdkMetadata.contentDisposition
        contentEncoding = sdkMetadata.contentEncoding
        contentLanguage = sdkMetadata.contentLanguage
        contentType = sdkMetadata.contentType
        sdkMetadata.customMetadataKeys.forEach { setCustomMetadata(it, sdkMetadata.getCustomMetadata(it)) }
    }
}
