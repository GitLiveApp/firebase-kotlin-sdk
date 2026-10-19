/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRCollectionReference
import cocoapods.FirebaseFirestoreInternal.FIRDocumentChange
import cocoapods.FirebaseFirestoreInternal.FIRDocumentChangeType
import cocoapods.FirebaseFirestoreInternal.FIRDocumentReference
import cocoapods.FirebaseFirestoreInternal.FIRDocumentSnapshot
import cocoapods.FirebaseFirestoreInternal.FIRFieldPath
import cocoapods.FirebaseFirestoreInternal.FIRFirestore
import cocoapods.FirebaseFirestoreInternal.FIRFirestoreSettings
import cocoapods.FirebaseFirestoreInternal.FIRLocalCacheSettingsProtocol
import cocoapods.FirebaseFirestoreInternal.FIRMemoryCacheSettings
import cocoapods.FirebaseFirestoreInternal.FIRMemoryEagerGCSettings
import cocoapods.FirebaseFirestoreInternal.FIRMemoryLRUGCSettings
import cocoapods.FirebaseFirestoreInternal.FIRPersistentCacheSettings
import cocoapods.FirebaseFirestoreInternal.FIRQuery
import cocoapods.FirebaseFirestoreInternal.FIRQuerySnapshot
import cocoapods.FirebaseFirestoreInternal.FIRSnapshotMetadata
import cocoapods.FirebaseFirestoreInternal.FIRTransaction
import cocoapods.FirebaseFirestoreInternal.FIRWriteBatch
import com.google.firebase.firestore.toFirestoreException
import com.google.firebase.firestore.toIos
import dev.gitlive.firebase.firestore.internal.toCompat
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.numberWithLong
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_t
import com.google.firebase.firestore.FirebaseFirestore as CompatFirebaseFirestore
import com.google.firebase.firestore.firestoreSettings as compatFirestoreSettings

/** The underlying Firebase iOS SDK object. */
public val FirebaseFirestore.ios: FIRFirestore get() = compat.ios

public operator fun FirebaseFirestore.Companion.invoke(ios: FIRFirestore): FirebaseFirestore = FirebaseFirestore(CompatFirebaseFirestore(ios))

/** The cache settings as the Firebase iOS SDK's. */
public val LocalCacheSettings.ios: FIRLocalCacheSettingsProtocol get() = when (this) {
    is LocalCacheSettings.Persistent -> FIRPersistentCacheSettings(NSNumber.numberWithLong(sizeBytes))
    is LocalCacheSettings.Memory -> FIRMemoryCacheSettings(
        when (garbaseCollectorSettings) {
            is MemoryGarbageCollectorSettings.Eager -> FIRMemoryEagerGCSettings()
            is MemoryGarbageCollectorSettings.LRUGC -> FIRMemoryLRUGCSettings(NSNumber.numberWithLong(garbaseCollectorSettings.sizeBytes))
        },
    )
}

public actual data class FirebaseFirestoreSettings(
    actual val sslEnabled: Boolean,
    actual val host: String,
    actual val cacheSettings: LocalCacheSettings,
    val dispatchQueue: dispatch_queue_t,
) {

    public actual companion object {
        public actual val CACHE_SIZE_UNLIMITED: Long = -1L
        internal actual val DEFAULT_HOST: String = "firestore.googleapis.com"
        internal actual val MINIMUM_CACHE_BYTES: Long = 1 * 1024 * 1024
        internal actual val DEFAULT_CACHE_SIZE_BYTES: Long = 100 * 1024 * 1024
    }

    public actual class Builder(
        public actual var sslEnabled: Boolean,
        public actual var host: String,
        public actual var cacheSettings: LocalCacheSettings,
        public var dispatchQueue: dispatch_queue_t,
    ) {

        public actual constructor() : this(
            true,
            DEFAULT_HOST,
            persistentCacheSettings { },
            dispatch_get_main_queue(),
        )

        public actual constructor(settings: FirebaseFirestoreSettings) : this(
            settings.sslEnabled,
            settings.host,
            settings.cacheSettings,
            settings.dispatchQueue,
        )

        public actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings, dispatchQueue)
    }

    /** The settings as the Firebase iOS SDK's. */
    val ios: FIRFirestoreSettings get() = FIRFirestoreSettings().apply {
        cacheSettings = this@FirebaseFirestoreSettings.cacheSettings.ios
        sslEnabled = this@FirebaseFirestoreSettings.sslEnabled
        host = this@FirebaseFirestoreSettings.host
        dispatchQueue = this@FirebaseFirestoreSettings.dispatchQueue
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
        dispatchQueue = it.dispatchQueue
    }
}.apply(builder).build()

internal actual fun FirebaseFirestoreSettings.applyTo(firestore: CompatFirebaseFirestore) {
    firestore.firestoreSettings = compatFirestoreSettings {
        setSslEnabled(sslEnabled)
        setHost(host)
        setLocalCacheSettings(cacheSettings.toCompat())
    }.also { it.callbackContext = dispatchQueue }
}

/** The underlying Firebase iOS SDK object. */
public val WriteBatch.ios: FIRWriteBatch get() = compat.ios

public operator fun WriteBatch.Companion.invoke(ios: FIRWriteBatch): WriteBatch = WriteBatch(com.google.firebase.firestore.WriteBatch(ios))

/** The underlying Firebase iOS SDK object. */
public val Transaction.ios: FIRTransaction get() = compat.ios

public operator fun Transaction.Companion.invoke(ios: FIRTransaction): Transaction = Transaction(com.google.firebase.firestore.Transaction(ios))

/** The underlying Firebase iOS SDK object. */
public val DocumentReference.ios: FIRDocumentReference get() = compat.ios

public operator fun DocumentReference.Companion.invoke(ios: FIRDocumentReference): DocumentReference = DocumentReference(com.google.firebase.firestore.DocumentReference(ios))

/** The underlying Firebase iOS SDK object. */
public val Query.ios: FIRQuery get() = compat.ios

public operator fun Query.Companion.invoke(ios: FIRQuery): Query = Query(com.google.firebase.firestore.Query(ios))

/** The underlying Firebase iOS SDK object. */
public val CollectionReference.ios: FIRCollectionReference get() = compat.ios

public operator fun CollectionReference.Companion.invoke(ios: FIRCollectionReference): CollectionReference = CollectionReference(com.google.firebase.firestore.CollectionReference(ios))

/** The underlying Firebase iOS SDK object. */
public val QuerySnapshot.ios: FIRQuerySnapshot get() = compat.ios

/** The underlying Firebase iOS SDK object. */
public val DocumentChange.ios: FIRDocumentChange get() = compat.ios

/** The underlying Firebase iOS SDK object. */
public val DocumentSnapshot.ios: FIRDocumentSnapshot get() = compat.ios

public operator fun DocumentSnapshot.Companion.invoke(ios: FIRDocumentSnapshot): DocumentSnapshot = DocumentSnapshot(com.google.firebase.firestore.DocumentSnapshot(ios))

/** The underlying Firebase iOS SDK object. */
public val SnapshotMetadata.ios: FIRSnapshotMetadata get() = compat.ios

/** The field path as the Firebase iOS SDK's. */
public val FieldPath.ios: FIRFieldPath get() = compat.toIos()

/** The change type as the Firebase iOS SDK's. */
public val ChangeType.ios: FIRDocumentChangeType get() = when (this) {
    ChangeType.ADDED -> FIRDocumentChangeType.FIRDocumentChangeTypeAdded
    ChangeType.MODIFIED -> FIRDocumentChangeType.FIRDocumentChangeTypeModified
    ChangeType.REMOVED -> FIRDocumentChangeType.FIRDocumentChangeTypeRemoved
}

/** The error as a [FirebaseFirestoreException] with the matching [FirestoreExceptionCode]. */
public fun NSError.toException(): FirebaseFirestoreException = toFirestoreException()
