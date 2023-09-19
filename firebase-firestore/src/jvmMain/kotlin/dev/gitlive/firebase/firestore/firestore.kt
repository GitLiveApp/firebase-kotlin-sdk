/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.MetadataChanges
import dev.gitlive.firebase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
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

actual data class FirebaseFirestore(val android: com.google.firebase.firestore.FirebaseFirestore) {

    actual data class Settings(
        actual val sslEnabled: Boolean? = null,
        actual val host: String? = null,
        actual val cacheSettings: LocalCacheSettings? = null
    ) {
        actual companion object {
            actual fun create(sslEnabled: Boolean?, host: String?, cacheSettings: LocalCacheSettings?) = Settings(sslEnabled, host, cacheSettings)
        }
    }

    private var lastSettings = Settings()

    actual fun collection(collectionPath: String) = CollectionReference(android.collection(collectionPath))

    actual fun document(documentPath: String) = DocumentReference(android.document(documentPath))

    actual fun collectionGroup(collectionId: String) = Query(android.collectionGroup(collectionId))

    actual fun batch() = WriteBatch(android.batch())

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T =
        android.runTransaction { runBlocking { Transaction(it).func() } }.await()

    actual suspend fun clearPersistence() =
        android.clearPersistence().await().run { }

    actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
        android.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
    }

    actual fun setSettings(settings: Settings) {
        lastSettings = settings
        android.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder().also { builder ->
            if (settings.cacheSettings is LocalCacheSettings.Persistent) {
                builder.isPersistenceEnabled = true
            }
            settings.sslEnabled?.let { builder.isSslEnabled = it }
            settings.host?.let { builder.host = it }
            when (val cacheSettings = settings.cacheSettings) {
                is LocalCacheSettings.Persistent -> cacheSettings.sizeBytes
                is LocalCacheSettings.Memory -> when (val garbageCollectorSettings = cacheSettings.garbaseCollectorSettings) {
                    is LocalCacheSettings.Memory.GarbageCollectorSettings.Eager -> null
                    is LocalCacheSettings.Memory.GarbageCollectorSettings.LRUGC -> garbageCollectorSettings.sizeBytes
                }
                null -> null
            }?.let { builder.cacheSizeBytes = it }
        }.build()
    }

    actual fun updateSettings(settings: Settings) = setSettings(
        Settings(settings.sslEnabled ?: lastSettings.sslEnabled, settings.host ?: lastSettings.host, settings.cacheSettings ?: lastSettings.cacheSettings)
    )

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
    override fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        encodedFieldsAndValues: List<Pair<String, Any?>>,
        merge: Boolean
    ): BaseWriteBatch {
        val serializedItem = encodedData as Map<String, *>
        val serializedFieldAndValues = encodedFieldsAndValues.toMap()

        val result = serializedItem + serializedFieldAndValues
        if (merge) {
            android.set(documentRef.android, result, com.google.firebase.firestore.SetOptions.merge())
        } else {
            android.set(documentRef.android, result)
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseWriteBatch = android.update(documentRef.android, encodedData as Map<String, Any>).let { this }

    @Suppress("UNCHECKED_CAST")
    override fun updateEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): BaseWriteBatch {
        val serializedItem = encodedData as Map<String, *>
        val serializedFieldAndValues = encodedFieldsAndValues.toMap()

        val result = serializedItem + serializedFieldAndValues
        return android.update(documentRef.android, result).let { this }
    }

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
    actual val async = Async(android)

    actual val document: DocumentReference
        get() = DocumentReference(android.document())

    actual val parent: DocumentReference?
        get() = android.parent?.let{DocumentReference(it)}

    actual fun document(documentPath: String) = DocumentReference(android.document(documentPath))

    actual suspend inline fun <reified T> add(data: T, encodeSettings: EncodeSettings) =
        DocumentReference(android.add(encode(data, encodeSettings)!!).await())
    actual suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings) =
        DocumentReference(android.add(encode(strategy, data, encodeSettings)!!).await())

    @Suppress("DeferredIsResult")
    actual class Async(@PublishedApi internal val android: com.google.firebase.firestore.CollectionReference) {
        actual inline fun <reified T> add(data: T, encodeSettings: EncodeSettings) =
            android.add(encode(data, encodeSettings)!!).asDeferred().convert(::DocumentReference)
        actual fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings) =
            android.add(encode(strategy, data, encodeSettings)!!).asDeferred().convert(::DocumentReference)
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

    actual fun <T> data(strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings, serverTimestampBehavior: ServerTimestampBehavior): T =
        decode(strategy, android.getData(serverTimestampBehavior.toAndroid()), decodeSettings)

    actual inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior): T =
        decode(value = android.get(field, serverTimestampBehavior.toAndroid()))

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings, serverTimestampBehavior: ServerTimestampBehavior): T =
        decode(strategy, android.get(field, serverTimestampBehavior.toAndroid()), decodeSettings)

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
