/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import dev.gitlive.firebase.firestore.stub
import java.util.concurrent.Executor

/*
 * Header stubs for com.google.firebase:firebase-firestore (see buildSrc utils/HeaderStubs.kt): compiled against,
 * verified to match the real classes, and deleted from the output so the real SDK binds at runtime. The constructors of
 * Query, DocumentSnapshot and AggregateField only exist for the stubs extending them (the SDK's constructors take
 * internal types); they are deprecated with an error so that they need not match the real classes and cannot be called.
 */

private const val STUB_CONSTRUCTOR = "Header stub constructor; the SDK's class is not instantiable"

public actual class FirebaseFirestore private constructor() {
    public actual val app: FirebaseApp get() = stub()
    public actual var firestoreSettings: FirebaseFirestoreSettings
        get() = stub()
        set(_) = stub()
    public actual val persistentCacheIndexManager: PersistentCacheIndexManager? get() = stub()
    public actual fun collection(collectionPath: String): CollectionReference = stub()
    public actual fun collectionGroup(collectionId: String): Query = stub()
    public actual fun document(documentPath: String): DocumentReference = stub()
    public actual fun batch(): WriteBatch = stub()
    public actual fun runBatch(batchFunction: WriteBatch.Function): Task<Nothing?> = stub()
    public actual fun <TResult> runTransaction(updateFunction: Transaction.Function<TResult>): Task<TResult> = stub()
    public actual fun <TResult> runTransaction(options: TransactionOptions, updateFunction: Transaction.Function<TResult>): Task<TResult> = stub()
    public actual fun addSnapshotsInSyncListener(runnable: Runnable): ListenerRegistration = stub()
    public actual fun loadBundle(bundleData: ByteArray): LoadBundleTask = stub()
    public actual fun getNamedQuery(name: String): Task<Query?> = stub()
    public actual fun useEmulator(host: String, port: Int): Unit = stub()
    public actual fun clearPersistence(): Task<Nothing?> = stub()
    public actual fun disableNetwork(): Task<Nothing?> = stub()
    public actual fun enableNetwork(): Task<Nothing?> = stub()
    public actual fun terminate(): Task<Nothing?> = stub()
    public actual fun waitForPendingWrites(): Task<Nothing?> = stub()

    public actual companion object {
        @JvmStatic
        public actual fun getInstance(): FirebaseFirestore = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp): FirebaseFirestore = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp, database: String): FirebaseFirestore = stub()

        @JvmStatic
        public actual fun getInstance(database: String): FirebaseFirestore = stub()

        @JvmStatic
        public actual fun setLoggingEnabled(loggingEnabled: Boolean): Unit = stub()
    }
}

public actual class PersistentCacheIndexManager private constructor() {
    public actual fun enableIndexAutoCreation(): Unit = stub()
    public actual fun disableIndexAutoCreation(): Unit = stub()
    public actual fun deleteAllIndexes(): Unit = stub()
}

public actual abstract class LoadBundleTask private constructor() : Task<LoadBundleTaskProgress>() {
    public actual fun addOnProgressListener(listener: OnProgressListener<LoadBundleTaskProgress>): LoadBundleTask = stub()
    public fun addOnProgressListener(executor: Executor, listener: OnProgressListener<LoadBundleTaskProgress>): LoadBundleTask = stub()
    public fun addOnProgressListener(activity: Activity, listener: OnProgressListener<LoadBundleTaskProgress>): LoadBundleTask = stub()
}

public actual class LoadBundleTaskProgress private constructor() {
    public actual val documentsLoaded: Int get() = stub()
    public actual val totalDocuments: Int get() = stub()
    public actual val bytesLoaded: Long get() = stub()
    public actual val totalBytes: Long get() = stub()
    public actual val taskState: TaskState get() = stub()
    public actual val exception: Exception? get() = stub()

    public actual enum class TaskState {
        ERROR,
        RUNNING,
        SUCCESS,
    }
}

@Suppress("DEPRECATION_ERROR")
public actual open class Query {
    @Deprecated(STUB_CONSTRUCTOR, level = DeprecationLevel.ERROR)
    protected constructor()

    public actual val firestore: FirebaseFirestore get() = stub()
    public actual fun whereEqualTo(field: String, value: Any?): Query = stub()
    public actual fun whereEqualTo(fieldPath: FieldPath, value: Any?): Query = stub()
    public actual fun whereNotEqualTo(field: String, value: Any?): Query = stub()
    public actual fun whereNotEqualTo(fieldPath: FieldPath, value: Any?): Query = stub()
    public actual fun whereLessThan(field: String, value: Any): Query = stub()
    public actual fun whereLessThan(fieldPath: FieldPath, value: Any): Query = stub()
    public actual fun whereLessThanOrEqualTo(field: String, value: Any): Query = stub()
    public actual fun whereLessThanOrEqualTo(fieldPath: FieldPath, value: Any): Query = stub()
    public actual fun whereGreaterThan(field: String, value: Any): Query = stub()
    public actual fun whereGreaterThan(fieldPath: FieldPath, value: Any): Query = stub()
    public actual fun whereGreaterThanOrEqualTo(field: String, value: Any): Query = stub()
    public actual fun whereGreaterThanOrEqualTo(fieldPath: FieldPath, value: Any): Query = stub()
    public actual fun whereArrayContains(field: String, value: Any): Query = stub()
    public actual fun whereArrayContains(fieldPath: FieldPath, value: Any): Query = stub()
    public actual fun whereArrayContainsAny(field: String, values: List<Any>): Query = stub()
    public actual fun whereArrayContainsAny(fieldPath: FieldPath, values: List<Any>): Query = stub()
    public actual fun whereIn(field: String, values: List<Any>): Query = stub()
    public actual fun whereIn(fieldPath: FieldPath, values: List<Any>): Query = stub()
    public actual fun whereNotIn(field: String, values: List<Any>): Query = stub()
    public actual fun whereNotIn(fieldPath: FieldPath, values: List<Any>): Query = stub()
    public actual fun where(filter: Filter): Query = stub()
    public actual fun orderBy(field: String): Query = stub()
    public actual fun orderBy(fieldPath: FieldPath): Query = stub()
    public actual fun orderBy(field: String, direction: Direction): Query = stub()
    public actual fun orderBy(fieldPath: FieldPath, direction: Direction): Query = stub()
    public actual fun limit(limit: Long): Query = stub()
    public actual fun limitToLast(limit: Long): Query = stub()
    public actual fun startAt(snapshot: DocumentSnapshot): Query = stub()
    public actual fun startAt(vararg fieldValues: Any?): Query = stub()
    public actual fun startAfter(snapshot: DocumentSnapshot): Query = stub()
    public actual fun startAfter(vararg fieldValues: Any?): Query = stub()
    public actual fun endBefore(snapshot: DocumentSnapshot): Query = stub()
    public actual fun endBefore(vararg fieldValues: Any?): Query = stub()
    public actual fun endAt(snapshot: DocumentSnapshot): Query = stub()
    public actual fun endAt(vararg fieldValues: Any?): Query = stub()
    public actual fun get(): Task<QuerySnapshot> = stub()
    public actual fun get(source: Source): Task<QuerySnapshot> = stub()
    public actual fun addSnapshotListener(listener: EventListener<QuerySnapshot>): ListenerRegistration = stub()
    public actual fun addSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration = stub()
    public actual fun addSnapshotListener(options: SnapshotListenOptions, listener: EventListener<QuerySnapshot>): ListenerRegistration = stub()

    /** Not in the common API (the executor is Android-only); the dev.gitlive layer's Android listeners bind to this member. */
    public fun addSnapshotListener(executor: Executor, metadataChanges: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration = stub()
    public actual fun count(): AggregateQuery = stub()
    public actual fun aggregate(aggregateField: AggregateField, vararg aggregateFields: AggregateField): AggregateQuery = stub()

    public actual enum class Direction {
        ASCENDING,
        DESCENDING,
    }
}

@Suppress("DEPRECATION_ERROR")
public actual class CollectionReference private constructor() : Query() {
    public actual val id: String get() = stub()
    public actual val path: String get() = stub()
    public actual val parent: DocumentReference? get() = stub()
    public actual fun document(): DocumentReference = stub()
    public actual fun document(documentPath: String): DocumentReference = stub()
    public actual fun add(data: Any): Task<DocumentReference> = stub()
}

public actual class AggregateQuery private constructor() {
    public actual val query: Query get() = stub()
    public actual val aggregateFields: List<AggregateField> get() = stub()
    public actual fun get(source: AggregateSource): Task<AggregateQuerySnapshot> = stub()
}

public actual class AggregateQuerySnapshot private constructor() {
    public actual val query: AggregateQuery get() = stub()
    public actual val count: Long get() = stub()
    public actual fun get(aggregateField: AggregateField): Any? = stub()
    public actual fun get(averageAggregateField: AggregateField.AverageAggregateField): Double? = stub()
    public actual fun get(countAggregateField: AggregateField.CountAggregateField): Long = stub()
    public actual fun getDouble(aggregateField: AggregateField): Double? = stub()
    public actual fun getLong(aggregateField: AggregateField): Long? = stub()
}

@Suppress("DEPRECATION_ERROR")
public actual abstract class AggregateField {
    @Deprecated(STUB_CONSTRUCTOR, level = DeprecationLevel.ERROR)
    protected constructor()

    public actual val alias: String get() = stub()
    public actual val fieldPath: String get() = stub()
    public actual val operator: String get() = stub()

    public actual class CountAggregateField private constructor() : AggregateField()
    public actual class SumAggregateField private constructor() : AggregateField()
    public actual class AverageAggregateField private constructor() : AggregateField()

    public actual companion object {
        @JvmStatic
        public actual fun count(): CountAggregateField = stub()

        @JvmStatic
        public actual fun sum(field: String): SumAggregateField = stub()

        @JvmStatic
        public actual fun sum(fieldPath: FieldPath): SumAggregateField = stub()

        @JvmStatic
        public actual fun average(field: String): AverageAggregateField = stub()

        @JvmStatic
        public actual fun average(fieldPath: FieldPath): AverageAggregateField = stub()
    }
}

public actual class DocumentReference private constructor() {
    public actual val firestore: FirebaseFirestore get() = stub()
    public actual val id: String get() = stub()
    public actual val path: String get() = stub()
    public actual val parent: CollectionReference get() = stub()
    public actual fun collection(collectionPath: String): CollectionReference = stub()
    public actual fun get(): Task<DocumentSnapshot> = stub()
    public actual fun get(source: Source): Task<DocumentSnapshot> = stub()
    public actual fun set(data: Any): Task<Nothing?> = stub()
    public actual fun set(data: Any, options: SetOptions): Task<Nothing?> = stub()
    public actual fun update(data: Map<String, Any?>): Task<Nothing?> = stub()
    public actual fun update(field: String, value: Any?, vararg moreFieldsAndValues: Any?): Task<Nothing?> = stub()
    public actual fun update(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Task<Nothing?> = stub()
    public actual fun delete(): Task<Nothing?> = stub()
    public actual fun addSnapshotListener(listener: EventListener<DocumentSnapshot>): ListenerRegistration = stub()
    public actual fun addSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration = stub()
    public actual fun addSnapshotListener(options: SnapshotListenOptions, listener: EventListener<DocumentSnapshot>): ListenerRegistration = stub()

    /** Not in the common API (the executor is Android-only); the dev.gitlive layer's Android listeners bind to this member. */
    public fun addSnapshotListener(executor: Executor, metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration = stub()
}

@Suppress("DEPRECATION_ERROR")
public actual open class DocumentSnapshot {
    @Deprecated(STUB_CONSTRUCTOR, level = DeprecationLevel.ERROR)
    protected constructor()

    public actual val id: String get() = stub()
    public actual val reference: DocumentReference get() = stub()
    public actual val metadata: SnapshotMetadata get() = stub()
    public actual val data: Map<String, Any?>? get() = stub()
    public actual fun exists(): Boolean = stub()
    public actual fun getData(serverTimestampBehavior: ServerTimestampBehavior): Map<String, Any?>? = stub()
    public actual fun contains(field: String): Boolean = stub()
    public actual fun contains(fieldPath: FieldPath): Boolean = stub()
    public actual fun get(field: String): Any? = stub()
    public actual fun get(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? = stub()
    public actual fun get(fieldPath: FieldPath): Any? = stub()
    public actual fun get(fieldPath: FieldPath, serverTimestampBehavior: ServerTimestampBehavior): Any? = stub()
    public actual fun getBoolean(field: String): Boolean? = stub()
    public actual fun getDouble(field: String): Double? = stub()
    public actual fun getLong(field: String): Long? = stub()
    public actual fun getString(field: String): String? = stub()
    public actual fun getTimestamp(field: String): Timestamp? = stub()
    public actual fun getTimestamp(field: String, serverTimestampBehavior: ServerTimestampBehavior): Timestamp? = stub()
    public actual fun getBlob(field: String): Blob? = stub()
    public actual fun getGeoPoint(field: String): GeoPoint? = stub()
    public actual fun getDocumentReference(field: String): DocumentReference? = stub()

    public actual enum class ServerTimestampBehavior {
        NONE,
        ESTIMATE,
        PREVIOUS,
    }
}

@Suppress("DEPRECATION_ERROR")
public actual class QueryDocumentSnapshot private constructor() : DocumentSnapshot()

public actual class QuerySnapshot private constructor() : Iterable<QueryDocumentSnapshot> {
    public actual val query: Query get() = stub()
    public actual val metadata: SnapshotMetadata get() = stub()
    public actual val documents: List<DocumentSnapshot> get() = stub()
    public actual val documentChanges: List<DocumentChange> get() = stub()
    public actual fun getDocumentChanges(metadataChanges: MetadataChanges): List<DocumentChange> = stub()
    public actual val isEmpty: Boolean get() = stub()
    public actual fun size(): Int = stub()
    actual override fun iterator(): Iterator<QueryDocumentSnapshot> = stub()
}

public actual class DocumentChange private constructor() {
    public actual val document: QueryDocumentSnapshot get() = stub()
    public actual val type: Type get() = stub()
    public actual val oldIndex: Int get() = stub()
    public actual val newIndex: Int get() = stub()

    public actual enum class Type {
        ADDED,
        MODIFIED,
        REMOVED,
    }
}

public actual class SnapshotMetadata private constructor() {
    public actual fun hasPendingWrites(): Boolean = stub()
    public actual val isFromCache: Boolean get() = stub()
}

public actual class Transaction private constructor() {
    /** Not in the common API (the JS SDK reads asynchronously); the nonJsMain extension binds to this member. */
    public fun get(documentRef: DocumentReference): DocumentSnapshot = stub()
    public actual fun set(documentRef: DocumentReference, data: Any): Transaction = stub()
    public actual fun set(documentRef: DocumentReference, data: Any, options: SetOptions): Transaction = stub()
    public actual fun update(documentRef: DocumentReference, data: Map<String, Any?>): Transaction = stub()
    public actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): Transaction = stub()
    public actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Transaction = stub()
    public actual fun delete(documentRef: DocumentReference): Transaction = stub()

    public actual fun interface Function<TResult> {
        public actual fun apply(transaction: Transaction): TResult
    }
}

public actual class WriteBatch private constructor() {
    public actual fun set(documentRef: DocumentReference, data: Any): WriteBatch = stub()
    public actual fun set(documentRef: DocumentReference, data: Any, options: SetOptions): WriteBatch = stub()
    public actual fun update(documentRef: DocumentReference, data: Map<String, Any?>): WriteBatch = stub()
    public actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch = stub()
    public actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch = stub()
    public actual fun delete(documentRef: DocumentReference): WriteBatch = stub()
    public actual fun commit(): Task<Nothing?> = stub()

    public actual fun interface Function {
        public actual fun apply(batch: WriteBatch)
    }
}

public actual class FieldPath private constructor() {
    public actual companion object {
        @JvmStatic
        public actual fun of(vararg fieldNames: String): FieldPath = stub()

        @JvmStatic
        public actual fun documentId(): FieldPath = stub()

        @JvmStatic
        public actual fun fromDotSeparatedPath(path: String): FieldPath = stub()
    }
}

public actual abstract class FieldValue private constructor() {
    public actual companion object {
        @JvmStatic
        public actual fun serverTimestamp(): FieldValue = stub()

        @JvmStatic
        public actual fun delete(): FieldValue = stub()

        @JvmStatic
        public actual fun arrayUnion(vararg elements: Any): FieldValue = stub()

        @JvmStatic
        public actual fun arrayRemove(vararg elements: Any): FieldValue = stub()

        @JvmStatic
        public actual fun increment(l: Long): FieldValue = stub()

        @JvmStatic
        public actual fun increment(d: Double): FieldValue = stub()
    }
}

public actual class SetOptions private constructor() {
    public actual companion object {
        @JvmStatic
        public actual fun merge(): SetOptions = stub()

        @JvmStatic
        public actual fun mergeFields(vararg fields: String): SetOptions = stub()

        @JvmStatic
        public actual fun mergeFields(fields: List<String>): SetOptions = stub()

        @JvmStatic
        public actual fun mergeFieldPaths(fields: List<FieldPath>): SetOptions = stub()
    }
}

public actual class Filter actual constructor() {
    public actual companion object {
        @JvmStatic
        public actual fun equalTo(field: String, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun equalTo(fieldPath: FieldPath, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun notEqualTo(field: String, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun notEqualTo(fieldPath: FieldPath, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun greaterThan(field: String, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun greaterThan(fieldPath: FieldPath, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun greaterThanOrEqualTo(field: String, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun greaterThanOrEqualTo(fieldPath: FieldPath, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun lessThan(field: String, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun lessThan(fieldPath: FieldPath, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun lessThanOrEqualTo(field: String, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun lessThanOrEqualTo(fieldPath: FieldPath, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun arrayContains(field: String, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun arrayContains(fieldPath: FieldPath, value: Any?): Filter = stub()

        @JvmStatic
        public actual fun arrayContainsAny(field: String, values: List<Any>): Filter = stub()

        @JvmStatic
        public actual fun arrayContainsAny(fieldPath: FieldPath, values: List<Any>): Filter = stub()

        @JvmStatic
        public actual fun inArray(field: String, values: List<Any>): Filter = stub()

        @JvmStatic
        public actual fun inArray(fieldPath: FieldPath, values: List<Any>): Filter = stub()

        @JvmStatic
        public actual fun notInArray(field: String, values: List<Any>): Filter = stub()

        @JvmStatic
        public actual fun notInArray(fieldPath: FieldPath, values: List<Any>): Filter = stub()

        @JvmStatic
        public actual fun and(vararg filters: Filter): Filter = stub()

        @JvmStatic
        public actual fun or(vararg filters: Filter): Filter = stub()
    }
}

public actual class FirebaseFirestoreSettings private constructor() {
    public actual val host: String get() = stub()
    public actual val isSslEnabled: Boolean get() = stub()
    public actual val cacheSettings: LocalCacheSettings? get() = stub()
    public actual val grpcFlowControlWindow: Int get() = stub()

    public actual companion object {
        @JvmField
        public actual val CACHE_SIZE_UNLIMITED: Long = -1L

        @JvmField
        public actual val DEFAULT_GRPC_FLOW_CONTROL_WINDOW: Int = 256 * 1024
    }

    public actual class Builder {
        public actual constructor()
        public actual constructor(settings: FirebaseFirestoreSettings)
        public actual var host: String
            get() = stub()
            set(_) = stub()
        public actual var isSslEnabled: Boolean
            get() = stub()
            set(_) = stub()
        public actual var grpcFlowControlWindow: Int
            get() = stub()
            set(_) = stub()
        public actual fun setHost(host: String): Builder = stub()
        public actual fun setSslEnabled(value: Boolean): Builder = stub()
        public actual fun setLocalCacheSettings(cacheSettings: LocalCacheSettings): Builder = stub()
        public actual fun setGrpcFlowControlWindow(grpcFlowControlWindow: Int): Builder = stub()
        public actual fun build(): FirebaseFirestoreSettings = stub()
    }
}

public actual class PersistentCacheSettings private constructor() : LocalCacheSettings {
    public actual val sizeBytes: Long get() = stub()

    public actual class Builder private constructor() {
        public actual fun setSizeBytes(sizeBytes: Long): Builder = stub()
        public actual fun build(): PersistentCacheSettings = stub()
    }

    public actual companion object {
        @JvmStatic
        public actual fun newBuilder(): Builder = stub()
    }
}

public actual class MemoryCacheSettings private constructor() : LocalCacheSettings {
    public actual val garbageCollectorSettings: MemoryGarbageCollectorSettings get() = stub()

    public actual class Builder private constructor() {
        public actual fun setGcSettings(gcSettings: MemoryGarbageCollectorSettings): Builder = stub()
        public actual fun build(): MemoryCacheSettings = stub()
    }

    public actual companion object {
        @JvmStatic
        public actual fun newBuilder(): Builder = stub()
    }
}

public actual class MemoryEagerGcSettings private constructor() : MemoryGarbageCollectorSettings {
    public actual class Builder private constructor() {
        public actual fun build(): MemoryEagerGcSettings = stub()
    }

    public actual companion object {
        @JvmStatic
        public actual fun newBuilder(): Builder = stub()
    }
}

public actual class MemoryLruGcSettings private constructor() : MemoryGarbageCollectorSettings {
    public actual val sizeBytes: Long get() = stub()

    public actual class Builder private constructor() {
        public actual fun setSizeBytes(sizeBytes: Long): Builder = stub()
        public actual fun build(): MemoryLruGcSettings = stub()
    }

    public actual companion object {
        @JvmStatic
        public actual fun newBuilder(): Builder = stub()
    }
}
