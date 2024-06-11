@file:JsModule("firebase/storage")
@file:JsNonModule

package dev.gitlive.firebase.storage.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

external fun getStorage(app: FirebaseApp? = definedExternally): FirebaseStorage

external fun ref(storage: FirebaseStorage, url: String? = definedExternally): StorageReference
external fun ref(ref: StorageReference, url: String? = definedExternally): StorageReference

external fun getDownloadURL(ref: StorageReference): Promise<String>

external fun uploadBytes(ref: StorageReference, file: dynamic): Promise<Unit>

external fun uploadBytesResumable(ref: StorageReference, data: dynamic): UploadTask

external fun deleteObject(ref: StorageReference): Promise<Unit>

external fun listAll(ref: StorageReference): Promise<ListResult>

external fun connectFirestoreEmulator(
    storage: FirebaseStorage,
    host: String,
    port: Double,
    options: Any? = definedExternally,
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

open external class ListResult {
    val items: Array<StorageReference>
    val nextPageToken: String
    val prefixes: Array<StorageReference>
}

external interface StorageError

external interface UploadTaskSnapshot {
    val bytesTransferred: Number
    val ref: StorageReference
    val state: String
    val task: UploadTask
    val totalBytes: Number
}

external class UploadTask : Promise<UploadTaskSnapshot> {
    fun cancel(): Boolean
    fun on(event: String, next: (snapshot: UploadTaskSnapshot) -> Unit, error: (a: StorageError) -> Unit, complete: () -> Unit): () -> Unit
    fun pause(): Boolean
    fun resume(): Boolean
    val snapshot: UploadTaskSnapshot
}
