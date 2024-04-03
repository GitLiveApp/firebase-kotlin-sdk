/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.MetadataChanges
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import com.google.firebase.firestore.FieldPath as AndroidFieldPath
import com.google.firebase.firestore.Filter as AndroidFilter
import com.google.firebase.firestore.Query as AndroidQuery

actual val Firebase.firestore get() =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())

actual fun Firebase.firestore(app: FirebaseApp) =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance(app.android))

actual class FirebaseFirestore(val android: com.google.firebase.firestore.FirebaseFirestore) {

    actual fun collection(collectionPath: String) = CollectionReference(NativeCollectionReference(android.collection(collectionPath)))

    actual fun collectionGroup(collectionId: String) = Query(android.collectionGroup(collectionId).native)

    actual fun document(documentPath: String) = DocumentReference(NativeDocumentReference(android.document(documentPath)))

    actual fun batch() = WriteBatch(NativeWriteBatch(android.batch()))

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T =
        android.runTransaction { runBlocking { Transaction(NativeTransaction(it)).func() } }.await()

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

internal val SetOptions.android: com.google.firebase.firestore.SetOptions? get() = when (this) {
    is SetOptions.Merge -> com.google.firebase.firestore.SetOptions.merge()
    is SetOptions.Overwrite -> null
    is SetOptions.MergeFields -> com.google.firebase.firestore.SetOptions.mergeFields(fields)
    is SetOptions.MergeFieldPaths -> com.google.firebase.firestore.SetOptions.mergeFieldPaths(encodedFieldPaths)
}

@PublishedApi
internal actual class NativeWriteBatch(val android: com.google.firebase.firestore.WriteBatch) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): NativeWriteBatch = (setOptions.android?.let {
        android.set(documentRef.android, encodedData, it)
    } ?: android.set(documentRef.android, encodedData)).let {
        this
    }

    @Suppress("UNCHECKED_CAST")
    actual fun updateEncoded(documentRef: DocumentReference, encodedData: Any) = android.update(documentRef.android, encodedData as Map<String, Any>).let { this }

    actual fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        android.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        android.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun commit() {
        android.commit().await()
    }
}

val WriteBatch.android get() = native.android

@PublishedApi
internal actual class NativeTransaction(val android: com.google.firebase.firestore.Transaction) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): NativeTransaction {
        setOptions.android?.let {
            android.set(documentRef.android, encodedData, it)
        } ?: android.set(documentRef.android, encodedData)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    actual fun updateEncoded(documentRef: DocumentReference, encodedData: Any) = android.update(documentRef.android, encodedData as Map<String, Any>).let { this }

    actual fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        android.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        android.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        NativeDocumentSnapshot(android.get(documentRef.android))
}

val Transaction.android get() = native.android

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReferenceType = com.google.firebase.firestore.DocumentReference

@PublishedApi
internal actual class NativeDocumentReference actual constructor(actual val nativeValue: NativeDocumentReferenceType) {
    val android: NativeDocumentReferenceType by ::nativeValue
    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual val parent: NativeCollectionReference
        get() = NativeCollectionReference(android.parent)

    actual fun collection(collectionPath: String) = NativeCollectionReference(android.collection(collectionPath))

    actual suspend fun get() =
        NativeDocumentSnapshot(android.get().await())

    actual suspend fun setEncoded(encodedData: Any, setOptions: SetOptions) {
        val task = (setOptions.android?.let {
            android.set(encodedData, it)
        } ?: android.set(encodedData))
        task.await()
    }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun updateEncoded(encodedData: Any) {
        android.update(encodedData as Map<String, Any>).await()
    }

    actual suspend fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>) {
        encodedFieldsAndValues.takeUnless { encodedFieldsAndValues.isEmpty() }?.let {
            android.update(encodedFieldsAndValues.toMap())
        }?.await()
    }

    actual suspend fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>) {
        encodedFieldsAndValues.takeUnless { encodedFieldsAndValues.isEmpty() }
            ?.performUpdate { field, value, moreFieldsAndValues ->
                android.update(field, value, *moreFieldsAndValues)
            }?.await()
    }

    actual suspend fun delete() {
        android.delete().await()
    }

    actual val snapshots: Flow<NativeDocumentSnapshot> get() = snapshots()

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val metadataChanges = if(includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val listener = android.addSnapshotListener(metadataChanges) { snapshot, exception ->
            snapshot?.let { trySend(NativeDocumentSnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is NativeDocumentReference && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()
}

val DocumentReference.android get() = native.android

@PublishedApi
internal actual open class NativeQuery(open val android: AndroidQuery)
internal val AndroidQuery.native get() = NativeQuery(this)

actual open class Query internal actual constructor(nativeQuery: NativeQuery) {

    open val android = nativeQuery.android

    actual suspend fun get() = QuerySnapshot(android.get().await())

    actual fun limit(limit: Number) = Query(NativeQuery(android.limit(limit.toLong())))

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
        android.where(filter.toAndroidFilter()).native
    )

    private fun Filter.toAndroidFilter(): AndroidFilter = when (this) {
        is Filter.And -> AndroidFilter.and(*filters.map { it.toAndroidFilter() }.toTypedArray())
        is Filter.Or -> AndroidFilter.or(*filters.map { it.toAndroidFilter() }.toTypedArray())
        is Filter.Field -> {
            when (constraint) {
                is WhereConstraint.ForNullableObject -> {
                    val modifier: (String, Any?) -> AndroidFilter = when (constraint) {
                        is WhereConstraint.EqualTo -> AndroidFilter::equalTo
                        is WhereConstraint.NotEqualTo -> AndroidFilter::notEqualTo
                    }
                    modifier.invoke(field, constraint.safeValue)
                }
                is WhereConstraint.ForObject -> {
                    val modifier: (String, Any) -> AndroidFilter = when (constraint) {
                        is WhereConstraint.LessThan -> AndroidFilter::lessThan
                        is WhereConstraint.GreaterThan -> AndroidFilter::greaterThan
                        is WhereConstraint.LessThanOrEqualTo -> AndroidFilter::lessThanOrEqualTo
                        is WhereConstraint.GreaterThanOrEqualTo -> AndroidFilter::greaterThanOrEqualTo
                        is WhereConstraint.ArrayContains -> AndroidFilter::arrayContains
                    }
                    modifier.invoke(field, constraint.safeValue)
                }
                is WhereConstraint.ForArray -> {
                    val modifier: (String, List<Any>) -> AndroidFilter = when (constraint) {
                        is WhereConstraint.InArray -> AndroidFilter::inArray
                        is WhereConstraint.ArrayContainsAny -> AndroidFilter::arrayContainsAny
                        is WhereConstraint.NotInArray -> AndroidFilter::notInArray
                    }
                    modifier.invoke(field, constraint.safeValues)
                }
            }
        }
        is Filter.Path -> {
            when (constraint) {
                is WhereConstraint.ForNullableObject -> {
                    val modifier: (AndroidFieldPath, Any?) -> AndroidFilter = when (constraint) {
                        is WhereConstraint.EqualTo -> AndroidFilter::equalTo
                        is WhereConstraint.NotEqualTo -> AndroidFilter::notEqualTo
                    }
                    modifier.invoke(path.android, constraint.safeValue)
                }
                is WhereConstraint.ForObject -> {
                    val modifier: (AndroidFieldPath, Any) -> AndroidFilter = when (constraint) {
                        is WhereConstraint.LessThan -> AndroidFilter::lessThan
                        is WhereConstraint.GreaterThan -> AndroidFilter::greaterThan
                        is WhereConstraint.LessThanOrEqualTo -> AndroidFilter::lessThanOrEqualTo
                        is WhereConstraint.GreaterThanOrEqualTo -> AndroidFilter::greaterThanOrEqualTo
                        is WhereConstraint.ArrayContains -> AndroidFilter::arrayContains
                    }
                    modifier.invoke(path.android, constraint.safeValue)
                }
                is WhereConstraint.ForArray -> {
                    val modifier: (AndroidFieldPath, List<Any>) -> AndroidFilter = when (constraint) {
                        is WhereConstraint.InArray -> AndroidFilter::inArray
                        is WhereConstraint.ArrayContainsAny -> AndroidFilter::arrayContainsAny
                        is WhereConstraint.NotInArray -> AndroidFilter::notInArray
                    }
                    modifier.invoke(path.android, constraint.safeValues)
                }
            }
        }
    }

    internal actual fun _orderBy(field: String, direction: Direction) = Query(android.orderBy(field, direction).native)
    internal actual fun _orderBy(field: FieldPath, direction: Direction) = Query(android.orderBy(field.android, direction).native)

    internal actual fun _startAfter(document: DocumentSnapshot) = Query(android.startAfter(document.android).native)
    internal actual fun _startAfter(vararg fieldValues: Any) = Query(android.startAfter(*fieldValues).native)
    internal actual fun _startAt(document: DocumentSnapshot) = Query(android.startAt(document.android).native)
    internal actual fun _startAt(vararg fieldValues: Any) = Query(android.startAt(*fieldValues).native)

    internal actual fun _endBefore(document: DocumentSnapshot) = Query(android.endBefore(document.android).native)
    internal actual fun _endBefore(vararg fieldValues: Any) = Query(android.endBefore(*fieldValues).native)
    internal actual fun _endAt(document: DocumentSnapshot) = Query(android.endAt(document.android).native)
    internal actual fun _endAt(vararg fieldValues: Any) = Query(android.endAt(*fieldValues).native)
}

actual typealias Direction = com.google.firebase.firestore.Query.Direction
actual typealias ChangeType = com.google.firebase.firestore.DocumentChange.Type

@PublishedApi
internal actual class NativeCollectionReference(override val android: com.google.firebase.firestore.CollectionReference) : NativeQuery(android) {

    actual val path: String
        get() = android.path

    actual val document: NativeDocumentReference
        get() = NativeDocumentReference(android.document())

    actual val parent: NativeDocumentReference?
        get() = android.parent?.let{ NativeDocumentReference(it) }

    actual fun document(documentPath: String) = NativeDocumentReference(android.document(documentPath))

    actual suspend fun addEncoded(data: Any) = NativeDocumentReference(android.add(data).await())
}

val CollectionReference.android get() = native.android

actual typealias FirebaseFirestoreException = com.google.firebase.firestore.FirebaseFirestoreException

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual typealias FirestoreExceptionCode = com.google.firebase.firestore.FirebaseFirestoreException.Code

actual class QuerySnapshot(val android: com.google.firebase.firestore.QuerySnapshot) {
    actual val documents
        get() = android.documents.map { DocumentSnapshot(NativeDocumentSnapshot(it)) }
    actual val documentChanges
        get() = android.documentChanges.map { DocumentChange(it) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(android.metadata)
}

actual class DocumentChange(val android: com.google.firebase.firestore.DocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshot(android.document))
    actual val newIndex: Int
        get() = android.newIndex
    actual val oldIndex: Int
        get() = android.oldIndex
    actual val type: ChangeType
        get() = android.type
}

@PublishedApi
internal actual class NativeDocumentSnapshot(val android: com.google.firebase.firestore.DocumentSnapshot) {

    actual val id get() = android.id
    actual val reference get() = NativeDocumentReference(android.reference)

    actual fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? = android.get(field, serverTimestampBehavior.toAndroid())
    actual fun encodedData(serverTimestampBehavior: ServerTimestampBehavior): Any? = android.getData(serverTimestampBehavior.toAndroid())

    actual fun contains(field: String) = android.contains(field)

    actual val exists get() = android.exists()

    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(android.metadata)

    fun ServerTimestampBehavior.toAndroid(): com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior = when (this) {
        ServerTimestampBehavior.ESTIMATE -> com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ServerTimestampBehavior.NONE -> com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.NONE
        ServerTimestampBehavior.PREVIOUS -> com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.PREVIOUS
    }
}

val DocumentSnapshot.android get() = native.android

actual class SnapshotMetadata(val android: com.google.firebase.firestore.SnapshotMetadata) {
    actual val hasPendingWrites: Boolean get() = android.hasPendingWrites()
    actual val isFromCache: Boolean get() = android.isFromCache
}

actual class FieldPath private constructor(val android: com.google.firebase.firestore.FieldPath) {

    actual companion object {
        actual val documentId = FieldPath(com.google.firebase.firestore.FieldPath.documentId())
    }

    actual constructor(vararg fieldNames: String) : this(
        com.google.firebase.firestore.FieldPath.of(
            *fieldNames
        )
    )

    actual val documentId: FieldPath get() = FieldPath.documentId
    actual val encoded: EncodedFieldPath = android
    override fun equals(other: Any?): Boolean = other is FieldPath && android == other.android
    override fun hashCode(): Int = android.hashCode()
    override fun toString(): String = android.toString()
}

actual typealias EncodedFieldPath = com.google.firebase.firestore.FieldPath
