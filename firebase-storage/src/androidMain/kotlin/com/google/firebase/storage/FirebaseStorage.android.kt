/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.storage

import android.net.Uri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import dev.gitlive.firebase.storage.stub
import java.io.File

/*
 * Header stubs for com.google.firebase:firebase-storage (see buildSrc utils/HeaderStubs.kt): compiled against,
 * verified to match the real classes, and deleted from the output so the real SDK binds at runtime. The task stubs
 * extend the real Play Services Task (firebase-app strips its stubs), so they leave its members inherited. The members with
 * android.net.Uri / java.io.File signatures that are not in the common API are declared here so that the shipped
 * String / Any extensions (StorageFiles.android.kt, StorageDownloads.android.kt, StorageTaskUri.android.kt) bind to them.
 */

public actual class FirebaseStorage private constructor() {
    public actual val app: FirebaseApp get() = stub()
    public actual var maxOperationRetryTimeMillis: Long
        get() = stub()
        set(_) = stub()
    public actual var maxUploadRetryTimeMillis: Long
        get() = stub()
        set(_) = stub()

    /** Not in the common API (JS has no download retry time); the shipped nonJsMain extension binds to this member. */
    public var maxDownloadRetryTimeMillis: Long
        get() = stub()
        set(_) = stub()
    public actual val reference: StorageReference get() = stub()
    public actual fun getReference(location: String): StorageReference = stub()
    public actual fun getReferenceFromUrl(fullUrl: String): StorageReference = stub()
    public actual fun useEmulator(host: String, port: Int): Unit = stub()

    public actual companion object {
        @JvmStatic
        public actual fun getInstance(): FirebaseStorage = stub()

        @JvmStatic
        public actual fun getInstance(url: String): FirebaseStorage = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp): FirebaseStorage = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp, url: String): FirebaseStorage = stub()
    }
}

public actual class StorageReference private constructor() : Comparable<StorageReference> {
    public actual val activeUploadTasks: List<UploadTask> get() = stub()
    public actual val bucket: String get() = stub()
    public actual val name: String get() = stub()
    public actual val parent: StorageReference? get() = stub()
    public actual val path: String get() = stub()
    public actual val root: StorageReference get() = stub()
    public actual val storage: FirebaseStorage get() = stub()
    public actual fun child(pathString: String): StorageReference = stub()
    actual override fun compareTo(other: StorageReference): Int = stub()
    public actual fun delete(): Task<Nothing?> = stub()
    public actual fun getBytes(maxDownloadSizeBytes: Long): Task<ByteArray> = stub()
    public actual fun getDownloadUrl(): Task<Any> = stub()
    public actual fun getMetadata(): Task<StorageMetadata> = stub()
    public actual fun list(maxResults: Int): Task<ListResult> = stub()
    public actual fun list(maxResults: Int, pageToken: String): Task<ListResult> = stub()
    public actual fun listAll(): Task<ListResult> = stub()
    public actual fun putBytes(bytes: ByteArray): UploadTask = stub()
    public actual fun putBytes(bytes: ByteArray, metadata: StorageMetadata): UploadTask = stub()
    public actual fun updateMetadata(metadata: StorageMetadata): Task<StorageMetadata> = stub()

    /** The SDK's file members; the shipped extensions of common code bind to these. */
    public fun putFile(uri: Uri): UploadTask = stub()
    public fun putFile(uri: Uri, metadata: StorageMetadata): UploadTask = stub()
    public fun getFile(destinationUri: Uri): FileDownloadTask = stub()
    public fun getFile(destinationFile: File): FileDownloadTask = stub()
    public val activeDownloadTasks: List<FileDownloadTask> get() = stub()
}

public actual abstract class CancellableTask<StateT> actual constructor() : Task<StateT>() {
    public actual abstract fun cancel(): Boolean
    public actual abstract val isInProgress: Boolean
    public actual abstract fun addOnProgressListener(listener: OnProgressListener<StateT>): CancellableTask<StateT>
}

public actual abstract class ControllableTask<StateT> actual constructor() : CancellableTask<StateT>() {
    public actual abstract val isPaused: Boolean
    public actual abstract fun pause(): Boolean
    public actual abstract fun resume(): Boolean
    public actual abstract fun addOnPausedListener(listener: OnPausedListener<StateT>): ControllableTask<StateT>
}

public actual abstract class StorageTask<ResultT : StorageTask.ProvideError> protected actual constructor() : ControllableTask<ResultT>() {
    public actual val snapshot: ResultT get() = stub()
    actual override val isInProgress: Boolean get() = stub()
    actual override val isPaused: Boolean get() = stub()
    actual override fun cancel(): Boolean = stub()
    actual override fun pause(): Boolean = stub()
    actual override fun resume(): Boolean = stub()
    actual override fun addOnCanceledListener(listener: OnCanceledListener): StorageTask<ResultT> = stub()
    actual override fun addOnCompleteListener(listener: OnCompleteListener<ResultT>): StorageTask<ResultT> = stub()
    actual override fun addOnFailureListener(listener: OnFailureListener): StorageTask<ResultT> = stub()
    actual override fun addOnPausedListener(listener: OnPausedListener<ResultT>): StorageTask<ResultT> = stub()
    actual override fun addOnProgressListener(listener: OnProgressListener<ResultT>): StorageTask<ResultT> = stub()
    actual override fun addOnSuccessListener(listener: OnSuccessListener<in ResultT>): StorageTask<ResultT> = stub()
    public actual fun removeOnCanceledListener(listener: OnCanceledListener): StorageTask<ResultT> = stub()
    public actual fun removeOnCompleteListener(listener: OnCompleteListener<ResultT>): StorageTask<ResultT> = stub()
    public actual fun removeOnFailureListener(listener: OnFailureListener): StorageTask<ResultT> = stub()
    public actual fun removeOnPausedListener(listener: OnPausedListener<ResultT>): StorageTask<ResultT> = stub()
    public actual fun removeOnProgressListener(listener: OnProgressListener<ResultT>): StorageTask<ResultT> = stub()
    public actual fun removeOnSuccessListener(listener: OnSuccessListener<in ResultT>): StorageTask<ResultT> = stub()
    actual override fun <ContinuationResultT> continueWith(continuation: Continuation<ResultT, ContinuationResultT>): Task<ContinuationResultT> = stub()
    actual override fun <ContinuationResultT> continueWithTask(continuation: Continuation<ResultT, Task<ContinuationResultT>>): Task<ContinuationResultT> = stub()
    actual override fun <ContinuationResultT> onSuccessTask(successContinuation: SuccessContinuation<ResultT, ContinuationResultT>): Task<ContinuationResultT> = stub()
    protected actual open fun onCanceled(): Unit = stub()
    protected actual open fun onFailure(): Unit = stub()
    protected actual open fun onPaused(): Unit = stub()
    protected actual open fun onProgress(): Unit = stub()
    protected actual open fun onQueued(): Unit = stub()
    protected actual open fun onSuccess(): Unit = stub()

    public actual interface ProvideError {
        public actual val error: Exception?
    }

    public actual open inner class SnapshotBase actual constructor(error: Exception?) : ProvideError {
        actual override val error: Exception? get() = stub()
        public actual val storage: StorageReference get() = stub()
        public actual val task: StorageTask<ResultT> get() = stub()
    }
}

public actual abstract class UploadTask private constructor() : StorageTask<UploadTask.TaskSnapshot>() {
    protected actual open fun resetState(): Unit = stub()
    protected actual open fun schedule(): Unit = stub()

    public actual inner class TaskSnapshot private constructor() : SnapshotBase(null) {
        public actual val bytesTransferred: Long get() = stub()
        public actual val metadata: StorageMetadata? get() = stub()
        public actual val totalByteCount: Long get() = stub()

        /** The SDK's Uri member; the shipped String extension of common code binds to it. */
        public val uploadSessionUri: Uri? get() = stub()
    }
}

public actual abstract class FileDownloadTask private constructor() : StorageTask<FileDownloadTask.TaskSnapshot>() {
    public actual inner class TaskSnapshot private constructor() : SnapshotBase(null) {
        public actual val bytesTransferred: Long get() = stub()
        public actual val totalByteCount: Long get() = stub()
    }
}

public actual class StorageMetadata actual constructor() {
    public actual val bucket: String? get() = stub()
    public actual val cacheControl: String? get() = stub()
    public actual val contentDisposition: String? get() = stub()
    public actual val contentEncoding: String? get() = stub()
    public actual val contentLanguage: String? get() = stub()
    public actual val contentType: String? get() = stub()
    public actual val creationTimeMillis: Long get() = stub()
    public actual fun getCustomMetadata(key: String): String? = stub()
    public actual val customMetadataKeys: Set<String> get() = stub()
    public actual val generation: String? get() = stub()
    public actual val md5Hash: String? get() = stub()
    public actual val metadataGeneration: String? get() = stub()
    public actual val name: String? get() = stub()
    public actual val path: String get() = stub()
    public actual val reference: StorageReference? get() = stub()
    public actual val sizeBytes: Long get() = stub()
    public actual val updatedTimeMillis: Long get() = stub()

    public actual class Builder {
        public actual constructor()
        public actual constructor(original: StorageMetadata)
        public actual var cacheControl: String?
            get() = stub()
            set(_) = stub()
        public actual var contentDisposition: String?
            get() = stub()
            set(_) = stub()
        public actual var contentEncoding: String?
            get() = stub()
            set(_) = stub()
        public actual var contentLanguage: String?
            get() = stub()
            set(_) = stub()
        public actual var contentType: String?
            get() = stub()
            set(_) = stub()
        public actual fun build(): StorageMetadata = stub()
        public actual fun setCacheControl(cacheControl: String?): Builder = stub()
        public actual fun setContentDisposition(contentDisposition: String?): Builder = stub()
        public actual fun setContentEncoding(contentEncoding: String?): Builder = stub()
        public actual fun setContentLanguage(contentLanguage: String?): Builder = stub()
        public actual fun setContentType(contentType: String?): Builder = stub()
        public actual fun setCustomMetadata(key: String, value: String?): Builder = stub()
    }
}

public actual open class StorageException private constructor() : FirebaseException("") {
    public actual val errorCode: Int get() = stub()
    public actual val httpResultCode: Int get() = stub()

    @get:JvmName("getIsRecoverableException")
    public actual val isRecoverableException: Boolean get() = stub()

    @Retention(AnnotationRetention.SOURCE)
    public actual annotation class ErrorCode

    public actual companion object {
        @JvmField
        public actual val ERROR_BUCKET_NOT_FOUND: Int = -13011

        @JvmField
        public actual val ERROR_CANCELED: Int = -13040

        @JvmField
        public actual val ERROR_INVALID_CHECKSUM: Int = -13031

        @JvmField
        public actual val ERROR_NOT_AUTHENTICATED: Int = -13020

        @JvmField
        public actual val ERROR_NOT_AUTHORIZED: Int = -13021

        @JvmField
        public actual val ERROR_OBJECT_NOT_FOUND: Int = -13010

        @JvmField
        public actual val ERROR_PROJECT_NOT_FOUND: Int = -13012

        @JvmField
        public actual val ERROR_QUOTA_EXCEEDED: Int = -13013

        @JvmField
        public actual val ERROR_RETRY_LIMIT_EXCEEDED: Int = -13030

        @JvmField
        public actual val ERROR_UNKNOWN: Int = -13000

        @JvmStatic
        public actual fun fromException(exception: Throwable): StorageException = stub()

        @JvmStatic
        public actual fun fromExceptionAndHttpCode(exception: Throwable?, httpResultCode: Int): StorageException? = stub()
    }
}

public actual class ListResult private constructor() {
    public actual val items: List<StorageReference> get() = stub()
    public actual val pageToken: String? get() = stub()
    public actual val prefixes: List<StorageReference> get() = stub()
}
