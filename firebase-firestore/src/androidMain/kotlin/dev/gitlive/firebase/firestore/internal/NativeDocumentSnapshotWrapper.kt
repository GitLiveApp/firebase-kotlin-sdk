package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.SnapshotMetadata

@PublishedApi
internal actual class NativeDocumentSnapshotWrapper internal actual constructor(actual val native: com.google.firebase.firestore.DocumentSnapshot) {

    actual val id get() = native.id
    actual val reference get() = NativeDocumentReference(native.reference)

    actual fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? = native.get(field, serverTimestampBehavior.toAndroid())
    actual fun getEncoded(fieldPath: EncodedFieldPath, serverTimestampBehavior: ServerTimestampBehavior): Any? = native.get(fieldPath, serverTimestampBehavior.toAndroid())
    actual fun encodedData(serverTimestampBehavior: ServerTimestampBehavior): Any? = native.getData(serverTimestampBehavior.toAndroid())

    actual fun contains(field: String) = native.contains(field)
    actual fun contains(fieldPath: EncodedFieldPath) = native.contains(fieldPath)

    actual val exists get() = native.exists()

    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(native.metadata)

    fun ServerTimestampBehavior.toAndroid(): com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior = when (this) {
        ServerTimestampBehavior.ESTIMATE -> com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ServerTimestampBehavior.NONE -> com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.NONE
        ServerTimestampBehavior.PREVIOUS -> com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.PREVIOUS
    }
}
