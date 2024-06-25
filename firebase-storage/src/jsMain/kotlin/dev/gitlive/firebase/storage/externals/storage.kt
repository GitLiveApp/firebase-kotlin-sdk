@file:JsModule("firebase/storage")
@file:JsNonModule

package dev.gitlive.firebase.storage.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.collections.JsMap

external fun getStorage(app: FirebaseApp? = definedExternally, bucketUrl: String): FirebaseStorage

external fun getStorage(app: FirebaseApp? = definedExternally): FirebaseStorage

external fun ref(storage: FirebaseStorage, url: String? = definedExternally): StorageReference
external fun ref(ref: StorageReference, path: String? = definedExternally): StorageReference

external fun getDownloadURL(ref: StorageReference): Promise<String>

external fun getMetadata(ref: StorageReference): Promise<FullMetadata>
external fun updateMetadata(ref: StorageReference, metadata: SettableMetadata): Promise<FullMetadata>

external fun uploadBytes(ref: StorageReference, file: dynamic, metadata: Json?): Promise<UploadResult>
external fun uploadBytesResumable(ref: StorageReference, data: dynamic, metadata: Json?): UploadTask

external fun deleteObject(ref: StorageReference): Promise<Unit>

external fun list(ref: StorageReference, options: ListOptions?): Promise<ListResult>
external fun listAll(ref: StorageReference): Promise<ListResult>

external fun connectStorageEmulator(
    storage: FirebaseStorage,
    host: String,
    port: Double,
    options: Any? = definedExternally
)

external interface FirebaseStorage {
    var maxOperationRetryTime: Double
    var maxUploadRetryTime: Double
}

external interface StorageReference {
    val bucket: String
    val fullPath: String
    val name: String
    val parent: StorageReference?
    val root: StorageReference
    val storage: FirebaseStorage
}

external interface ListOptions {
    val maxResults: Double?
    val pageToken: String?
}

external interface ListResult {
    val items: Array<StorageReference>
    val nextPageToken: String
    val prefixes: Array<StorageReference>
}

external interface StorageError

external interface SettableMetadata {
    val cacheControl: String?
    val contentDisposition: String?
    val contentEncoding: String?
    val contentLanguage: String?
    val contentType: String?
    val customMetadata: Json?
}

external interface UploadMetadata : SettableMetadata {
    val md5Hash: String?
}

external interface FullMetadata : UploadMetadata {
    val bucket: String
    val downloadTokens: Array<String>?
    val fullPath: String
    val generation: String
    val metageneration: String
    val name: String
    val ref: StorageReference?
    val size: Double
    val timeCreated: String
    val updated: String
}

external interface UploadResult {
    val metadata: FullMetadata
    val ref: StorageReference
}

external interface UploadTask {
    fun cancel(): Boolean;
    fun on(event: String, next: (snapshot: UploadTaskSnapshot) -> Unit, error: (a: StorageError) -> Unit, complete: () -> Unit): () -> Unit
    fun pause(): Boolean;
    fun resume(): Boolean;
    fun then(onFulfilled: ((UploadTaskSnapshot) -> Unit)?, onRejected: ((StorageError) -> Unit)?): Promise<Unit>
    val snapshot: UploadTaskSnapshot
}

external interface UploadTaskSnapshot {
    val bytesTransferred: Double
    val ref: StorageReference
    val state: String
    val task: UploadTask
    val totalBytes: Double
}