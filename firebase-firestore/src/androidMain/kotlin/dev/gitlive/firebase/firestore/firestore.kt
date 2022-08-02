/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.SetOptions
import dev.gitlive.firebase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy

actual val Firebase.firestore get() =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())

actual fun Firebase.firestore(app: FirebaseApp) =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance(app.android))

@Suppress("DeferredIsResult")
@PublishedApi
internal fun Task<Void>.asUnitDeferred(): Deferred<Unit> = CompletableDeferred<Unit>()
    .apply {
        asDeferred().invokeOnCompletion { exception ->
            if (exception == null) complete(Unit) else completeExceptionally(exception)
        }
    }

actual class FirebaseFirestore(val android: com.google.firebase.firestore.FirebaseFirestore) {

    actual fun collection(collectionPath: String) = CollectionReference(android.collection(collectionPath))

    actual fun document(documentPath: String) = DocumentReference(android.document(documentPath))

    actual fun collectionGroup(collectionId: String) = Query(android.collectionGroup(collectionId))

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

    actual val async = Async(android)

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

    actual fun <T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        encodeDefaults: Boolean,
        merge: Boolean,
        vararg fieldsAndValues: Pair<String, Any?>
    ): WriteBatch {
        val serializedItem = encode(strategy, data, encodeDefaults) as Map<String, *>
        val serializedFieldAndValues = fieldsAndValues.associate { (field, value) ->
            field to encode(value, encodeDefaults)
        }

        val result = serializedItem + serializedFieldAndValues
        if (merge) {
            android.set(documentRef.android, result, SetOptions.merge())
        } else {
            android.set(documentRef.android, result)
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    actual inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean) =
        android.update(documentRef.android, encode(data, encodeDefaults) as Map<String, Any>).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        android.update(documentRef.android, encode(strategy, data, encodeDefaults) as Map<String, Any>).let { this }

    @JvmName("updateFields")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
            ?.map { (field, value) -> field to encode(value, true) }
            ?.let { encoded -> android.update(documentRef.android, encoded.toMap()) }
            .let { this }

    @JvmName("updateFieldsExtended")
    actual inline fun <reified T> update(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        encodeDefaults: Boolean,
        vararg fieldsAndValues: Pair<String, Any?>
    ): WriteBatch {
        val serializedItem = encode(strategy, data, encodeDefaults) as Map<String, *>
        val serializedFieldAndValues = fieldsAndValues.associate { (field, value) ->
            field to encode(value, encodeDefaults)
        }

        val result = serializedItem + serializedFieldAndValues
        return android.update(documentRef.android, result).let { this }
    }


    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
            ?.map { (field, value) -> field.android to encode(value, true) }
            ?.let { encoded ->
                android.update(
                    documentRef.android,
                    encoded.first().first,
                    encoded.first().second,
                    *encoded.drop(1)
                        .flatMap { (field, value) -> listOf(field, value) }
                        .toTypedArray()
                )
            }
            .let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun commit() = async.commit().await()

    @Suppress("DeferredIsResult")
    actual class Async(private val android: com.google.firebase.firestore.WriteBatch) {
        actual fun commit(): Deferred<Unit> = android.commit().asUnitDeferred()
    }
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
        fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
            ?.map { (field, value) -> field to encode(value, true) }
            ?.let { encoded -> android.update(documentRef.android, encoded.toMap()) }
            .let { this }

    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
            ?.map { (field, value) -> field.android to encode(value, true) }
            ?.let { encoded ->
                android.update(
                    documentRef.android,
                    encoded.first().first,
                    encoded.first().second,
                    *encoded.drop(1)
                        .flatMap { (field, value) -> listOf(field, value) }
                        .toTypedArray()
                )
            }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        DocumentSnapshot(android.get(documentRef.android))
}

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReference = com.google.firebase.firestore.DocumentReference

@Serializable(with = DocumentReferenceSerializer::class)
actual class DocumentReference actual constructor(internal actual val nativeValue: NativeDocumentReference) {
    val android: NativeDocumentReference = nativeValue

    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual val async = Async(android)

    actual fun collection(collectionPath: String) = CollectionReference(android.collection(collectionPath))

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean) =
        async.set(data, encodeDefaults, merge).await()

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        async.set(data, encodeDefaults, mergeFields = mergeFields).await()

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        async.set(data, encodeDefaults, mergeFieldPaths = mergeFieldPaths).await()

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        async.set(strategy, data, encodeDefaults, merge).await()

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        async.set(strategy, data, encodeDefaults, mergeFields = mergeFields).await()

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        async.set(strategy, data, encodeDefaults, mergeFieldPaths = mergeFieldPaths).await()

    @Suppress("UNCHECKED_CAST")
    actual suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean) =
        async.update(data, encodeDefaults).await()

    @Suppress("UNCHECKED_CAST")
    actual suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        async.update(strategy, data, encodeDefaults).await()

    @JvmName("updateFields")
    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) =
        async.update(fieldsAndValues = fieldsAndValues).await()

    @JvmName("updateFieldPaths")
    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        async.update(fieldsAndValues = fieldsAndValues).await()

    actual suspend fun delete() =
        async.delete().await()

    actual suspend fun get() =
        DocumentSnapshot(android.get().await())

    actual val snapshots get() = callbackFlow<DocumentSnapshot> {
        val listener = android.addSnapshotListener { snapshot, exception ->
            snapshot?.let { safeOffer(DocumentSnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is DocumentReference && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    @Suppress("DeferredIsResult")
    actual class Async(@PublishedApi internal val android: NativeDocumentReference) {
        actual inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
            true -> android.set(encode(data, encodeDefaults)!!, SetOptions.merge())
            false -> android.set(encode(data, encodeDefaults)!!)
        }.asUnitDeferred()

        actual inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
            android.set(encode(data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
                .asUnitDeferred()

        actual inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
            android.set(encode(data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
                .asUnitDeferred()

        actual fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) = when(merge) {
            true -> android.set(encode(strategy, data, encodeDefaults)!!, SetOptions.merge())
            false -> android.set(encode(strategy, data, encodeDefaults)!!)
        }.asUnitDeferred()

        actual fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
            android.set(encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
                .asUnitDeferred()

        actual fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
            android.set(encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.map { it.android }))
                .asUnitDeferred()

        @Suppress("UNCHECKED_CAST")
        actual inline fun <reified T> update(data: T, encodeDefaults: Boolean) =
            android.update(encode(data, encodeDefaults) as Map<String, Any>).asUnitDeferred()

        @Suppress("UNCHECKED_CAST")
        actual fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
            android.update(encode(strategy, data, encodeDefaults) as Map<String, Any>).asUnitDeferred()

        @JvmName("updateFields")
        actual fun update(vararg fieldsAndValues: Pair<String, Any?>) =
            fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
                ?.map { (field, value) -> field to encode(value, true) }
                ?.let { encoded -> android.update(encoded.toMap()) }
                ?.asUnitDeferred() ?: CompletableDeferred(Unit)

        @JvmName("updateFieldPaths")
        actual fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
            fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
                ?.map { (field, value) -> field.android to encode(value, true) }
                ?.let { encoded ->
                    android.update(
                        encoded.first().first,
                        encoded.first().second,
                        *encoded.drop(1)
                            .flatMap { (field, value) -> listOf(field, value) }
                            .toTypedArray()
                    )
                }
                ?.asUnitDeferred() ?: CompletableDeferred(Unit)

        actual fun delete() =
            android.delete().asUnitDeferred()
    }
}

actual open class Query(open val android: com.google.firebase.firestore.Query) {

    actual suspend fun get() = QuerySnapshot(android.get().await())

    actual fun limit(limit: Number) = Query(android.limit(limit.toLong()))

    actual val snapshots get() = callbackFlow<QuerySnapshot> {
        val listener = android.addSnapshotListener { snapshot, exception ->
            snapshot?.let { safeOffer(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    internal actual fun _where(field: String, equalTo: Any?) = Query(android.whereEqualTo(field, equalTo))
    internal actual fun _where(path: FieldPath, equalTo: Any?) = Query(android.whereEqualTo(path.android, equalTo))

    internal actual fun _where(
        field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?, notEqualTo: Any?,
        lessThanOrEqualTo: Any?, greaterThanOrEqualTo: Any?
    ) = Query(
            when {
                lessThan != null -> android.whereLessThan(field, lessThan)
                greaterThan != null -> android.whereGreaterThan(field, greaterThan)
                arrayContains != null -> android.whereArrayContains(field, arrayContains)
                notEqualTo != null -> android.whereNotEqualTo(field, notEqualTo)
                lessThanOrEqualTo != null -> android.whereLessThanOrEqualTo(field, lessThanOrEqualTo)
                greaterThanOrEqualTo != null -> android.whereGreaterThanOrEqualTo(field, greaterThanOrEqualTo)
                else -> android
            }
        )

    internal actual fun _where(
        path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?, notEqualTo: Any?,
        lessThanOrEqualTo: Any?, greaterThanOrEqualTo: Any?
    ) = Query(
            when {
                lessThan != null -> android.whereLessThan(path.android, lessThan)
                greaterThan != null -> android.whereGreaterThan(path.android, greaterThan)
                arrayContains != null -> android.whereArrayContains(path.android, arrayContains)
                notEqualTo != null -> android.whereNotEqualTo(path.android, notEqualTo)
                lessThanOrEqualTo != null -> android.whereLessThanOrEqualTo(path.android, lessThanOrEqualTo)
                greaterThanOrEqualTo != null -> android.whereGreaterThanOrEqualTo(path.android, greaterThanOrEqualTo)
                else -> android
            }
        )

    internal actual fun _where(
        field: String, inArray: List<Any>?, arrayContainsAny: List<Any>?, notInArray: List<Any>?
    ) = Query(
            when {
                inArray != null -> android.whereIn(field, inArray)
                arrayContainsAny != null -> android.whereArrayContainsAny(field, arrayContainsAny)
                notInArray != null -> android.whereNotIn(field, notInArray)
                else -> android
            }
        )

    internal actual fun _where(
        path: FieldPath, inArray: List<Any>?, arrayContainsAny: List<Any>?, notInArray: List<Any>?
    ) = Query(
            when {
                inArray != null -> android.whereIn(path.android, inArray)
                arrayContainsAny != null -> android.whereArrayContainsAny(path.android, arrayContainsAny)
                notInArray != null -> android.whereNotIn(path.android, notInArray)
                else -> android
            }
        )

    internal actual fun _orderBy(field: String, direction: Direction) = Query(android.orderBy(field, direction))
    internal actual fun _orderBy(field: FieldPath, direction: Direction) = Query(android.orderBy(field.android, direction))
}

actual typealias Direction = com.google.firebase.firestore.Query.Direction
actual typealias ChangeType = com.google.firebase.firestore.DocumentChange.Type

actual class CollectionReference(override val android: com.google.firebase.firestore.CollectionReference) : Query(android) {

    actual val path: String
        get() = android.path
    actual val async = Async(android)

    actual fun document(documentPath: String) = DocumentReference(android.document(documentPath))

    actual fun document() = DocumentReference(android.document())

    actual suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean) =
        DocumentReference(android.add(encode(data, encodeDefaults)!!).await())
    actual suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        DocumentReference(android.add(encode(strategy, data, encodeDefaults)!!).await())

    @Suppress("DeferredIsResult")
    actual class Async(@PublishedApi internal val android: com.google.firebase.firestore.CollectionReference) {
        actual inline fun <reified T> add(data: T, encodeDefaults: Boolean) =
            android.add(encode(data, encodeDefaults)!!).asDeferred().convert(::DocumentReference)
        actual fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
            android.add(encode(strategy, data, encodeDefaults)!!).asDeferred().convert(::DocumentReference)
    }
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
    actual val isFromCache: Boolean get() = android.isFromCache
}

actual class FieldPath private constructor(val android: com.google.firebase.firestore.FieldPath) {
    actual constructor(vararg fieldNames: String) : this(com.google.firebase.firestore.FieldPath.of(*fieldNames))
    actual val documentId: FieldPath get() = FieldPath(com.google.firebase.firestore.FieldPath.documentId())

    override fun equals(other: Any?): Boolean = other is FieldPath && android == other.android
    override fun hashCode(): Int = android.hashCode()
    override fun toString(): String = android.toString()
}
