package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

/** Returns the [FirebaseStorage] instance of the default [FirebaseApp]. */
actual val Firebase.storage: FirebaseStorage
    get() = TODO("Not yet implemented")

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
actual fun Firebase.storage(app: FirebaseApp): FirebaseStorage {
    TODO("Not yet implemented")
}

actual class FirebaseStorage {
    actual val maxOperationRetryTimeMillis: Long
        get() = TODO("Not yet implemented")
    actual val maxUploadRetryTimeMillis: Long
        get() = TODO("Not yet implemented")

    actual fun setMaxOperationRetryTimeMillis(maxOperationRetryTimeMillis: Long) {
    }

    actual fun setMaxUploadRetryTimeMillis(maxUploadRetryTimeMillis: Long) {
    }

    actual fun useEmulator(host: String, port: Int) {
    }

    actual val reference: StorageReference
        get() = TODO("Not yet implemented")

    actual fun reference(location: String): StorageReference {
        TODO("Not yet implemented")
    }

}

actual class StorageReference {
    actual val name: String
        get() = TODO("Not yet implemented")
    actual val path: String
        get() = TODO("Not yet implemented")
    actual val bucket: String
        get() = TODO("Not yet implemented")
    actual val parent: StorageReference?
        get() = TODO("Not yet implemented")
    actual val root: StorageReference
        get() = TODO("Not yet implemented")
    actual val storage: FirebaseStorage
        get() = TODO("Not yet implemented")

    actual fun child(path: String): StorageReference {
        TODO("Not yet implemented")
    }

    actual suspend fun delete() {
    }

    actual suspend fun getDownloadUrl(): String {
        TODO("Not yet implemented")
    }

    actual suspend fun listAll(): ListResult {
        TODO("Not yet implemented")
    }

    actual fun putFileResumable(file: File): ProgressFlow {
        TODO("Not yet implemented")
    }

    actual suspend fun putFile(file: File) {
    }

}

actual class ListResult {
    actual val prefixes: List<StorageReference>
        get() = TODO("Not yet implemented")
    actual val items: List<StorageReference>
        get() = TODO("Not yet implemented")
    actual val pageToken: String?
        get() = TODO("Not yet implemented")
}

actual class File
actual class FirebaseStorageException : FirebaseException()