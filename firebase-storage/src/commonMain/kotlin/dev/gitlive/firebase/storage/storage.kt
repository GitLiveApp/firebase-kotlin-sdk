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
}

expect open class StorageException : FirebaseException
