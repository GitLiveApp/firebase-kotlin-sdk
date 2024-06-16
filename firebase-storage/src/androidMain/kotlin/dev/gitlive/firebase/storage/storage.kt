/*
 * Copyright (c) 2023 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.storage

import android.net.Uri
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.OnPausedListener
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

actual val Firebase.storage get() =
    FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance())

actual fun Firebase.storage(app: FirebaseApp) =
    FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance(app.android))

actual class FirebaseStorage(val android: com.google.firebase.storage.FirebaseStorage) {
    actual val maxOperationRetryTimeMillis = android.maxOperationRetryTimeMillis
    actual val maxUploadRetryTimeMillis = android.maxUploadRetryTimeMillis

    actual fun setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long) {
        android.maxOperationRetryTimeMillis = maxOperationRetryTimeMillis
    }

    actual fun setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long) {
        android.maxUploadRetryTimeMillis = maxUploadRetryTimeMillis
    }

    actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
    }

    actual val reference get() = StorageReference(android.reference)

    actual fun reference(location: String )= StorageReference(android.getReference(location))

}

actual class StorageReference(val android: com.google.firebase.storage.StorageReference) {
    actual val name: String get() = android.name
    actual val path: String get() = android.path
    actual val bucket: String get() = android.bucket
    actual val parent: StorageReference? get() = android.parent?.let { StorageReference(it) }
    actual val root: StorageReference get() = StorageReference(android.root)
    actual val storage: FirebaseStorage get() = FirebaseStorage(android.storage)

    actual suspend fun getMetadata(): FirebaseStorageMetadata? = android.metadata.await().toFirebaseStorageMetadata()

    actual fun child(path: String): StorageReference = StorageReference(android.child(path))

    actual suspend fun delete() = android.delete().await().run { Unit }

    actual suspend fun getDownloadUrl(): String = android.downloadUrl.await().toString()

    actual suspend fun listAll(): ListResult = ListResult(android.listAll().await())

    actual suspend fun putFile(file: File, metadata: FirebaseStorageMetadata?) {
        if (metadata != null) {
            android.putFile(file.uri, metadata.toStorageMetadata()).await().run {}
        } else {
            android.putFile(file.uri).await().run {}
        }
    }

    actual suspend fun putData(data: Data, metadata: FirebaseStorageMetadata?) {
        if (metadata != null) {
            android.putBytes(data.data, metadata.toStorageMetadata()).await().run {}
        } else {
            android.putBytes(data.data).await().run {}
        }
    }

    actual fun putFileResumable(file: File, metadata: FirebaseStorageMetadata?): ProgressFlow {
        val android = if (metadata != null) {
            android.putFile(file.uri, metadata.toStorageMetadata())
        } else {
            android.putFile(file.uri)
        }

        val flow = callbackFlow {
            val onCanceledListener = OnCanceledListener { cancel() }
            val onCompleteListener = OnCompleteListener<UploadTask.TaskSnapshot> { close(it.exception) }
            val onPausedListener = OnPausedListener<UploadTask.TaskSnapshot> { trySendBlocking(Progress.Paused(it.bytesTransferred, it.totalByteCount)) }
            val onProgressListener = OnProgressListener<UploadTask.TaskSnapshot> { trySendBlocking(Progress.Running(it.bytesTransferred, it.totalByteCount)) }
            android.addOnCanceledListener(onCanceledListener)
            android.addOnCompleteListener(onCompleteListener)
            android.addOnPausedListener(onPausedListener)
            android.addOnProgressListener(onProgressListener)
            awaitClose {
                android.removeOnCanceledListener(onCanceledListener)
                android.removeOnCompleteListener(onCompleteListener)
                android.removeOnPausedListener(onPausedListener)
                android.removeOnProgressListener(onProgressListener)
            }
        }

        return object : ProgressFlow {
            override suspend fun collect(collector: FlowCollector<Progress>) = collector.emitAll(flow)
            override fun pause() = android.pause().run {}
            override fun resume() = android.resume().run {}
            override fun cancel() = android.cancel().run {}
        }
    }
}

actual class ListResult(android: com.google.firebase.storage.ListResult) {
    actual val prefixes: List<StorageReference> = android.prefixes.map { StorageReference(it) }
    actual val items: List<StorageReference> = android.items.map { StorageReference(it) }
    actual val pageToken: String? = android.pageToken
}

actual class File(val uri: Uri)

actual class Data(val data: ByteArray)

actual typealias FirebaseStorageException = com.google.firebase.storage.StorageException

internal fun FirebaseStorageMetadata.toStorageMetadata(): StorageMetadata {
    return StorageMetadata.Builder()
        .setCacheControl(this.cacheControl)
        .setContentDisposition(this.contentDisposition)
        .setContentEncoding(this.contentEncoding)
        .setContentLanguage(this.contentLanguage)
        .setContentType(this.contentType)
        .apply {
            customMetadata.entries.forEach {
                (key, value) -> setCustomMetadata(key, value)
            }
        }.build()
}

internal fun StorageMetadata.toFirebaseStorageMetadata(): FirebaseStorageMetadata {
    val sdkMetadata = this
    return storageMetadata {
        md5Hash = sdkMetadata.md5Hash
        cacheControl = sdkMetadata.cacheControl
        contentDisposition = sdkMetadata.contentDisposition
        contentEncoding = sdkMetadata.contentEncoding
        contentLanguage = sdkMetadata.contentLanguage
        contentType = sdkMetadata.contentType
        sdkMetadata.customMetadataKeys.forEach {
            setCustomMetadata(it, sdkMetadata.getCustomMetadata(it))
        }
    }
}