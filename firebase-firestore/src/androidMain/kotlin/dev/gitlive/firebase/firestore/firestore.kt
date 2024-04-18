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
import com.google.firebase.firestore.PersistentCacheSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.channels.ProducerScope
import dev.gitlive.firebase.firestore.Source.*
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
actual internal class NativeFirebaseFirestoreWrapper actual constructor(actual val native: NativeFirebaseFirestore) {

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

    actual fun collection(collectionPath: String) = NativeCollectionReference(native.collection(collectionPath))

    actual fun collectionGroup(collectionId: String) = native.collectionGroup(collectionId).native

    actual fun document(documentPath: String) = NativeDocumentReference(native.document(documentPath))

    actual fun batch() = NativeWriteBatch(native.batch())

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend NativeTransaction.() -> T): T =
        native.runTransaction { runBlocking { NativeTransaction(it).func() } }.await()

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

    actual suspend fun get(source: Source) =
        NativeDocumentSnapshot(android.get(source.toAndroidSource()).await())

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

    actual fun snapshots(includeMetadataChanges: Boolean) = addSnapshotListener(includeMetadataChanges) { snapshot, exception ->
        snapshot?.let { trySend(NativeDocumentSnapshot(snapshot)) }
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

@PublishedApi
internal actual open class NativeQuery(open val android: AndroidQuery)
internal val AndroidQuery.native get() = NativeQuery(this)

actual open class Query internal actual constructor(nativeQuery: NativeQuery) {

    open val android = nativeQuery.android

    actual suspend fun get(source: Source) = QuerySnapshot(android.get(source.toAndroidSource()).await())

    actual fun limit(limit: Number) = Query(NativeQuery(android.limit(limit.toLong())))

    actual val snapshots get() = addSnapshotListener { snapshot, exception ->
        snapshot?.let { trySend(QuerySnapshot(snapshot)) }
        exception?.let { close(exception) }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = addSnapshotListener(includeMetadataChanges) { snapshot, exception ->
        snapshot?.let { trySend(QuerySnapshot(snapshot)) }
        exception?.let { close(exception) }
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

    private fun addSnapshotListener(
        includeMetadataChanges: Boolean = false,
        listener: ProducerScope<QuerySnapshot>.(com.google.firebase.firestore.QuerySnapshot?, com.google.firebase.firestore.FirebaseFirestoreException?) -> Unit
    ) = callbackFlow {
        val executor = callbackExecutorMap[android.firestore] ?: TaskExecutors.MAIN_THREAD
        val metadataChanges = if(includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val registration = android.addSnapshotListener(executor, metadataChanges) { snapshots, exception ->
            listener(snapshots, exception)
        }
        awaitClose { registration.remove() }
    }
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

internal typealias NativeSource = com.google.firebase.firestore.Source

private fun Source.toAndroidSource() = when(this) {
    CACHE -> NativeSource.CACHE
    SERVER -> NativeSource.SERVER
    DEFAULT -> NativeSource.DEFAULT
}
