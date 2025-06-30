/*
 * Copyright (c) 2023 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.js
import dev.gitlive.firebase.storage.externals.*
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

public actual val Firebase.storage: FirebaseStorage
    get() = FirebaseStorage(getStorage())

public actual fun Firebase.storage(url: String): FirebaseStorage = FirebaseStorage(getStorage(null, url))

public actual fun Firebase.storage(app: FirebaseApp): FirebaseStorage = FirebaseStorage(getStorage(app.js))

public actual fun Firebase.storage(app: FirebaseApp, url: String): FirebaseStorage = FirebaseStorage(getStorage(app.js, url))

public val FirebaseStorage.js: dev.gitlive.firebase.storage.externals.FirebaseStorage get() = js

public actual class FirebaseStorage(internal val js: dev.gitlive.firebase.storage.externals.FirebaseStorage) {
    public actual val maxOperationRetryTime: Duration = js.maxOperationRetryTime.milliseconds
    public actual val maxUploadRetryTime: Duration = js.maxUploadRetryTime.milliseconds

    public actual fun setMaxOperationRetryTime(maxOperationRetryTime: Duration) {
        js.maxOperationRetryTime = maxOperationRetryTime.toDouble(DurationUnit.MILLISECONDS)
    }

    public actual fun setMaxUploadRetryTime(maxUploadRetryTime: Duration) {
        js.maxUploadRetryTime = maxUploadRetryTime.toDouble(DurationUnit.MILLISECONDS)
    }

    public actual fun useEmulator(host: String, port: Int) {
        connectStorageEmulator(js, host, port.toDouble())
    }

    public actual val reference: StorageReference get() = StorageReference(ref(js))

    public actual fun reference(location: String): StorageReference = rethrow { StorageReference(ref(js, location)) }

    public actual fun getReferenceFromUrl(fullUrl: String): StorageReference = rethrow { StorageReference(ref(js, fullUrl)) }
}

public val StorageReference.js: dev.gitlive.firebase.storage.externals.StorageReference get() = js

public actual class StorageReference(internal val js: dev.gitlive.firebase.storage.externals.StorageReference) {
    public actual val path: String get() = js.fullPath
    public actual val name: String get() = js.name
    public actual val bucket: String get() = js.bucket
    public actual val parent: StorageReference? get() = js.parent?.let { StorageReference(it) }
    public actual val root: StorageReference get() = StorageReference(js.root)
    public actual val storage: FirebaseStorage get() = FirebaseStorage(js.storage)

    public actual suspend fun getMetadata(): FirebaseStorageMetadata? = rethrow { getMetadata(js).await().toFirebaseStorageMetadata() }

    public actual fun child(path: String): StorageReference = StorageReference(ref(js, path))

    public actual suspend fun delete(): Unit = rethrow { deleteObject(js).await() }

    public actual suspend fun getDownloadUrl(): String = rethrow { getDownloadURL(js).await().toString() }

    public actual suspend fun listAll(): ListResult = rethrow { ListResult(listAll(js).await()) }

    public actual suspend fun putFile(file: File, metadata: FirebaseStorageMetadata?): Unit = rethrow { uploadBytes(js, file, metadata?.toStorageMetadata()).await() }

    public actual suspend fun putData(data: Data, metadata: FirebaseStorageMetadata?): Unit = rethrow { uploadBytes(js, data.data, metadata?.toStorageMetadata()).await() }

    public actual fun putFileResumable(file: File, metadata: FirebaseStorageMetadata?): ProgressFlow = rethrow {
        val uploadTask = uploadBytesResumable(js, file, metadata?.toStorageMetadata())

        val flow = callbackFlow {
            val unsubscribe = uploadTask.on(
                "state_changed",
                {
                    when (it.state) {
                        "paused" -> trySend(Progress.Paused(it.bytesTransferred, it.totalBytes))
                        "running" -> trySend(Progress.Running(it.bytesTransferred, it.totalBytes))
                        "canceled" -> cancel()
                        "success", "error" -> Unit
                        else -> TODO("Unknown state ${it.state}")
                    }
                },
                { close(errorToException(it)) },
                { close() },
            )
            awaitClose { unsubscribe() }
        }

        return object : ProgressFlow {
            override suspend fun collect(collector: FlowCollector<Progress>) = collector.emitAll(flow)
            override fun pause() = uploadTask.pause().run {}
            override fun resume() = uploadTask.resume().run {}
            override fun cancel() = uploadTask.cancel().run {}
        }
    }
}

public actual class ListResult(js: dev.gitlive.firebase.storage.externals.ListResult) {
    public actual val prefixes: List<StorageReference> = js.prefixes.map { StorageReference(it) }
    public actual val items: List<StorageReference> = js.items.map { StorageReference(it) }
    public actual val pageToken: String? = js.nextPageToken
}

public actual typealias File = org.w3c.files.File
public actual class Data(public val data: org.khronos.webgl.Uint8Array)

public actual open class FirebaseStorageException(code: String, cause: Throwable) : FirebaseException(code, cause)

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw errorToException(e)
    }
}

internal fun errorToException(error: dynamic) = (error?.code ?: error?.message ?: "")
    .toString()
    .lowercase()
    .let { code ->
        when {
            else -> {
                println("Unknown error code in ${JSON.stringify(error)}")
                FirebaseStorageException(code, error as Throwable)
            }
        }
    }

internal fun UploadMetadata.toFirebaseStorageMetadata(): FirebaseStorageMetadata {
    val sdkMetadata = this
    return storageMetadata {
        md5Hash = sdkMetadata.md5Hash
        cacheControl = sdkMetadata.cacheControl
        contentDisposition = sdkMetadata.contentDisposition
        contentEncoding = sdkMetadata.contentEncoding
        contentLanguage = sdkMetadata.contentLanguage
        contentType = sdkMetadata.contentType
        customMetadata = sdkMetadata.customMetadata?.let { metadata ->
            val objectKeys = js("Object.keys")
            objectKeys(metadata).unsafeCast<Array<String>>().associateWith { key ->
                metadata[key]?.toString().orEmpty()
            }
        }.orEmpty().toMutableMap()
    }
}

internal fun FirebaseStorageMetadata.toStorageMetadata(): Json = json(
    "cacheControl" to cacheControl,
    "contentDisposition" to contentDisposition,
    "contentEncoding" to contentEncoding,
    "contentLanguage" to contentLanguage,
    "contentType" to contentType,
    "customMetadata" to json(*customMetadata.toList().toTypedArray()),
    "md5Hash" to md5Hash,
)
