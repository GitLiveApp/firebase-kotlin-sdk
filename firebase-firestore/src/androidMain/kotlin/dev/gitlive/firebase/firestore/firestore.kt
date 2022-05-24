/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import dev.gitlive.firebase.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy

actual val Firebase.firestore get() =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())

actual fun Firebase.firestore(app: FirebaseApp) =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance(app.android))

/** Helper method to perform an update operation. */
@JvmName("performUpdateFields")
private fun <R> performUpdate(
    fieldsAndValues: Array<out Pair<String, Any?>>,
    update: (String, Any?, Array<Any?>) -> R
) = performUpdate(fieldsAndValues, { it }, { encode(it, true) }, update)

/** Helper method to perform an update operation. */
@JvmName("performUpdateFieldPaths")
private fun <R> performUpdate(
    fieldsAndValues: Array<out Pair<FieldPath, Any?>>,
    update: (com.google.firebase.firestore.FieldPath, Any?, Array<Any?>) -> R
) = performUpdate(fieldsAndValues, { it.android }, { encode(it, true) }, update)

actual class FirebaseFirestore(val android: com.google.firebase.firestore.FirebaseFirestore) {

    actual fun collection(collectionPath: String) = CollectionReference(android.collection(collectionPath))

    actual fun collectionGroup(collectionId: String) = Query(android.collectionGroup(collectionId))

    actual fun document(documentPath: String) = DocumentReference(android.document(documentPath))

    actual fun batch() = WriteBatch(android.batch())

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        android.runTransaction { runBlocking { Transaction(it).func() } }.await()

    actual suspend fun clearPersistence() =
        android.clearPersistence().await().run { }

    actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
        android.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
    }

    actual fun setSettings(persistenceEnabled: Boolean?, sslEnabled: Boolean?, host: String?, cacheSizeBytes: Long?) {
        android.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder().also { builder ->
                persistenceEnabled?.let { builder.setPersistenceEnabled(it) }
                sslEnabled?.let { builder.isSslEnabled = it }
                host?.let { builder.host = it }
                cacheSizeBytes?.let { builder.cacheSizeBytes = it }
            }.build()
        }

    actual suspend fun disableNetwork() =
        android.disableNetwork().await().run { }

    actual suspend fun enableNetwork() =
        android.enableNetwork().await().run { }

}

actual class WriteBatch(val android: com.google.firebase.firestore.WriteBatch) {

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, encode(data, encodeDefaults)!!, SetOptions.merge())
        false -> android.set(documentRef.android, encode(data, encodeDefaults)!!)
    }.let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        android.set(documentRef.android, encode(data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        android.set(documentRef.android, encode(data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, encode(strategy, data, encodeDefaults)!!, SetOptions.merge())
        false -> android.set(documentRef.android, encode(strategy, data, encodeDefaults)!!)
    }.let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        android.set(documentRef.android, encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        android.set(documentRef.android, encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .let { this }

    @Suppress("UNCHECKED_CAST")
    actual inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean) =
        android.update(documentRef.android, encode(data, encodeDefaults) as Map<String, Any>).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        android.update(documentRef.android, encode(strategy, data, encodeDefaults) as Map<String, Any>).let { this }

    @JvmName("updateFields")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            android.update(documentRef.android, field, value, *moreFieldsAndValues)
        }.let { this }

    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            android.update(documentRef.android, field, value, *moreFieldsAndValues)
        }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun commit() = android.commit().await().run { Unit }

}

actual class Transaction(val android: com.google.firebase.firestore.Transaction) {

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, encode(data, encodeDefaults)!!, SetOptions.merge())
        false -> android.set(documentRef.android, encode(data, encodeDefaults)!!)
    }.let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) =
        android.set(documentRef.android, encode(data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        android.set(documentRef.android, encode(data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .let { this }

    actual fun <T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        encodeDefaults: Boolean,
        merge: Boolean
    ) = when(merge) {
        true -> android.set(documentRef.android, encode(strategy, data, encodeDefaults)!!, SetOptions.merge())
        false -> android.set(documentRef.android, encode(strategy, data, encodeDefaults)!!)
    }.let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        android.set(documentRef.android, encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        android.set(documentRef.android, encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) =
        android.update(documentRef.android, encode(data, encodeDefaults) as Map<String, Any>).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        android.update(documentRef.android, encode(strategy, data, encodeDefaults) as Map<String, Any>).let { this }

    @JvmName("updateFields")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            android.update(documentRef.android, field, value, *moreFieldsAndValues)
        }.let { this }

    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            android.update(documentRef.android, field, value, *moreFieldsAndValues)
        }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        DocumentSnapshot(android.get(documentRef.android))
}

actual class DocumentReference(val android: com.google.firebase.firestore.DocumentReference) {

    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual val parent: CollectionReference
        get() = CollectionReference(android.parent)

    actual fun collection(collectionPath: String) = CollectionReference(android.collection(collectionPath))

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> android.set(encode(data, encodeDefaults)!!, SetOptions.merge())
        false -> android.set(encode(data, encodeDefaults)!!)
    }.await().run { Unit }

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        android.set(encode(data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .await().run { Unit }

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        android.set(encode(data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .await().run { Unit }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> android.set(encode(strategy, data, encodeDefaults)!!, SetOptions.merge())
        false -> android.set(encode(strategy, data, encodeDefaults)!!)
    }.await().run { Unit }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        android.set(encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .await().run { Unit }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        android.set(encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .await().run { Unit }

    @Suppress("UNCHECKED_CAST")
    actual suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean) =
        android.update(encode(data, encodeDefaults) as Map<String, Any>).await().run { Unit }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        android.update(encode(strategy, data, encodeDefaults) as Map<String, Any>).await().run { Unit }

    @JvmName("updateFields")
    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) =
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            android.update(field, value, *moreFieldsAndValues)
        }?.await()
            .run { Unit }

    @JvmName("updateFieldPaths")
    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            android.update(field, value, *moreFieldsAndValues)
        }?.await()
            .run { Unit }

    actual suspend fun delete() =
        android.delete().await().run { Unit }

    actual suspend fun get() =
        DocumentSnapshot(android.get().await())

    actual val snapshots get() = callbackFlow<DocumentSnapshot> {
        val listener = android.addSnapshotListener { snapshot, exception ->
            snapshot?.let { trySend(DocumentSnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }
}

actual open class Query(open val android: com.google.firebase.firestore.Query) {

    actual suspend fun get() = QuerySnapshot(android.get().await())

    actual fun limit(limit: Number) = Query(android.limit(limit.toLong()))

    actual val snapshots get() = callbackFlow<QuerySnapshot> {
        val listener = android.addSnapshotListener { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow<QuerySnapshot> {
        val metadataChanges = if(includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val listener = android.addSnapshotListener(metadataChanges) { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    internal actual fun _where(field: String, equalTo: Any?) = Query(android.whereEqualTo(field, equalTo))
    internal actual fun _where(path: FieldPath, equalTo: Any?) = Query(android.whereEqualTo(path.android, equalTo))

    internal actual fun _where(field: String, equalTo: DocumentReference) = Query(android.whereEqualTo(field, equalTo.android))
    internal actual fun _where(path: FieldPath, equalTo: DocumentReference) = Query(android.whereEqualTo(path.android, equalTo.android))

    internal actual fun _where(field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { android.whereLessThan(field, it) } ?: android).let { android2 ->
            (greaterThan?.let { android2.whereGreaterThan(field, it) } ?: android2).let { android3 ->
                arrayContains?.let { android3.whereArrayContains(field, it) } ?: android3
            }
        }
    )

    internal actual fun _where(path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { android.whereLessThan(path.android, it) } ?: android).let { android2 ->
            (greaterThan?.let { android2.whereGreaterThan(path.android, it) } ?: android2).let { android3 ->
                arrayContains?.let { android3.whereArrayContains(path.android, it) } ?: android3
            }
        }
    )

    internal actual fun _where(field: String, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { android.whereIn(field, it) } ?: android).let { android2 ->
            arrayContainsAny?.let { android2.whereArrayContainsAny(field, it) } ?: android2
        }
    )

    internal actual fun _where(path: FieldPath, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { android.whereIn(path.android, it) } ?: android).let { android2 ->
            arrayContainsAny?.let { android2.whereArrayContainsAny(path.android, it) } ?: android2
        }
    )

    internal actual fun _orderBy(field: String, direction: Direction) = Query(android.orderBy(field, direction))
    internal actual fun _orderBy(field: FieldPath, direction: Direction) = Query(android.orderBy(field.android, direction))

    internal actual fun _startAfter(document: DocumentSnapshot) = Query(android.startAfter(document.android))
    internal actual fun _startAfter(vararg fieldValues: Any) = Query(android.startAfter(*fieldValues))
    internal actual fun _startAt(document: DocumentSnapshot) = Query(android.startAt(document.android))
    internal actual fun _startAt(vararg fieldValues: Any) = Query(android.startAt(*fieldValues))

    internal actual fun _endBefore(document: DocumentSnapshot) = Query(android.endBefore(document.android))
    internal actual fun _endBefore(vararg fieldValues: Any) = Query(android.endBefore(*fieldValues))
    internal actual fun _endAt(document: DocumentSnapshot) = Query(android.endAt(document.android))
    internal actual fun _endAt(vararg fieldValues: Any) = Query(android.endAt(*fieldValues))
}

actual typealias Direction = com.google.firebase.firestore.Query.Direction
actual typealias ChangeType = com.google.firebase.firestore.DocumentChange.Type

actual class CollectionReference(override val android: com.google.firebase.firestore.CollectionReference) : Query(android) {

    actual val path: String
        get() = android.path

    actual val document: DocumentReference
        get() = DocumentReference(android.document())

    actual val parent: DocumentReference?
        get() = android.parent?.let{DocumentReference(it)}

    actual fun document(documentPath: String) = DocumentReference(android.document(documentPath))

    actual suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean) =
        DocumentReference(android.add(encode(data, encodeDefaults)!!).await())

    actual suspend fun <T> add(data: T, strategy: SerializationStrategy<T>, encodeDefaults: Boolean) =
        DocumentReference(android.add(encode(strategy, data, encodeDefaults)!!).await())
    actual suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        DocumentReference(android.add(encode(strategy, data, encodeDefaults)!!).await())
}

actual typealias FirebaseFirestoreException = com.google.firebase.firestore.FirebaseFirestoreException

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual typealias FirestoreExceptionCode = com.google.firebase.firestore.FirebaseFirestoreException.Code

actual class QuerySnapshot(val android: com.google.firebase.firestore.QuerySnapshot) {
    actual val documents
        get() = android.documents.map { DocumentSnapshot(it) }
    actual val documentChanges
        get() = android.documentChanges.map { DocumentChange(it) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(android.metadata)
}

actual class DocumentChange(val android: com.google.firebase.firestore.DocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(android.document)
    actual val newIndex: Int
        get() = android.newIndex
    actual val oldIndex: Int
        get() = android.oldIndex
    actual val type: ChangeType
        get() = android.type
}

@Suppress("UNCHECKED_CAST")
actual class DocumentSnapshot(val android: com.google.firebase.firestore.DocumentSnapshot) {

    actual val id get() = android.id
    actual val reference get() = DocumentReference(android.reference)

    actual inline fun <reified T: Any> data(serverTimestampBehavior: ServerTimestampBehavior): T =
        decode(value = android.getData(serverTimestampBehavior.toAndroid()))

    actual fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior): T =
        decode(strategy, android.getData(serverTimestampBehavior.toAndroid()))

    actual inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior): T =
        decode(value = android.get(field, serverTimestampBehavior.toAndroid()))

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior): T =
        decode(strategy, android.get(field, serverTimestampBehavior.toAndroid()))

    actual fun contains(field: String) = android.contains(field)

    actual val exists get() = android.exists()

    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(android.metadata)

    fun ServerTimestampBehavior.toAndroid(): com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior = when (this) {
        ServerTimestampBehavior.ESTIMATE -> com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ServerTimestampBehavior.NONE -> com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.NONE
        ServerTimestampBehavior.PREVIOUS -> com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.PREVIOUS
    }
}

actual class SnapshotMetadata(val android: com.google.firebase.firestore.SnapshotMetadata) {
    actual val hasPendingWrites: Boolean get() = android.hasPendingWrites()
    actual val isFromCache: Boolean get() = android.isFromCache()
}

actual class FieldPath private constructor(val android: com.google.firebase.firestore.FieldPath) {
    actual constructor(vararg fieldNames: String) : this(com.google.firebase.firestore.FieldPath.of(*fieldNames))
    actual val documentId: FieldPath get() = FieldPath(com.google.firebase.firestore.FieldPath.documentId())
}

/** Represents a platform specific Firebase FieldValue. */
private typealias NativeFieldValue = com.google.firebase.firestore.FieldValue

/** Represents a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
actual class FieldValue internal actual constructor(internal actual val nativeValue: Any) {
    init {
        require(nativeValue is NativeFieldValue)
    }
    override fun equals(other: Any?): Boolean =
        this === other || other is FieldValue && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    actual companion object {
        actual val serverTimestamp: FieldValue get() = FieldValue(NativeFieldValue.serverTimestamp())
        actual val delete: FieldValue get() = FieldValue(NativeFieldValue.delete())
        actual fun increment(value: Int): FieldValue = FieldValue(NativeFieldValue.increment(value.toDouble()))
        actual fun arrayUnion(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.arrayUnion(*elements))
        actual fun arrayRemove(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.arrayRemove(*elements))
    }
}
