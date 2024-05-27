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
    val maxOperationRetryTimeMillis: Long
    val maxUploadRetryTimeMillis: Long

    fun setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long)
    fun setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long)
    fun useEmulator(host: String, port: Int)

    val reference: StorageReference
    fun reference(location: String): StorageReference

}

expect class StorageReference {
    val name: String
    val path: String
    val bucket: String
    val parent: StorageReference?
    val root: StorageReference
    val storage: FirebaseStorage

    fun child(path: String): StorageReference

    suspend fun delete()

    suspend fun getDownloadUrl(): String

    suspend fun listAll(): ListResult

    suspend fun putFile(file: File, metadata: FirebaseStorageMetadata? = null)

    fun putFileResumable(file: File, metadata: FirebaseStorageMetadata? = null): ProgressFlow
}

expect class ListResult {
    val prefixes: List<StorageReference>
    val items: List<StorageReference>
    val pageToken: String?
}

expect class File

sealed class Progress(val bytesTransferred: Number, val totalByteCount: Number) {
    class Running internal constructor(bytesTransferred: Number, totalByteCount: Number): Progress(bytesTransferred, totalByteCount)
    class Paused internal constructor(bytesTransferred: Number, totalByteCount: Number): Progress(bytesTransferred, totalByteCount)
}

interface ProgressFlow : Flow<Progress> {
    fun pause()
    fun resume()
    fun cancel()
}

expect class FirebaseStorageException : FirebaseException

data class FirebaseStorageMetadata(
    var md5Hash: String? = null,
    var cacheControl: String? = null,
    var contentDisposition: String? = null,
    var contentEncoding: String? = null,
    var contentLanguage: String? = null,
    var contentType: String? = null,
    var customMetadata: MutableMap<String, String> = mutableMapOf()
) {
    fun setCustomMetadata(key: String, value: String?) {
        value?.let {
            customMetadata[key] = it
        }
    }
}

fun storageMetadata(init: FirebaseStorageMetadata.() -> Unit): FirebaseStorageMetadata {
    val metadata = FirebaseStorageMetadata()
    metadata.init()
    return metadata
}