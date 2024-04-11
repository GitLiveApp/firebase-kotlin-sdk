/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.MetadataChanges
import dev.gitlive.firebase.EncodedObject
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldPath as AndroidFieldPath
import com.google.firebase.firestore.Filter as AndroidFilter
import com.google.firebase.firestore.Query as AndroidQuery

actual val Firebase.firestore get() =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())

actual fun Firebase.firestore(app: FirebaseApp) =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance(app.android))

actual class FirebaseFirestore(val android: com.google.firebase.firestore.FirebaseFirestore) {

    actual fun collection(collectionPath: String) = CollectionReference(NativeCollectionReferenceWrapper(android.collection(collectionPath)))

    actual fun collectionGroup(collectionId: String) = Query(android.collectionGroup(collectionId).wrapped)

    actual fun document(documentPath: String) = DocumentReference(NativeDocumentReference(android.document(documentPath)))

    actual fun batch() = WriteBatch(NativeWriteBatchWrapper(android.batch()))

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T =
        android.runTransaction { runBlocking { Transaction(NativeTransactionWrapper(it)).func() } }.await()

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

actual typealias NativeWriteBatch = com.google.firebase.firestore.WriteBatch

@PublishedApi
internal actual class NativeWriteBatchWrapper actual internal constructor(actual val native: NativeWriteBatch) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions
    ): NativeWriteBatchWrapper = (setOptions.android?.let {
        native.set(documentRef.android, encodedData.android, it)
    } ?: native.set(documentRef.android, encodedData.android)).let {
        this
    }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject) = native.update(documentRef.android, encodedData.android).let { this }

    actual fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        native.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        native.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.delete(documentRef.android).let { this }

    actual suspend fun commit() {
        native.commit().await()
    }
}

val WriteBatch.android get() = native

actual typealias NativeTransaction = com.google.firebase.firestore.Transaction

@PublishedApi
internal actual class NativeTransactionWrapper actual internal constructor(actual val native: NativeTransaction) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions
    ): NativeTransactionWrapper {
        setOptions.android?.let {
            native.set(documentRef.android, encodedData.android, it)
        } ?: native.set(documentRef.android, encodedData.android)
        return this
    }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject) = native.update(documentRef.android, encodedData.android).let { this }

    actual fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        native.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ) = encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
        native.update(documentRef.android, field, value, *moreFieldsAndValues)
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.delete(documentRef.android).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        NativeDocumentSnapshotWrapper(native.get(documentRef.android))
}

val Transaction.android get() = native

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReferenceType = com.google.firebase.firestore.DocumentReference

@PublishedApi
internal actual class NativeDocumentReference actual constructor(actual val nativeValue: NativeDocumentReferenceType) {
    val android: NativeDocumentReferenceType by ::nativeValue
    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual val parent: NativeCollectionReferenceWrapper
        get() = NativeCollectionReferenceWrapper(android.parent)

    actual fun collection(collectionPath: String) = NativeCollectionReferenceWrapper(android.collection(collectionPath))

    actual suspend fun get() =
        NativeDocumentSnapshotWrapper(android.get().await())

    actual suspend fun setEncoded(encodedData: EncodedObject, setOptions: SetOptions) {
        val task = (setOptions.android?.let {
            android.set(encodedData.android, it)
        } ?: android.set(encodedData.android))
        task.await()
    }

    actual suspend fun updateEncoded(encodedData: EncodedObject) {
        android.update(encodedData.android).await()
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

    actual val snapshots: Flow<NativeDocumentSnapshotWrapper> get() = snapshots()

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val metadataChanges = if(includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val listener = android.addSnapshotListener(metadataChanges) { snapshot, exception ->
            snapshot?.let { trySend(NativeDocumentSnapshotWrapper(snapshot)) }
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

actual typealias NativeQuery = AndroidQuery

@PublishedApi
internal actual open class NativeQueryWrapper actual internal constructor(actual open val native: AndroidQuery) {
    actual suspend fun get() = QuerySnapshot(native.get().await())

    actual fun limit(limit: Number) = native.limit(limit.toLong()).wrapped

    actual val snapshots get() = callbackFlow<QuerySnapshot> {
        val listener = native.addSnapshotListener { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow<QuerySnapshot> {
        val metadataChanges = if(includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val listener = native.addSnapshotListener(metadataChanges) { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    actual fun where(filter: Filter) = native.where(filter.toAndroidFilter()).wrapped

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

    actual fun orderBy(field: String, direction: Direction) = native.orderBy(field, direction).wrapped
    actual fun orderBy(field: EncodedFieldPath, direction: Direction) = native.orderBy(field, direction).wrapped

    actual fun startAfter(document: NativeDocumentSnapshot) = native.startAfter(document).wrapped
    actual fun startAfter(vararg fieldValues: Any) = native.startAfter(*fieldValues).wrapped
    actual fun startAt(document: NativeDocumentSnapshot) = native.startAt(document).wrapped
    actual fun startAt(vararg fieldValues: Any) = native.startAt(*fieldValues).wrapped

    actual fun endBefore(document: NativeDocumentSnapshot) = native.endBefore(document).wrapped
    actual fun endBefore(vararg fieldValues: Any) = native.endBefore(*fieldValues).wrapped
    actual fun endAt(document: NativeDocumentSnapshot) = native.endAt(document).wrapped
    actual fun endAt(vararg fieldValues: Any) = native.endAt(*fieldValues).wrapped
}

val Query.android get() = native

internal val AndroidQuery.wrapped get() = NativeQueryWrapper(this)

actual typealias Direction = com.google.firebase.firestore.Query.Direction
actual typealias ChangeType = com.google.firebase.firestore.DocumentChange.Type

actual typealias NativeCollectionReference = com.google.firebase.firestore.CollectionReference

@PublishedApi
internal actual class NativeCollectionReferenceWrapper internal actual constructor(override actual val native: NativeCollectionReference) : NativeQueryWrapper(native) {

    actual val path: String
        get() = native.path

    actual val document: NativeDocumentReference
        get() = NativeDocumentReference(native.document())

    actual val parent: NativeDocumentReference?
        get() = native.parent?.let{ NativeDocumentReference(it) }

    actual fun document(documentPath: String) = NativeDocumentReference(native.document(documentPath))

    actual suspend fun addEncoded(data: EncodedObject) = NativeDocumentReference(native.add(data.android).await())
}

val CollectionReference.android get() = native

actual typealias FirebaseFirestoreException = com.google.firebase.firestore.FirebaseFirestoreException

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual typealias FirestoreExceptionCode = com.google.firebase.firestore.FirebaseFirestoreException.Code

actual class QuerySnapshot(val android: com.google.firebase.firestore.QuerySnapshot) {
    actual val documents
        get() = android.documents.map { DocumentSnapshot(NativeDocumentSnapshotWrapper(it)) }
    actual val documentChanges
        get() = android.documentChanges.map { DocumentChange(it) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(android.metadata)
}

actual class DocumentChange(val android: com.google.firebase.firestore.DocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshotWrapper(android.document))
    actual val newIndex: Int
        get() = android.newIndex
    actual val oldIndex: Int
        get() = android.oldIndex
    actual val type: ChangeType
        get() = android.type
}

actual typealias NativeDocumentSnapshot = com.google.firebase.firestore.DocumentSnapshot

@PublishedApi
internal actual class NativeDocumentSnapshotWrapper actual internal constructor(actual val native: com.google.firebase.firestore.DocumentSnapshot) {

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

val DocumentSnapshot.android get() = native

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
