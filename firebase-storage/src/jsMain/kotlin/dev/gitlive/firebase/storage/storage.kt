/*
 * Copyright (c) 2023 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.storage.externals.*
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll

actual val Firebase.storage
    get() = FirebaseStorage(getStorage())

actual fun Firebase.storage(app: FirebaseApp) =
    FirebaseStorage(getStorage(app.js))

actual class FirebaseStorage(val js: dev.gitlive.firebase.storage.externals.FirebaseStorage) {
    actual val maxOperationRetryTimeMillis = js.maxOperationRetryTime.toLong()
    actual val maxUploadRetryTimeMillis = js.maxUploadRetryTime.toLong()

    actual fun setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long) {
        js.maxOperationRetryTime = maxOperationRetryTimeMillis.toDouble()
    }

    actual fun setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long) {
        js.maxUploadRetryTime = maxUploadRetryTimeMillis.toDouble()
    }

    actual fun useEmulator(host: String, port: Int) {
        connectFirestoreEmulator(js, host, port.toDouble())
    }

    actual val reference: StorageReference get() = StorageReference(ref(js))

    actual fun reference(location: String) = rethrow { StorageReference(ref(js, location)) }

}

actual class StorageReference(val js: dev.gitlive.firebase.storage.externals.StorageReference) {
    actual val path: String get() = js.fullPath
    actual val name: String get() = js.name
    actual val bucket: String get() = js.bucket
    actual val parent: StorageReference? get() = js.parent?.let { StorageReference(it) }
    actual val root: StorageReference get() = StorageReference(js.root)
    actual val storage: FirebaseStorage get() = FirebaseStorage(js.storage)

    actual fun child(path: String): StorageReference = StorageReference(ref(js, path))

    actual suspend fun delete() = rethrow { deleteObject(js).await() }

    actual suspend fun getDownloadUrl(): String = rethrow { getDownloadURL(js).await().toString() }

    actual suspend fun listAll(): ListResult = rethrow { ListResult(listAll(js).await()) }

    actual suspend fun putFile(file: File): Unit = rethrow { uploadBytes(js, file).await() }

    actual fun putFileResumable(file: File): ProgressFlow = rethrow {
        val uploadTask = uploadBytesResumable(js, file)

        val flow = callbackFlow {
            val unsubscribe = uploadTask.on(
                "state_changed",
                {
                    when(it.state) {
                        "paused" -> trySend(Progress.Paused(it.bytesTransferred, it.totalBytes))
                        "running" -> trySend(Progress.Running(it.bytesTransferred, it.totalBytes))
                        "canceled" -> cancel()
                        "success", "error" -> Unit
                        else -> TODO("Unknown state ${it.state}")
                    }
                },
                { close(errorToException(it)) },
                { close() }
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

actual class ListResult(js: dev.gitlive.firebase.storage.externals.ListResult) {
    actual val prefixes: List<StorageReference> = js.prefixes.map { StorageReference(it) }
    actual val items: List<StorageReference> = js.items.map { StorageReference(it) }
    actual val pageToken: String? = js.nextPageToken
}

actual typealias File = org.w3c.files.File

actual open class FirebaseStorageException(code: String, cause: Throwable) :
    FirebaseException(code, cause)

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
                FirebaseStorageException(code, error)
            }
        }
    }