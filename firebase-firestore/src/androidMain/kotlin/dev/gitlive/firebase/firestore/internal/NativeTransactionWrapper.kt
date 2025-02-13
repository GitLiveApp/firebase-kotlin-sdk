package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.firestore.android
import dev.gitlive.firebase.firestore.performUpdate
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.android

internal actual class NativeTransactionWrapper internal actual constructor(actual val native: NativeTransaction) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions,
    ): NativeTransactionWrapper {
        setOptions.android?.let {
            native.set(documentRef.android, encodedData.android, it)
        } ?: native.set(documentRef.android, encodedData.android)
        return this
    }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject) = native.update(documentRef.android, encodedData.android).let { this }

    actual fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>,
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        native.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>,
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        native.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.delete(documentRef.android).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        NativeDocumentSnapshotWrapper(native.get(documentRef.android))
}
