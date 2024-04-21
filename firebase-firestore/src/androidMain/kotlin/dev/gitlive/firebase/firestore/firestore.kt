/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.gitlive.firebase.firestore

import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.MemoryEagerGcSettings
import com.google.firebase.firestore.MemoryLruGcSettings
import com.google.firebase.firestore.MetadataChanges
import dev.gitlive.firebase.internal.EncodedObject
import com.google.firebase.firestore.PersistentCacheSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.channels.ProducerScope
import dev.gitlive.firebase.firestore.Source.*
import dev.gitlive.firebase.internal.android
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import com.google.firebase.firestore.FieldPath as AndroidFieldPath
import com.google.firebase.firestore.Filter as AndroidFilter
import com.google.firebase.firestore.Query as AndroidQuery
import com.google.firebase.firestore.firestoreSettings as androidFirestoreSettings
import com.google.firebase.firestore.memoryCacheSettings as androidMemoryCacheSettings
import com.google.firebase.firestore.memoryEagerGcSettings as androidMemoryEagerGcSettings
import com.google.firebase.firestore.memoryLruGcSettings as androidMemoryLruGcSettings
import com.google.firebase.firestore.persistentCacheSettings as androidPersistentCacheSettings

actual val Firebase.firestore get() =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())

actual fun Firebase.firestore(app: FirebaseApp) =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance(app.android))

val LocalCacheSettings.android: com.google.firebase.firestore.LocalCacheSettings get() = when (this) {
    is LocalCacheSettings.Persistent -> androidPersistentCacheSettings {
        setSizeBytes(sizeBytes)
    }
    is LocalCacheSettings.Memory -> androidMemoryCacheSettings {
        setGcSettings(
            when (garbaseCollectorSettings) {
                is MemoryGarbageCollectorSettings.Eager -> androidMemoryEagerGcSettings {  }
                is MemoryGarbageCollectorSettings.LRUGC -> androidMemoryLruGcSettings {
                    setSizeBytes(garbaseCollectorSettings.sizeBytes)
                }
            }
        )
    }
}

// Since on iOS Callback threads are set as settings, we store the settings explicitly here as well
private val callbackExecutorMap = ConcurrentHashMap<com.google.firebase.firestore.FirebaseFirestore, Executor>()

actual typealias NativeFirebaseFirestore = com.google.firebase.firestore.FirebaseFirestore
internal actual class NativeFirebaseFirestoreWrapper actual constructor(actual val native: NativeFirebaseFirestore) {

    actual var settings: FirebaseFirestoreSettings
        get() = with(native.firestoreSettings) {
            FirebaseFirestoreSettings(
                isSslEnabled,
                host,
                cacheSettings?.let { localCacheSettings ->
                    when (localCacheSettings) {
                        is MemoryCacheSettings -> {
                            val garbageCollectionSettings = when (val settings = localCacheSettings.garbageCollectorSettings) {
                                is MemoryEagerGcSettings -> MemoryGarbageCollectorSettings.Eager
                                is MemoryLruGcSettings -> MemoryGarbageCollectorSettings.LRUGC(settings.sizeBytes)
                                else -> throw IllegalArgumentException("Existing settings does not have valid GarbageCollectionSettings")
                            }
                            LocalCacheSettings.Memory(garbageCollectionSettings)
                        }

                        is PersistentCacheSettings -> LocalCacheSettings.Persistent(localCacheSettings.sizeBytes)
                        else -> throw IllegalArgumentException("Existing settings is not of a valid type")
                    }
                } ?: kotlin.run {
                    @Suppress("DEPRECATION")
                    when {
                        isPersistenceEnabled -> LocalCacheSettings.Persistent(cacheSizeBytes)
                        cacheSizeBytes == FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED -> LocalCacheSettings.Memory(MemoryGarbageCollectorSettings.Eager)
                        else -> LocalCacheSettings.Memory(MemoryGarbageCollectorSettings.LRUGC(cacheSizeBytes))
                    }
                },
                callbackExecutorMap[native] ?: TaskExecutors.MAIN_THREAD
            )
        }
        set(value) {
            native.firestoreSettings = androidFirestoreSettings {
                isSslEnabled = value.sslEnabled
                host = value.host
                setLocalCacheSettings(value.cacheSettings.android)
            }
            callbackExecutorMap[native] = value.callbackExecutor
        }

    actual fun collection(collectionPath: String) = native.collection(collectionPath)

    actual fun collectionGroup(collectionId: String) = native.collectionGroup(collectionId)

    actual fun document(documentPath: String) = NativeDocumentReference(native.document(documentPath))

    actual fun batch() = native.batch()

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend NativeTransaction.() -> T): T =
        native.runTransaction { runBlocking { it.func() } }.await()

    actual suspend fun clearPersistence() =
        native.clearPersistence().await().run { }

    actual fun useEmulator(host: String, port: Int) {
        native.useEmulator(host, port)
    }

    actual suspend fun disableNetwork() =
        native.disableNetwork().await().run { }

    actual suspend fun enableNetwork() =
        native.enableNetwork().await().run { }

}

val FirebaseFirestore.android get() = native

actual data class FirebaseFirestoreSettings(
    actual val sslEnabled: Boolean,
    actual val host: String,
    actual val cacheSettings: LocalCacheSettings,
    val callbackExecutor: Executor,
) {

    actual companion object {
        actual val CACHE_SIZE_UNLIMITED: Long = -1L
        internal actual val DEFAULT_HOST: String = "firestore.googleapis.com"
        internal actual val MINIMUM_CACHE_BYTES: Long = 1 * 1024 * 1024
        internal actual val DEFAULT_CACHE_SIZE_BYTES: Long = 100 * 1024 * 1024
    }

    actual class Builder internal constructor(
        actual var sslEnabled: Boolean,
        actual var host: String,
        actual var cacheSettings: LocalCacheSettings,
        var callbackExecutor: Executor,
    ) {

        actual constructor() : this(
            true,
            DEFAULT_HOST,
            persistentCacheSettings {  },
            TaskExecutors.MAIN_THREAD
        )
        actual constructor(settings: FirebaseFirestoreSettings) : this(settings.sslEnabled, settings.host, settings.cacheSettings, settings.callbackExecutor)

        actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings, callbackExecutor)
    }
}

actual fun firestoreSettings(
    settings: FirebaseFirestoreSettings?,
    builder: FirebaseFirestoreSettings.Builder.() -> Unit
): FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().apply {
        settings?.let {
            sslEnabled = it.sslEnabled
            host = it.host
            cacheSettings = it.cacheSettings
            callbackExecutor = it.callbackExecutor
        }
    }.apply(builder).build()

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

    actual fun collection(collectionPath: String) = android.collection(collectionPath)

    actual suspend fun get(source: Source) =
        android.get(source.toAndroidSource()).await()

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

    actual val snapshots: Flow<NativeDocumentSnapshot> get() = snapshots()

    actual fun snapshots(includeMetadataChanges: Boolean) = addSnapshotListener(includeMetadataChanges) { snapshot, exception ->
        snapshot?.let { trySend(snapshot) }
        exception?.let { close(exception) }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is NativeDocumentReference && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    private fun addSnapshotListener(
        includeMetadataChanges: Boolean = false,
        listener: ProducerScope<NativeDocumentSnapshot>.(com.google.firebase.firestore.DocumentSnapshot?, com.google.firebase.firestore.FirebaseFirestoreException?) -> Unit
    ) = callbackFlow {
        val executor = callbackExecutorMap[android.firestore] ?: TaskExecutors.MAIN_THREAD
        val metadataChanges = if(includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val registration = android.addSnapshotListener(executor, metadataChanges) { snapshots, exception ->
            listener(snapshots, exception)
        }
        awaitClose { registration.remove() }
    }
}

val DocumentReference.android get() = native.android

actual typealias NativeQuery = AndroidQuery

@PublishedApi
internal actual open class NativeQueryWrapper actual internal constructor(actual open val native: AndroidQuery) {

    actual fun limit(limit: Number) = native.limit(limit.toLong())

    actual val snapshots get() = callbackFlow<QuerySnapshot> {
        val listener = native.addSnapshotListener { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow<QuerySnapshot> {
        val metadataChanges =
            if (includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val listener = native.addSnapshotListener(metadataChanges) { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    actual suspend fun get(source: Source): QuerySnapshot = QuerySnapshot(native.get(source.toAndroidSource()).await())

    actual fun where(filter: Filter) = native.where(filter.toAndroidFilter())

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

    actual fun orderBy(field: String, direction: Direction) = native.orderBy(field, direction)
    actual fun orderBy(field: EncodedFieldPath, direction: Direction) = native.orderBy(field, direction)

    actual fun startAfter(document: NativeDocumentSnapshot) = native.startAfter(document)
    actual fun startAfter(vararg fieldValues: Any) = native.startAfter(*fieldValues)
    actual fun startAt(document: NativeDocumentSnapshot) = native.startAt(document)
    actual fun startAt(vararg fieldValues: Any) = native.startAt(*fieldValues)

    actual fun endBefore(document: NativeDocumentSnapshot) = native.endBefore(document)
    actual fun endBefore(vararg fieldValues: Any) = native.endBefore(*fieldValues)
    actual fun endAt(document: NativeDocumentSnapshot) = native.endAt(document)
    actual fun endAt(vararg fieldValues: Any) = native.endAt(*fieldValues)

    private fun addSnapshotListener(
        includeMetadataChanges: Boolean = false,
        listener: ProducerScope<QuerySnapshot>.(com.google.firebase.firestore.QuerySnapshot?, com.google.firebase.firestore.FirebaseFirestoreException?) -> Unit
    ) = callbackFlow {
        val executor = callbackExecutorMap[native.firestore] ?: TaskExecutors.MAIN_THREAD
        val metadataChanges = if(includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val registration = native.addSnapshotListener(executor, metadataChanges) { snapshots, exception ->
            listener(snapshots, exception)
        }
        awaitClose { registration.remove() }
    }
}

val Query.android get() = native

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

internal typealias NativeSource = com.google.firebase.firestore.Source

private fun Source.toAndroidSource() = when(this) {
    CACHE -> NativeSource.CACHE
    SERVER -> NativeSource.SERVER
    DEFAULT -> NativeSource.DEFAULT
}
