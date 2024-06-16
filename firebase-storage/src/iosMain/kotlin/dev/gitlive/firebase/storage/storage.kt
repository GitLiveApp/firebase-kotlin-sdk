/*
 * Copyright (c) 2023 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import cocoapods.FirebaseStorage.FIRStorage
import cocoapods.FirebaseStorage.FIRStorageListResult
import cocoapods.FirebaseStorage.FIRStorageMetadata
import cocoapods.FirebaseStorage.FIRStorageReference
import cocoapods.FirebaseStorage.FIRStorageTaskStatusFailure
import cocoapods.FirebaseStorage.FIRStorageTaskStatusPause
import cocoapods.FirebaseStorage.FIRStorageTaskStatusProgress
import cocoapods.FirebaseStorage.FIRStorageTaskStatusResume
import cocoapods.FirebaseStorage.FIRStorageTaskStatusSuccess
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL


actual val Firebase.storage get() =
    FirebaseStorage(FIRStorage.storage())

actual fun Firebase.storage(app: FirebaseApp): FirebaseStorage = FirebaseStorage(
    FIRStorage.storageForApp(app.ios as objcnames.classes.FIRApp)
)

actual class FirebaseStorage(val ios: FIRStorage) {
    actual val maxOperationRetryTimeMillis = ios.maxOperationRetryTime().toLong()
    actual val maxUploadRetryTimeMillis = ios.maxUploadRetryTime().toLong()

    actual fun setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long) {
        ios.setMaxOperationRetryTime(maxOperationRetryTimeMillis.toDouble())
    }

    actual fun setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long) {
        ios.setMaxUploadRetryTime(maxUploadRetryTimeMillis.toDouble())
    }

    actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }

    actual val reference get() = StorageReference(ios.reference())

    actual fun reference(location: String) = StorageReference(ios.referenceWithPath(location))
}

actual class StorageReference(val ios: FIRStorageReference) {
    actual val name: String get() = ios.name()
    actual val path: String get() = ios.fullPath()
    actual val bucket: String get() = ios.bucket()
    actual val parent: StorageReference? get() = ios.parent()?.let { StorageReference(it) }
    actual val root: StorageReference get() = StorageReference(ios.root())
    actual val storage: FirebaseStorage get() = FirebaseStorage(ios.storage())

    actual fun child(path: String): StorageReference = StorageReference(ios.child(path))

    actual suspend fun getMetadata(): FirebaseStorageMetadata? = ios.awaitResult {
        metadataWithCompletion { metadata, error ->
            if (error == null) {
                it.invoke(metadata?.toFirebaseStorageMetadata(), null)
            } else {
                it.invoke(null, error)
            }
        }
    }

    actual suspend fun delete() = await { ios.deleteWithCompletion(it) }

    actual suspend fun getDownloadUrl(): String = ios.awaitResult {
        downloadURLWithCompletion(completion = it)
    }.absoluteString()!!

    actual suspend fun listAll(): ListResult = awaitResult {
        ios.listAllWithCompletion { firStorageListResult, nsError ->
            it.invoke(firStorageListResult?.let { ListResult(it) }, nsError)
        }
    }

    actual suspend fun putFile(file: File, metadata: FirebaseStorageMetadata?) = ios.awaitResult { callback ->
        putFile(file.url, metadata?.toFIRMetadata(), callback)
    }.run {}

    actual suspend fun putData(data: Data, metadata: FirebaseStorageMetadata?) = ios.awaitResult { callback ->
        putData(data.data, metadata?.toFIRMetadata(), callback)
    }.run {}

    actual fun putFileResumable(file: File, metadata: FirebaseStorageMetadata?): ProgressFlow {
        val ios = ios.putFile(file.url, metadata?.toFIRMetadata())

        val flow = callbackFlow {
            ios.observeStatus(FIRStorageTaskStatusProgress) {
                val progress = it!!.progress()!!
                trySendBlocking(Progress.Running(progress.completedUnitCount, progress.totalUnitCount))
            }
            ios.observeStatus(FIRStorageTaskStatusPause) {
                val progress = it!!.progress()!!
                trySendBlocking(Progress.Paused(progress.completedUnitCount, progress.totalUnitCount))
            }
            ios.observeStatus(FIRStorageTaskStatusResume) {
                val progress = it!!.progress()!!
                trySendBlocking(Progress.Running(progress.completedUnitCount, progress.totalUnitCount))
            }
            ios.observeStatus(FIRStorageTaskStatusSuccess) { close(FirebaseStorageException(it!!.error().toString())) }
            ios.observeStatus(FIRStorageTaskStatusFailure) {
                when(it!!.error()!!.code) {
                    /*FIRStorageErrorCodeCancelled = */ -13040L -> cancel(it.error()!!.localizedDescription)
                    else -> close(FirebaseStorageException(it.error().toString()))
                }
            }
            awaitClose { ios.removeAllObservers() }
        }

        return object : ProgressFlow {
            override suspend fun collect(collector: FlowCollector<Progress>) = collector.emitAll(flow)
            override fun pause() = ios.pause()
            override fun resume() = ios.resume()
            override fun cancel() = ios.cancel()
        }
    }

}

actual class ListResult(ios: FIRStorageListResult) {
    actual val prefixes: List<StorageReference> = ios.prefixes().map { StorageReference(it as FIRStorageReference) }
    actual val items: List<StorageReference> = ios.items().map { StorageReference(it as FIRStorageReference) }
    actual val pageToken: String? = ios.pageToken()
}

actual class File(val url: NSURL)

actual class Data(val data: NSData)

actual class FirebaseStorageException(message: String): FirebaseException(message)

suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(FirebaseStorageException(error.toString()))
        }
    }
    job.await()
}

suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(FirebaseStorageException(error.toString()))
        }
    }
    return job.await() as R
}

fun FirebaseStorageMetadata.toFIRMetadata(): FIRStorageMetadata {
    val metadata = FIRStorageMetadata()
    val mappedMetadata: Map<Any?, String> = this.customMetadata.map {
        it.key to it.value
    }.toMap()
    metadata.setCustomMetadata(mappedMetadata)
    metadata.setCacheControl(this.cacheControl)
    metadata.setContentDisposition(this.contentDisposition)
    metadata.setContentEncoding(this.contentEncoding)
    metadata.setContentLanguage(this.contentLanguage)
    metadata.setContentType(this.contentType)
    return metadata
}

fun FIRStorageMetadata.toFirebaseStorageMetadata(): FirebaseStorageMetadata {
    val sdkMetadata = this
    return storageMetadata {
        md5Hash = sdkMetadata.md5Hash()
        cacheControl = sdkMetadata.cacheControl()
        contentDisposition = sdkMetadata.contentDisposition()
        contentEncoding = sdkMetadata.contentEncoding()
        contentLanguage = sdkMetadata.contentLanguage()
        contentType = sdkMetadata.contentType()
        sdkMetadata.customMetadata()?.forEach {
            setCustomMetadata(it.key.toString(), it.value.toString())
        }
    }
}