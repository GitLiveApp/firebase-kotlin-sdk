/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")

package dev.gitlive.firebase.firestore

import com.google.android.gms.tasks.TaskExecutors
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.firestore.internal.NativeDocumentSnapshotWrapper
import java.util.concurrent.Executor
import com.google.firebase.firestore.Query as AndroidQuery
import com.google.firebase.firestore.memoryCacheSettings as androidMemoryCacheSettings
import com.google.firebase.firestore.memoryEagerGcSettings as androidMemoryEagerGcSettings
import com.google.firebase.firestore.memoryLruGcSettings as androidMemoryLruGcSettings
import com.google.firebase.firestore.persistentCacheSettings as androidPersistentCacheSettings

public actual val Firebase.firestore: FirebaseFirestore get() =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())

public actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance(app.android))

public val LocalCacheSettings.android: com.google.firebase.firestore.LocalCacheSettings get() = when (this) {
    is LocalCacheSettings.Persistent -> androidPersistentCacheSettings {
        setSizeBytes(sizeBytes)
    }
    is LocalCacheSettings.Memory -> androidMemoryCacheSettings {
        setGcSettings(
            when (garbaseCollectorSettings) {
                is MemoryGarbageCollectorSettings.Eager -> androidMemoryEagerGcSettings { }
                is MemoryGarbageCollectorSettings.LRUGC -> androidMemoryLruGcSettings {
                    setSizeBytes(garbaseCollectorSettings.sizeBytes)
                }
            },
        )
    }
}

public actual typealias NativeFirebaseFirestore = com.google.firebase.firestore.FirebaseFirestore

public val FirebaseFirestore.android: NativeFirebaseFirestore get() = native

public actual data class FirebaseFirestoreSettings(
    actual val sslEnabled: Boolean,
    actual val host: String,
    actual val cacheSettings: LocalCacheSettings,
    val callbackExecutor: Executor,
) {

    public actual companion object {
        public actual val CACHE_SIZE_UNLIMITED: Long = -1L
        internal actual val DEFAULT_HOST: String = "firestore.googleapis.com"
        internal actual val MINIMUM_CACHE_BYTES: Long = 1 * 1024 * 1024
        internal actual val DEFAULT_CACHE_SIZE_BYTES: Long = 100 * 1024 * 1024
    }

    public actual class Builder internal constructor(
        public actual var sslEnabled: Boolean,
        public actual var host: String,
        public actual var cacheSettings: LocalCacheSettings,
        public var callbackExecutor: Executor,
    ) {

        public actual constructor() : this(
            true,
            DEFAULT_HOST,
            persistentCacheSettings { },
            TaskExecutors.MAIN_THREAD,
        )
        public actual constructor(settings: FirebaseFirestoreSettings) : this(settings.sslEnabled, settings.host, settings.cacheSettings, settings.callbackExecutor)

        public actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings, callbackExecutor)
    }
}

public actual fun firestoreSettings(
    settings: FirebaseFirestoreSettings?,
    builder: FirebaseFirestoreSettings.Builder.() -> Unit,
): FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().apply {
    settings?.let {
        sslEnabled = it.sslEnabled
        host = it.host
        cacheSettings = it.cacheSettings
        callbackExecutor = it.callbackExecutor
    }
}.apply(builder).build()

public actual typealias NativeWriteBatch = com.google.firebase.firestore.WriteBatch

public val WriteBatch.android: NativeWriteBatch get() = native

public actual typealias NativeTransaction = com.google.firebase.firestore.Transaction

public val Transaction.android: NativeTransaction get() = native

/** A class representing a platform specific Firebase DocumentReference. */
public actual typealias NativeDocumentReferenceType = com.google.firebase.firestore.DocumentReference

public val DocumentReference.android: NativeDocumentReferenceType get() = native.android

public actual typealias NativeQuery = AndroidQuery

public val Query.android: NativeQuery get() = native

public actual typealias Direction = com.google.firebase.firestore.Query.Direction
public actual typealias ChangeType = com.google.firebase.firestore.DocumentChange.Type

public actual typealias NativeCollectionReference = com.google.firebase.firestore.CollectionReference

public val CollectionReference.android: NativeCollectionReference get() = native

public actual typealias FirebaseFirestoreException = com.google.firebase.firestore.FirebaseFirestoreException

@Suppress("ConflictingExtensionProperty")
public actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

public actual typealias FirestoreExceptionCode = com.google.firebase.firestore.FirebaseFirestoreException.Code

public actual class QuerySnapshot(public val android: com.google.firebase.firestore.QuerySnapshot) {
    public actual val documents: List<DocumentSnapshot>
        get() = android.documents.map { DocumentSnapshot(NativeDocumentSnapshotWrapper(it)) }
    public actual val documentChanges: List<DocumentChange>
        get() = android.documentChanges.map { DocumentChange(it) }
    public actual val metadata: SnapshotMetadata get() = SnapshotMetadata(android.metadata)
}

public actual class DocumentChange(public val android: com.google.firebase.firestore.DocumentChange) {
    public actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshotWrapper(android.document))
    public actual val newIndex: Int
        get() = android.newIndex
    public actual val oldIndex: Int
        get() = android.oldIndex
    public actual val type: ChangeType
        get() = android.type
}

public actual typealias NativeDocumentSnapshot = com.google.firebase.firestore.DocumentSnapshot

public val DocumentSnapshot.android: NativeDocumentSnapshot get() = native

public actual class SnapshotMetadata(public val android: com.google.firebase.firestore.SnapshotMetadata) {
    public actual val hasPendingWrites: Boolean get() = android.hasPendingWrites()
    public actual val isFromCache: Boolean get() = android.isFromCache
}

public actual class FieldPath private constructor(public val android: com.google.firebase.firestore.FieldPath) {

    public actual companion object {
        public actual val documentId: FieldPath = FieldPath(com.google.firebase.firestore.FieldPath.documentId())
    }

    public actual constructor(vararg fieldNames: String) : this(
        com.google.firebase.firestore.FieldPath.of(
            *fieldNames,
        ),
    )

    public actual val documentId: FieldPath get() = FieldPath.documentId
    public actual val encoded: EncodedFieldPath = android
    override fun equals(other: Any?): Boolean = other is FieldPath && android == other.android
    override fun hashCode(): Int = android.hashCode()
    override fun toString(): String = android.toString()
}

public actual typealias EncodedFieldPath = com.google.firebase.firestore.FieldPath

internal typealias NativeSource = com.google.firebase.firestore.Source
