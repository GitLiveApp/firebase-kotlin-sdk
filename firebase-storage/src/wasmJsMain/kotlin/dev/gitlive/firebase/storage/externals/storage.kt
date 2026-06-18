@file:JsModule("firebase/storage")

package dev.gitlive.firebase.storage.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun getStorage(app: FirebaseApp? = definedExternally, bucketUrl: String): FirebaseStorage

public external fun getStorage(app: FirebaseApp? = definedExternally): FirebaseStorage

public external fun ref(storage: FirebaseStorage, url: String? = definedExternally): StorageReference
public external fun ref(ref: StorageReference, path: String? = definedExternally): StorageReference

public external fun getDownloadURL(ref: StorageReference): Promise<JsString>

public external fun getMetadata(ref: StorageReference): Promise<FullMetadata>
public external fun updateMetadata(ref: StorageReference, metadata: SettableMetadata): Promise<FullMetadata>

public external fun uploadBytes(ref: StorageReference, file: JsAny?, metadata: JsAny?): Promise<UploadResult>
public external fun uploadBytesResumable(ref: StorageReference, data: JsAny?, metadata: JsAny?): UploadTask

public external fun deleteObject(ref: StorageReference): Promise<JsAny?>

public external fun list(ref: StorageReference, options: ListOptions?): Promise<ListResult>
public external fun listAll(ref: StorageReference): Promise<ListResult>

public external fun connectStorageEmulator(
    storage: FirebaseStorage,
    host: String,
    port: Double,
    options: JsAny? = definedExternally,
)

public external interface FirebaseStorage : JsAny {
    public var maxOperationRetryTime: Double
    public var maxUploadRetryTime: Double
}

public external interface StorageReference : JsAny {
    public val bucket: String
    public val fullPath: String
    public val name: String
    public val parent: StorageReference?
    public val root: StorageReference
    public val storage: FirebaseStorage
}

public external interface ListOptions : JsAny {
    public val maxResults: Double?
    public val pageToken: String?
}

public external interface ListResult : JsAny {
    public val items: JsArray<StorageReference>
    public val nextPageToken: String
    public val prefixes: JsArray<StorageReference>
}

public external interface StorageError : JsAny

public external interface SettableMetadata : JsAny {
    public val cacheControl: String?
    public val contentDisposition: String?
    public val contentEncoding: String?
    public val contentLanguage: String?
    public val contentType: String?
    public val customMetadata: JsAny?
}

public external interface UploadMetadata : SettableMetadata {
    public val md5Hash: String?
}

public external interface FullMetadata : UploadMetadata {
    public val bucket: String
    public val downloadTokens: JsArray<JsString>?
    public val fullPath: String
    public val generation: String
    public val metageneration: String
    public val name: String
    public val ref: StorageReference?
    public val size: Double
    public val timeCreated: String
    public val updated: String
}

public external interface UploadResult : JsAny {
    public val metadata: FullMetadata
    public val ref: StorageReference
}

public external interface UploadTask : JsAny {
    public fun cancel(): Boolean
    public fun on(event: String, next: (snapshot: UploadTaskSnapshot) -> Unit, error: (a: StorageError) -> Unit, complete: () -> Unit): () -> Unit
    public fun pause(): Boolean
    public fun resume(): Boolean
    public fun then(onFulfilled: ((UploadTaskSnapshot) -> Unit)?, onRejected: ((StorageError) -> Unit)?): Promise<JsAny?>
    public val snapshot: UploadTaskSnapshot
}

public external interface UploadTaskSnapshot : JsAny {
    public val bytesTransferred: Double
    public val ref: StorageReference
    public val state: String
    public val task: UploadTask
    public val totalBytes: Double
}
