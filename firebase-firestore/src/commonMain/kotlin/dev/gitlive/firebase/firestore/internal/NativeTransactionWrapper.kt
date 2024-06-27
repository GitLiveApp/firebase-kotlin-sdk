package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.internal.EncodedObject

@PublishedApi
internal expect class NativeTransactionWrapper internal constructor(native: NativeTransaction) {

    val native: NativeTransaction

    fun setEncoded(documentRef: DocumentReference, encodedData: EncodedObject, setOptions: SetOptions): NativeTransactionWrapper
    fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeTransactionWrapper
    fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): NativeTransactionWrapper
    fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): NativeTransactionWrapper
    fun delete(documentRef: DocumentReference): NativeTransactionWrapper
    suspend fun get(documentRef: DocumentReference): NativeDocumentSnapshotWrapper
}
