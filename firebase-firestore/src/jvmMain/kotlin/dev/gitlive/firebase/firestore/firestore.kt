/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("jvm")
package dev.gitlive.firebase.firestore

import com.google.cloud.Timestamp
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.SetOptions
import com.google.firebase.ErrorCode
import com.google.firebase.cloud.FirestoreClient
import dev.gitlive.firebase.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

@PublishedApi
internal inline fun <reified T> decode(value: Any?): T =
    decode(value) { (it as? Timestamp)?.run { seconds * 1000 + (nanos / 1000000.0) } }

internal fun <T> decode(strategy: DeserializationStrategy<T>, value: Any?): T =
    decode(strategy, value) { (it as? Timestamp)?.run { seconds * 1000 + (nanos / 1000000.0) } }

@PublishedApi
internal inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean) =
    encode(value, shouldEncodeElementDefault, com.google.cloud.firestore.FieldValue.serverTimestamp())

private fun <T> encode(strategy: SerializationStrategy<T> , value: T, shouldEncodeElementDefault: Boolean): Any? =
    encode(strategy, value, shouldEncodeElementDefault, com.google.cloud.firestore.FieldValue.serverTimestamp())

actual val Firebase.firestore get() =
    FirebaseFirestore(FirestoreClient.getFirestore())

actual fun Firebase.firestore(app: FirebaseApp) =
    FirebaseFirestore(FirestoreClient.getFirestore(app.jvm))

actual class FirebaseFirestore(val jvm: Firestore) {

    actual fun collection(collectionPath: String) = CollectionReference(jvm.collection(collectionPath))

    actual fun collectionGroup(collectionId: String) = Query(jvm.collectionGroup(collectionId))

    actual fun document(documentPath: String) = DocumentReference(jvm.document(documentPath))

    actual fun batch() = WriteBatch(jvm.batch())

    actual fun setLoggingEnabled(loggingEnabled: Boolean) = Unit

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T =
        jvm.runTransaction { transaction -> runBlocking { Transaction(transaction).func() } }.await()

    actual suspend fun clearPersistence() = Unit

    actual fun useEmulator(host: String, port: Int) = Unit

    actual fun setSettings(persistenceEnabled: Boolean?, sslEnabled: Boolean?, host: String?, cacheSizeBytes: Long?) {
        Firebase.firestore.setSettings(
            persistenceEnabled,
            sslEnabled,
            host,
            cacheSizeBytes,
        )
    }

    actual suspend fun disableNetwork() = Unit

    actual suspend fun enableNetwork() = Unit
}

actual class WriteBatch(val jvm: com.google.cloud.firestore.WriteBatch) {
    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> jvm.set(documentRef.jvm, encode(data, encodeDefaults)!!, SetOptions.merge())
        false -> jvm.set(documentRef.jvm, encode(data, encodeDefaults)!!)
    }.let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        jvm.set(documentRef.jvm, encode(data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        jvm.set(documentRef.jvm, encode(data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> jvm.set(documentRef.jvm, encode(strategy, data, encodeDefaults)!!, SetOptions.merge())
        false -> jvm.set(documentRef.jvm, encode(strategy, data, encodeDefaults)!!)
    }.let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        jvm.set(documentRef.jvm, encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        jvm.set(documentRef.jvm, encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .let { this }

    @Suppress("UNCHECKED_CAST")
    actual inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean) =
        jvm.update(documentRef.jvm, encode(data, encodeDefaults) as Map<String, Any>).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        jvm.update(documentRef.jvm, encode(strategy, data, encodeDefaults) as Map<String, Any>).let { this }

    @JvmName("updateFields")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        jvm.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.jvm,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value, true) })
                }.toTypedArray()
            ).let { this }

    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        jvm.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.jvm,
                fieldsAndValues[0].first.android,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field.android, value?.let { encode(value, true) })
                }.toTypedArray()
            ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        jvm.delete(documentRef.jvm).let { this }

    actual suspend fun commit() = jvm.commit().await().run { Unit }

}

actual class Transaction(val jvm: com.google.cloud.firestore.Transaction) {

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> jvm.set(documentRef.jvm, encode(data, encodeDefaults)!!, SetOptions.merge())
        false -> jvm.set(documentRef.jvm, encode(data, encodeDefaults)!!)
    }.let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) =
        jvm.set(documentRef.jvm, encode(data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        jvm.set(documentRef.jvm, encode(data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .let { this }

    actual fun <T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        encodeDefaults: Boolean,
        merge: Boolean
    ) = when(merge) {
        true -> jvm.set(documentRef.jvm, encode(strategy, data, encodeDefaults)!!, SetOptions.merge())
        false -> jvm.set(documentRef.jvm, encode(strategy, data, encodeDefaults)!!)
    }.let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        jvm.set(documentRef.jvm, encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        jvm.set(documentRef.jvm, encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) =
        jvm.update(documentRef.jvm, encode(data, encodeDefaults) as Map<String, Any>).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        jvm.update(documentRef.jvm, encode(strategy, data, encodeDefaults) as Map<String, Any>).let { this }

    @JvmName("updateFields")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        jvm.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.jvm,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value, true) })
                }.toTypedArray()
            ).let { this }

    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        jvm.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.jvm,
                fieldsAndValues[0].first.android,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field.android, value?.let { encode(value, true) })
                }.toTypedArray()
            ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        jvm.delete(documentRef.jvm).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        DocumentSnapshot(jvm.get(documentRef.jvm).await())
}

actual class DocumentReference(val jvm: com.google.cloud.firestore.DocumentReference) {

    actual val id: String
        get() = jvm.id

    actual val path: String
        get() = jvm.path

    actual fun collection(collectionPath: String) = CollectionReference(jvm.collection(collectionPath))

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> jvm.set(encode(data, encodeDefaults)!!, SetOptions.merge())
        false -> jvm.set(encode(data, encodeDefaults)!!)
    }.await().run { Unit }

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        jvm.set(encode(data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .await().run { Unit }

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        jvm.set(encode(data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .await().run { Unit }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
        true -> jvm.set(encode(strategy, data, encodeDefaults)!!, SetOptions.merge())
        false -> jvm.set(encode(strategy, data, encodeDefaults)!!)
    }.await().run { Unit }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        jvm.set(encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
            .await().run { Unit }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        jvm.set(encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
            .await().run { Unit }

    @Suppress("UNCHECKED_CAST")
    actual suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean) =
        jvm.update(encode(data, encodeDefaults) as Map<String, Any>).await().run { Unit }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        jvm.update(encode(strategy, data, encodeDefaults) as Map<String, Any>).await().run { Unit }

    @JvmName("updateFields")
    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) =
        jvm.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value, true) })
                }.toTypedArray()
            )
            ?.await()
            .run { Unit }

    @JvmName("updateFieldPaths")
    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        jvm.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                fieldsAndValues[0].first.android,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field.android, value?.let { encode(value, true) })
                }.toTypedArray()
            )
            ?.await()
            .run { Unit }

    actual suspend fun delete() =
        jvm.delete().await().run { Unit }

    actual suspend fun get() =
        DocumentSnapshot(jvm.get().await())

    actual val snapshots get() = callbackFlow<DocumentSnapshot> {
        val listener = jvm.addSnapshotListener { snapshot, exception ->
            snapshot?.let { safeOffer(DocumentSnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }
}

actual open class Query(open val jvm: com.google.cloud.firestore.Query) {

    actual suspend fun get() = QuerySnapshot(jvm.get().await())

    actual fun limit(limit: Number) = Query(jvm.limit(limit.toInt()))

    actual val snapshots get() = callbackFlow {
        val listener = jvm.addSnapshotListener { snapshot, exception ->
            snapshot?.let { safeOffer(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    internal actual fun _where(field: String, equalTo: Any?) = Query(jvm.whereEqualTo(field, equalTo))
    internal actual fun _where(path: FieldPath, equalTo: Any?) = Query(jvm.whereEqualTo(path.android, equalTo))

    internal actual fun _where(field: String, equalTo: DocumentReference) = Query(jvm.whereEqualTo(field, equalTo.jvm))
    internal actual fun _where(path: FieldPath, equalTo: DocumentReference) = Query(jvm.whereEqualTo(path.android, equalTo.jvm))

    internal actual fun _where(field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { jvm.whereLessThan(field, it) } ?: jvm).let { android2 ->
            (greaterThan?.let { android2.whereGreaterThan(field, it) } ?: android2).let { android3 ->
                arrayContains?.let { android3.whereArrayContains(field, it) } ?: android3
            }
        }
    )

    internal actual fun _where(path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { jvm.whereLessThan(path.android, it) } ?: jvm).let { android2 ->
            (greaterThan?.let { android2.whereGreaterThan(path.android, it) } ?: android2).let { android3 ->
                arrayContains?.let { android3.whereArrayContains(path.android, it) } ?: android3
            }
        }
    )

    internal actual fun _where(field: String, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { jvm.whereIn(field, it) } ?: jvm).let { android2 ->
            arrayContainsAny?.let { android2.whereArrayContainsAny(field, it) } ?: android2
        }
    )

    internal actual fun _where(path: FieldPath, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { jvm.whereIn(path.android, it) } ?: jvm).let { android2 ->
            arrayContainsAny?.let { android2.whereArrayContainsAny(path.android, it) } ?: android2
        }
    )

    internal actual fun _orderBy(field: String, direction: Direction) = Query(jvm.orderBy(field, direction))
    internal actual fun _orderBy(field: FieldPath, direction: Direction) = Query(jvm.orderBy(field.android, direction))
}

actual typealias Direction = com.google.cloud.firestore.Query.Direction
actual typealias ChangeType = com.google.cloud.firestore.DocumentChange.Type

actual class CollectionReference(override val jvm: com.google.cloud.firestore.CollectionReference) : Query(jvm) {

    actual val path: String
        get() = jvm.path

    actual val document: DocumentReference
        get() = DocumentReference(jvm.document())

    actual fun document(documentPath: String) = DocumentReference(jvm.document(documentPath))

    actual suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean) =
        DocumentReference(jvm.add(encode(data, encodeDefaults)!!).await()!!)

    actual suspend fun <T> add(data: T, strategy: SerializationStrategy<T>, encodeDefaults: Boolean) =
        DocumentReference(jvm.add(encode(strategy, data, encodeDefaults)!!).await()!!)
    actual suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        DocumentReference(jvm.add(encode(strategy, data, encodeDefaults)!!).await()!!)
}

actual typealias FirebaseFirestoreException = FirestoreException

actual val FirebaseFirestoreException.code: FirestoreExceptionCode
    get() = when(errorCode) {
        ErrorCode.INVALID_ARGUMENT -> FirestoreExceptionCode.INVALID_ARGUMENT
        ErrorCode.CANCELLED -> FirestoreExceptionCode.CANCELLED
        ErrorCode.ABORTED -> FirestoreExceptionCode.ABORTED
        ErrorCode.UNKNOWN -> FirestoreExceptionCode.UNKNOWN
        ErrorCode.ALREADY_EXISTS -> FirestoreExceptionCode.ALREADY_EXISTS
        ErrorCode.NOT_FOUND -> FirestoreExceptionCode.NOT_FOUND
        ErrorCode.DEADLINE_EXCEEDED -> FirestoreExceptionCode.DEADLINE_EXCEEDED
        ErrorCode.UNAVAILABLE -> FirestoreExceptionCode.UNAVAILABLE
        ErrorCode.UNAUTHENTICATED -> FirestoreExceptionCode.UNAUTHENTICATED
        ErrorCode.PERMISSION_DENIED -> FirestoreExceptionCode.PERMISSION_DENIED
        ErrorCode.RESOURCE_EXHAUSTED -> FirestoreExceptionCode.RESOURCE_EXHAUSTED
        ErrorCode.FAILED_PRECONDITION -> FirestoreExceptionCode.FAILED_PRECONDITION
        ErrorCode.OUT_OF_RANGE -> FirestoreExceptionCode.OUT_OF_RANGE
        ErrorCode.DATA_LOSS -> FirestoreExceptionCode.DATA_LOSS
        else -> FirestoreExceptionCode.OK
    }

actual enum class FirestoreExceptionCode {
    OK,
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED
}

actual class QuerySnapshot(val jvm: com.google.cloud.firestore.QuerySnapshot) {
    actual val documents
        get() = jvm.documents.map { DocumentSnapshot(it) }
    actual val documentChanges
        get() = jvm.documentChanges.map { DocumentChange(it) }
    actual val metadata: SnapshotMetadata get() = error("Snapshot Metadata is not supported on JVM.")
}

actual class DocumentChange(val android: com.google.cloud.firestore.DocumentChange) {
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
actual class DocumentSnapshot(val android: com.google.cloud.firestore.DocumentSnapshot) {

    actual val id get() = android.id
    actual val reference get() = DocumentReference(android.reference)

    actual inline fun <reified T: Any> data(): T = decode<T>(value = android.data)

    actual fun <T> data(strategy: DeserializationStrategy<T>) = decode(strategy, android.data)

    actual fun dataMap(): Map<String, Any?> = android.data ?: emptyMap()

    actual inline fun <reified T> get(field: String) = decode<T>(value = android.get(field))

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>) =
        decode(strategy, android.get(field))

    actual fun contains(field: String) = android.contains(field)

    actual val exists get() = android.exists()

    actual val metadata: SnapshotMetadata get() = SnapshotMetadata()
}

actual class SnapshotMetadata {
    init {
        error("Snapshot Metadata is not supported on the JVM")
    }
    actual val hasPendingWrites: Boolean get() = error("Snapshot Metadata is not supported on the JVM")
    actual val isFromCache: Boolean get() = error("Snapshot Metadata is not supported on the JVM")
}

actual class FieldPath private constructor(val android: com.google.cloud.firestore.FieldPath) {
    actual constructor(vararg fieldNames: String) : this(com.google.cloud.firestore.FieldPath.of(*fieldNames))
    actual val documentId: FieldPath get() = FieldPath(com.google.cloud.firestore.FieldPath.documentId())
}

actual object FieldValue {
    actual val serverTimestamp = Double.POSITIVE_INFINITY
    actual val delete: Any get() = com.google.cloud.firestore.FieldValue.delete()
    actual fun arrayUnion(vararg elements: Any): Any = com.google.cloud.firestore.FieldValue.arrayUnion(*elements)
    actual fun arrayRemove(vararg elements: Any): Any = com.google.cloud.firestore.FieldValue.arrayRemove(*elements)
    actual fun delete(): Any = delete
}

