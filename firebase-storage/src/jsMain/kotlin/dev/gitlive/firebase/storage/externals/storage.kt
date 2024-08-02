@file:JsModule("firebase/storage")
@file:JsNonModule

package dev.gitlive.firebase.storage.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Json
import kotlin.js.Promise

public external fun getStorage(app: FirebaseApp? = definedExternally, bucketUrl: String): FirebaseStorage

public external fun getStorage(app: FirebaseApp? = definedExternally): FirebaseStorage

public external fun ref(storage: FirebaseStorage, url: String? = definedExternally): StorageReference
public external fun ref(ref: StorageReference, path: String? = definedExternally): StorageReference

public external fun getDownloadURL(ref: StorageReference): Promise<String>

public external fun getMetadata(ref: StorageReference): Promise<FullMetadata>
public external fun updateMetadata(ref: StorageReference, metadata: SettableMetadata): Promise<FullMetadata>

public external fun uploadBytes(ref: StorageReference, file: dynamic, metadata: Json?): Promise<UploadResult>
public external fun uploadBytesResumable(ref: StorageReference, data: dynamic, metadata: Json?): UploadTask

public external fun deleteObject(ref: StorageReference): Promise<Unit>

public external fun list(ref: StorageReference, options: ListOptions?): Promise<ListResult>
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

public external interface ListOptions {
    public val maxResults: Double?
    public val pageToken: String?
}

public external interface ListResult {
    public val items: Array<StorageReference>
    public val nextPageToken: String
    public val prefixes: Array<StorageReference>
}

public external interface StorageError

public external interface SettableMetadata {
    public val cacheControl: String?
    public val contentDisposition: String?
    public val contentEncoding: String?
    public val contentLanguage: String?
    public val contentType: String?
    public val customMetadata: Json?
}

public external interface UploadMetadata : SettableMetadata {
    public val md5Hash: String?
}

public external interface FullMetadata : UploadMetadata {
    public val bucket: String
    public val downloadTokens: Array<String>?
    public val fullPath: String
    public val generation: String
    public val metageneration: String
    public val name: String
    public val ref: StorageReference?
    public val size: Double
    public val timeCreated: String
    public val updated: String
}

public external interface UploadResult {
    public val metadata: FullMetadata
    public val ref: StorageReference
}

public external interface UploadTask {
    public fun cancel(): Boolean
    public fun on(event: String, next: (snapshot: UploadTaskSnapshot) -> Unit, error: (a: StorageError) -> Unit, complete: () -> Unit): () -> Unit
    public fun pause(): Boolean
    public fun resume(): Boolean
    public fun then(onFulfilled: ((UploadTaskSnapshot) -> Unit)?, onRejected: ((StorageError) -> Unit)?): Promise<Unit>
    public val snapshot: UploadTaskSnapshot
}

public external interface UploadTaskSnapshot {
    public val bytesTransferred: Double
    public val ref: StorageReference
    public val state: String
    public val task: UploadTask
    public val totalBytes: Double
}
