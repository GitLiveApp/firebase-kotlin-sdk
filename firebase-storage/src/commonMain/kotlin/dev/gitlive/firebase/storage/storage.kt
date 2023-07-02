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

    suspend fun delete()

    suspend fun getDownloadUrl(): String

    fun putFile(file: File): ProgressFlow

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
