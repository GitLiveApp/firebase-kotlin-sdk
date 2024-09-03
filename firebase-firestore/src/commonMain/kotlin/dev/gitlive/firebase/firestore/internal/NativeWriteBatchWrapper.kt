package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.NativeWriteBatch
import dev.gitlive.firebase.internal.EncodedObject

internal expect class NativeWriteBatchWrapper internal constructor(native: NativeWriteBatch) {
    val native: NativeWriteBatch
    fun setEncoded(documentRef: DocumentReference, encodedData: EncodedObject, setOptions: SetOptions): NativeWriteBatchWrapper
    fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeWriteBatchWrapper
    fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): NativeWriteBatchWrapper
    fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): NativeWriteBatchWrapper
    fun delete(documentRef: DocumentReference): NativeWriteBatchWrapper
    suspend fun commit()
}
