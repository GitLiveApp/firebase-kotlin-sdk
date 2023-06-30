package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow

/** Returns the [FirebaseStorage] instance of the default [FirebaseApp]. */
expect val Firebase.storage: FirebaseStorage

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
expect fun Firebase.storage(app: FirebaseApp): FirebaseStorage

expect class FirebaseStorage {
    fun getMaxOperationRetryTimeMillis(): Long
    fun getMaxUploadRetryTimeMillis(): Long
    fun setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long)
    fun setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long)
    fun useEmulator(host: String, port: Int)
}

expect class StorageReference {
    val name: String
    val path: String
    val bucket: String
    val parent: StorageReference?
    val root: StorageReference
    val storage: FirebaseStorage
    fun child(path: String): StorageReference
//    fun delete(): Task<Unit>
//    fun downloadUrl(): Task<Uri>
//    fun getBytes(maxDownloadSizeBytes: Long): Task<ByteArray>
//    fun getMetadata(): Task<StorageMetadata>
//    fun list(options: ListOptions? = definedExternally): Task<ListResult>
//    fun listAll(): Task<ListResult>
//    fun putBytes(bytes: ByteArray, metadata: StorageMetadata? = definedExternally): UploadTask
//    fun putFile(file: Uri, metadata: StorageMetadata? = definedExternally): UploadTask
//    fun putFile(file: Uri, metadata: StorageMetadata? = definedExternally, existingUploadUri: Uri? = definedExternally): UploadTask
//    fun putFile(file: Uri, metadata: StorageMetadata? = definedExternally, existingUploadUri: Uri? = definedExternally, existingUploadHeaders: Map<String, String>? = definedExternally): UploadTask
//    fun putStream(stream: InputStream, metadata: StorageMetadata? = definedExternally): UploadTask
//    fun updateMetadata(metadata: StorageMetadata): Task<StorageMetadata>
//    fun getStream(maxDownloadSizeBytes: Long = definedExternally): Flow<ByteReadPacket>
//    fun getStream(maxDownloadSizeBytes: Long = definedExternally, progressListener: StreamDownloadTask.StreamProcessor): Flow<ByteReadPacket>
//    fun getStream(maxDownloadSizeBytes: Long = definedExternally, progressListener: StreamDownloadTask.StreamProcessor, cancellationFlow: Flow<Unit>): Flow<ByteReadPacket>
//    fun getStream(maxDownloadSizeBytes: Long = definedExternally, progressListener: StreamDownloadTask.StreamProcessor, cancellationFlow: Flow<Unit>, executor: Executor): Flow<ByteReadPacket>
//    fun getStream(maxDownloadSizeBytes: Long = definedExternally, progressListener: StreamDownloadTask.StreamProcessor, cancellationFlow: Flow<Unit>, executor: Executor, bufferSize: Int): Flow<ByteReadPacket>
//    fun getStream(maxDownloadSizeBytes: Long = definedExternally, progressListener: StreamDownloadTask.StreamProcessor, cancellationFlow: Flow<Unit>, executor: Executor, bufferSize: Int, chunkSize: Int): Flow<ByteReadPacket>
//    fun getStream(maxDownloadSizeBytes: Long = definedExternally, progressListener: StreamDownloadTask.Stream
}

expect open class StorageException : FirebaseException
