@file:JsModule("firebase/storage")
@file:JsNonModule

package dev.gitlive.firebase.storage.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun getStorage(app: FirebaseApp? = definedExternally, url: String): FirebaseStorage

public external fun getStorage(app: FirebaseApp? = definedExternally): FirebaseStorage

public external fun ref(storage: FirebaseStorage, url: String? = definedExternally): StorageReference
public external fun ref(ref: StorageReference, url: String? = definedExternally): StorageReference

public external fun getDownloadURL(ref: StorageReference): Promise<String>

public external fun getMetadata(ref: StorageReference): Promise<StorageMetadata>

public external fun uploadBytes(ref: StorageReference, file: dynamic, metadata: StorageMetadata?): Promise<Unit>

public external fun uploadBytesResumable(ref: StorageReference, data: dynamic, metadata: StorageMetadata?): UploadTask

public external fun deleteObject(ref: StorageReference): Promise<Unit>

public external fun listAll(ref: StorageReference): Promise<ListResult>

public external fun connectStorageEmulator(
    storage: FirebaseStorage,
    host: String,
    port: Double,
    options: Any? = definedExternally,
)

public external interface FirebaseStorage {
    public var maxOperationRetryTime: Double
    public var maxUploadRetryTime: Double
}

public external interface StorageReference {
    public val bucket: String
    public val fullPath: String
    public val name: String
    public val parent: StorageReference?
    public val root: StorageReference
    public val storage: FirebaseStorage
}

public open external class ListResult {
    public val items: Array<StorageReference>
    public val nextPageToken: String
    public val prefixes: Array<StorageReference>
}

public external interface StorageError

public external interface UploadTaskSnapshot {
    public val bytesTransferred: Number
    public val ref: StorageReference
    public val state: String
    public val task: UploadTask
    public val totalBytes: Number
}

public external class StorageMetadata {
    public val bucket: String?
    public var cacheControl: String?
    public var contentDisposition: String?
    public var contentEncoding: String?
    public var contentLanguage: String?
    public var contentType: String?
    public var customMetadata: Map<String, String>?
    public val fullPath: String?
    public val generation: String?
    public val md5Hash: String?
    public val metageneration: String?
    public val name: String?
    public val size: Number?
    public val timeCreated: String?
    public val updated: String?
}

public external class UploadTask : Promise<UploadTaskSnapshot> {
    public fun cancel(): Boolean
    public fun on(event: String, next: (snapshot: UploadTaskSnapshot) -> Unit, error: (a: StorageError) -> Unit, complete: () -> Unit): () -> Unit
    public fun pause(): Boolean
    public fun resume(): Boolean
    public val snapshot: UploadTaskSnapshot
}
