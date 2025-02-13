package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.SnapshotMetadata

internal expect class NativeDocumentSnapshotWrapper internal constructor(native: NativeDocumentSnapshot) {

    val native: NativeDocumentSnapshot

    val exists: Boolean
    val id: String
    val reference: NativeDocumentReference
    val metadata: SnapshotMetadata

    fun contains(field: String): Boolean
    fun contains(fieldPath: EncodedFieldPath): Boolean

    fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?
    fun getEncoded(fieldPath: EncodedFieldPath, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?
    fun encodedData(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any?
}
