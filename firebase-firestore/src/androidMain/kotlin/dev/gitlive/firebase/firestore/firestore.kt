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
                is MemoryGarbageCollectorSettings.Eager -> androidMemoryEagerGcSettings { }
                is MemoryGarbageCollectorSettings.LRUGC -> androidMemoryLruGcSettings {
                    setSizeBytes(garbaseCollectorSettings.sizeBytes)
                }
            },
        )
    }
}

actual typealias NativeFirebaseFirestore = com.google.firebase.firestore.FirebaseFirestore

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
            persistentCacheSettings { },
            TaskExecutors.MAIN_THREAD,
        )
        actual constructor(settings: FirebaseFirestoreSettings) : this(settings.sslEnabled, settings.host, settings.cacheSettings, settings.callbackExecutor)

        actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings, callbackExecutor)
    }
}

actual fun firestoreSettings(
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

actual typealias NativeWriteBatch = com.google.firebase.firestore.WriteBatch

val WriteBatch.android get() = native

actual typealias NativeTransaction = com.google.firebase.firestore.Transaction

val Transaction.android get() = native

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReferenceType = com.google.firebase.firestore.DocumentReference

val DocumentReference.android get() = native.android

actual typealias NativeQuery = AndroidQuery

val Query.android get() = native

actual typealias Direction = com.google.firebase.firestore.Query.Direction
actual typealias ChangeType = com.google.firebase.firestore.DocumentChange.Type

actual typealias NativeCollectionReference = com.google.firebase.firestore.CollectionReference

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
            *fieldNames,
        ),
    )

    actual val documentId: FieldPath get() = FieldPath.documentId
    actual val encoded: EncodedFieldPath = android
    override fun equals(other: Any?): Boolean = other is FieldPath && android == other.android
    override fun hashCode(): Int = android.hashCode()
    override fun toString(): String = android.toString()
}

actual typealias EncodedFieldPath = com.google.firebase.firestore.FieldPath

internal typealias NativeSource = com.google.firebase.firestore.Source
