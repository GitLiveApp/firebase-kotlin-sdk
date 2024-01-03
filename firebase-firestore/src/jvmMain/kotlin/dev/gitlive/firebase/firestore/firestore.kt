/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import dev.gitlive.firebase.*
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy

import com.google.firebase.firestore.Query as AndroidQuery
import com.google.firebase.firestore.FieldPath as AndroidFieldPath
import com.google.firebase.firestore.Filter as AndroidFilter

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

val SetOptions.android: com.google.firebase.firestore.SetOptions? get() = when (this) {
    is SetOptions.Merge -> com.google.firebase.firestore.SetOptions.merge()
    is SetOptions.Overwrite -> null
    is SetOptions.MergeFields -> com.google.firebase.firestore.SetOptions.mergeFields(fields)
    is SetOptions.MergeFieldPaths -> com.google.firebase.firestore.SetOptions.mergeFieldPaths(encodedFieldPaths)
}

actual class WriteBatch(val android: com.google.firebase.firestore.WriteBatch) : BaseWriteBatch() {

    actual val async = Async(android)

    override fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): BaseWriteBatch = (setOptions.android?.let {
        android.set(documentRef.android, encodedData, it)
    } ?: android.set(documentRef.android, encodedData)).let {
        this
    }

    @Suppress("UNCHECKED_CAST")
    override fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseWriteBatch = android.update(documentRef.android, encodedData as Map<String, Any>).let { this }

    override fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): BaseWriteBatch = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        android.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    override fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ): BaseWriteBatch = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        android.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun commit() = async.commit().await()

    @Suppress("DeferredIsResult")
    actual class Async(private val android: com.google.firebase.firestore.WriteBatch) {
        actual fun commit(): Deferred<Unit> = android.commit().asUnitDeferred()
    }
}

actual class Transaction(val android: com.google.firebase.firestore.Transaction) : BaseTransaction() {

    override fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): BaseTransaction {
        setOptions.android?.let {
            android.set(documentRef.android, encodedData, it)
        } ?: android.set(documentRef.android, encodedData)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseTransaction = android.update(documentRef.android, encodedData as Map<String, Any>).let { this }

    override fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): BaseTransaction = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        android.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    override fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        android.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        DocumentSnapshot(android.get(documentRef.android))
}

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReference = com.google.firebase.firestore.DocumentReference

@Serializable(with = DocumentReferenceSerializer::class)
actual class DocumentReference actual constructor(internal actual val nativeValue: NativeDocumentReference) : BaseDocumentReference() {
    val android: NativeDocumentReference by ::nativeValue
    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual val parent: CollectionReference
        get() = CollectionReference(android.parent)

    override val async = Async(android)

    actual fun collection(collectionPath: String) = CollectionReference(android.collection(collectionPath))

    actual suspend fun get() =
        DocumentSnapshot(android.get().await())

    actual val snapshots: Flow<DocumentSnapshot> get() = snapshots()

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val metadataChanges = if(includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val listener = android.addSnapshotListener(metadataChanges) { snapshot, exception ->
            snapshot?.let { trySend(DocumentSnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }
    override fun equals(other: Any?): Boolean =
        this === other || other is DocumentReference && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    @Suppress("DeferredIsResult")
    class Async(@PublishedApi internal val android: NativeDocumentReference) : BaseDocumentReference.Async() {

        override fun setEncoded(encodedData: Any, setOptions: SetOptions): Deferred<Unit> = (setOptions.android?.let {
            android.set(encodedData, it)
        } ?: android.set(encodedData)).asUnitDeferred()

        @Suppress("UNCHECKED_CAST")
        override fun updateEncoded(encodedData: Any): Deferred<Unit> = android.update(encodedData as Map<String, Any>).asUnitDeferred()

        override fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>): Deferred<Unit> = encodedFieldsAndValues.takeUnless { encodedFieldsAndValues.isEmpty() }?.let {
            android.update(encodedFieldsAndValues.toMap())
        }?.asUnitDeferred() ?: CompletableDeferred(Unit)

        override fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): Deferred<Unit> = encodedFieldsAndValues.takeUnless { encodedFieldsAndValues.isEmpty() }
            ?.performUpdate { field, value, moreFieldsAndValues ->
                android.update(field, value, *moreFieldsAndValues)
            }?.asUnitDeferred() ?: CompletableDeferred(Unit)

        override fun delete() =
            android.delete().asUnitDeferred()
    }
}

actual typealias NativeQuery = AndroidQuery

actual open class Query internal actual constructor(nativeQuery: NativeQuery) {

    open val android = nativeQuery

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

    internal actual fun where(filter: Filter) = Query(
        filter.parseForQuery(android)
    )

    private fun Filter.parseForQuery(query: AndroidQuery): AndroidQuery = when (this) {
        is Filter.And -> filters.fold(query) { acc, andFilter ->
            andFilter.parseForQuery(acc)
        }
        is Filter.Or -> throw FirebaseFirestoreException(
            "Filter.Or not supported on JVM",
            com.google.firebase.firestore.FirebaseFirestoreException.Code.INVALID_ARGUMENT
        )
        is Filter.Field -> {
            when (constraint) {
                is WhereConstraint.ForNullableObject -> {
                    val modifier: AndroidQuery.(String, Any?) -> AndroidQuery = when (constraint) {
                        is WhereConstraint.EqualTo -> AndroidQuery::whereEqualTo
                        is WhereConstraint.NotEqualTo -> AndroidQuery::whereNotEqualTo
                    }
                    modifier.invoke(query, field, constraint.safeValue)
                }
                is WhereConstraint.ForObject -> {
                    val modifier: AndroidQuery.(String, Any) -> AndroidQuery = when (constraint) {
                        is WhereConstraint.LessThan -> AndroidQuery::whereLessThan
                        is WhereConstraint.GreaterThan -> AndroidQuery::whereGreaterThan
                        is WhereConstraint.LessThanOrEqualTo -> AndroidQuery::whereLessThanOrEqualTo
                        is WhereConstraint.GreaterThanOrEqualTo -> AndroidQuery::whereGreaterThanOrEqualTo
                        is WhereConstraint.ArrayContains -> AndroidQuery::whereArrayContains
                    }
                    modifier.invoke(query, field, constraint.safeValue)
                }
                is WhereConstraint.ForArray -> {
                    val modifier: AndroidQuery.(String, List<Any>) -> AndroidQuery = when (constraint) {
                        is WhereConstraint.InArray -> AndroidQuery::whereIn
                        is WhereConstraint.ArrayContainsAny -> AndroidQuery::whereArrayContainsAny
                        is WhereConstraint.NotInArray -> AndroidQuery::whereNotIn
                    }
                    modifier.invoke(query, field, constraint.safeValues)
                }
            }
        }
        is Filter.Path -> {
            when (constraint) {
                is WhereConstraint.ForNullableObject -> {
                    val modifier: AndroidQuery.(AndroidFieldPath, Any?) -> AndroidQuery = when (constraint) {
                        is WhereConstraint.EqualTo -> AndroidQuery::whereEqualTo
                        is WhereConstraint.NotEqualTo -> AndroidQuery::whereNotEqualTo
                    }
                    modifier.invoke(query, path.android, constraint.safeValue)
                }
                is WhereConstraint.ForObject -> {
                    val modifier: AndroidQuery.(AndroidFieldPath, Any) -> AndroidQuery = when (constraint) {
                        is WhereConstraint.LessThan -> AndroidQuery::whereLessThan
                        is WhereConstraint.GreaterThan -> AndroidQuery::whereGreaterThan
                        is WhereConstraint.LessThanOrEqualTo -> AndroidQuery::whereLessThanOrEqualTo
                        is WhereConstraint.GreaterThanOrEqualTo -> AndroidQuery::whereGreaterThanOrEqualTo
                        is WhereConstraint.ArrayContains -> AndroidQuery::whereArrayContains
                    }
                    modifier.invoke(query, path.android, constraint.safeValue)
                }
                is WhereConstraint.ForArray -> {
                    val modifier: AndroidQuery.(AndroidFieldPath, List<Any>) -> AndroidQuery = when (constraint) {
                        is WhereConstraint.InArray -> AndroidQuery::whereIn
                        is WhereConstraint.ArrayContainsAny -> AndroidQuery::whereArrayContainsAny
                        is WhereConstraint.NotInArray -> AndroidQuery::whereNotIn
                    }
                    modifier.invoke(query, path.android, constraint.safeValues)
                }
            }
        }
    }
    
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

actual class CollectionReference(override val android: com.google.firebase.firestore.CollectionReference) : BaseCollectionReference(android) {

    actual val path: String
        get() = android.path
    override val async = Async(android)

    actual val document: DocumentReference
        get() = DocumentReference(android.document())

    actual val parent: DocumentReference?
        get() = android.parent?.let{DocumentReference(it)}

    actual fun document(documentPath: String) = DocumentReference(android.document(documentPath))

    @Suppress("DeferredIsResult")
    class Async(@PublishedApi internal val android: com.google.firebase.firestore.CollectionReference) : BaseCollectionReference.Async() {
        override fun addEncoded(data: Any): Deferred<DocumentReference> = android.add(data).asDeferred().convert(::DocumentReference)
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
actual class DocumentSnapshot(val android: com.google.firebase.firestore.DocumentSnapshot) : BaseDocumentSnapshot() {

    actual val id get() = android.id
    actual val reference get() = DocumentReference(android.reference)

    override fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? = android.get(field, serverTimestampBehavior.toAndroid())
    override fun encodedData(serverTimestampBehavior: ServerTimestampBehavior): Any? = android.getData(serverTimestampBehavior.toAndroid())

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
    actual constructor(vararg fieldNames: String) : this(
        com.google.firebase.firestore.FieldPath.of(
            *fieldNames
        )
    )

    actual val documentId: FieldPath get() = FieldPath(com.google.firebase.firestore.FieldPath.documentId())
    actual val encoded: EncodedFieldPath = android
    override fun equals(other: Any?): Boolean = other is FieldPath && android == other.android
    override fun hashCode(): Int = android.hashCode()
    override fun toString(): String = android.toString()
}

actual typealias EncodedFieldPath = com.google.firebase.firestore.FieldPath
