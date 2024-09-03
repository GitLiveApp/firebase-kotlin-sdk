package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlin.time.Duration

/** Returns the [FirebaseStorage] instance of the default [FirebaseApp]. */
public actual val Firebase.storage: FirebaseStorage
    get() = TODO("Not yet implemented")

public actual fun Firebase.storage(url: String): FirebaseStorage = TODO("Not yet implemented")

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
public actual fun Firebase.storage(app: FirebaseApp): FirebaseStorage = TODO("Not yet implemented")

public actual fun Firebase.storage(app: FirebaseApp, url: String): FirebaseStorage = TODO("Not yet implemented")

public actual class FirebaseStorage {
    public actual val maxOperationRetryTime: Duration
        get() = TODO("Not yet implemented")
    public actual val maxUploadRetryTime: Duration
        get() = TODO("Not yet implemented")

    public actual fun setMaxOperationRetryTime(maxOperationRetryTime: Duration) {
    }

    public actual fun setMaxUploadRetryTime(maxUploadRetryTime: Duration) {
    }

    public actual fun useEmulator(host: String, port: Int) {
    }

    public actual val reference: StorageReference
        get() = TODO("Not yet implemented")

    public actual fun reference(location: String): StorageReference {
        TODO("Not yet implemented")
    }

    public actual fun getReferenceFromUrl(fullUrl: String): StorageReference {
        TODO("Not yet implemented")
    }
}

public actual class StorageReference {
    public actual val name: String
        get() = TODO("Not yet implemented")
    public actual val path: String
        get() = TODO("Not yet implemented")
    public actual val bucket: String
        get() = TODO("Not yet implemented")
    public actual val parent: StorageReference?
        get() = TODO("Not yet implemented")
    public actual val root: StorageReference
        get() = TODO("Not yet implemented")
    public actual val storage: FirebaseStorage
        get() = TODO("Not yet implemented")

    public actual suspend fun getMetadata(): FirebaseStorageMetadata? {
        TODO("Not yet implemented")
    }

    public actual fun child(path: String): StorageReference {
        TODO("Not yet implemented")
    }

    public actual suspend fun delete() {
    }

    public actual suspend fun getDownloadUrl(): String {
        TODO("Not yet implemented")
    }

    public actual suspend fun listAll(): ListResult {
        TODO("Not yet implemented")
    }

    public actual fun putFileResumable(file: File, metadata: FirebaseStorageMetadata?): ProgressFlow {
        TODO("Not yet implemented")
    }

    public actual suspend fun putFile(file: File, metadata: FirebaseStorageMetadata?) {
    }

    public actual suspend fun putData(data: Data, metadata: FirebaseStorageMetadata?) {
    }
}

public actual class ListResult {
    public actual val prefixes: List<StorageReference>
        get() = TODO("Not yet implemented")
    public actual val items: List<StorageReference>
        get() = TODO("Not yet implemented")
    public actual val pageToken: String?
        get() = TODO("Not yet implemented")
}

public actual class File
public actual class FirebaseStorageException internal constructor(message: String) : FirebaseException(message)
public actual class Data
