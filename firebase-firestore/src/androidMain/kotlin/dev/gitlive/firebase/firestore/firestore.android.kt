/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")
@file:JvmMultifileClass

package dev.gitlive.firebase.firestore

import com.google.android.gms.tasks.TaskExecutors
import dev.gitlive.firebase.firestore.internal.callbackExecutors
import dev.gitlive.firebase.firestore.internal.toCompat
import java.util.concurrent.Executor
import com.google.firebase.firestore.CollectionReference as AndroidCollectionReference
import com.google.firebase.firestore.DocumentChange as AndroidDocumentChange
import com.google.firebase.firestore.DocumentReference as AndroidDocumentReference
import com.google.firebase.firestore.DocumentSnapshot as AndroidDocumentSnapshot
import com.google.firebase.firestore.FieldPath as AndroidFieldPath
import com.google.firebase.firestore.FirebaseFirestore as AndroidFirebaseFirestore
import com.google.firebase.firestore.LocalCacheSettings as AndroidLocalCacheSettings
import com.google.firebase.firestore.Query as AndroidQuery
import com.google.firebase.firestore.QuerySnapshot as AndroidQuerySnapshot
import com.google.firebase.firestore.SnapshotMetadata as AndroidSnapshotMetadata
import com.google.firebase.firestore.Transaction as AndroidTransaction
import com.google.firebase.firestore.WriteBatch as AndroidWriteBatch
import com.google.firebase.firestore.firestoreSettings as androidFirestoreSettings

// Shipped code: it binds to the real Android SDK classes at runtime, so it reaches static members and builder setters
// through the Kotlin extensions and the fluent setters, never through the header stubs' Companion objects or property setters.

/** The underlying Firebase Android SDK object. */
public val FirebaseFirestore.android: AndroidFirebaseFirestore get() = compat

public operator fun FirebaseFirestore.Companion.invoke(android: AndroidFirebaseFirestore): FirebaseFirestore = FirebaseFirestore(android)

/** The cache settings as the Firebase Android SDK's. */
public val LocalCacheSettings.android: AndroidLocalCacheSettings get() = toCompat()

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

internal actual fun FirebaseFirestoreSettings.applyTo(firestore: AndroidFirebaseFirestore) {
    firestore.firestoreSettings = androidFirestoreSettings {
        setSslEnabled(sslEnabled)
        setHost(host)
        setLocalCacheSettings(cacheSettings.toCompat())
    }
    callbackExecutors[firestore] = callbackExecutor
}

/** The underlying Firebase Android SDK object. */
public val WriteBatch.android: AndroidWriteBatch get() = compat

public operator fun WriteBatch.Companion.invoke(android: AndroidWriteBatch): WriteBatch = WriteBatch(android)

/** The underlying Firebase Android SDK object. */
public val Transaction.android: AndroidTransaction get() = compat

public operator fun Transaction.Companion.invoke(android: AndroidTransaction): Transaction = Transaction(android)

/** The underlying Firebase Android SDK object. */
public val DocumentReference.android: AndroidDocumentReference get() = compat

public operator fun DocumentReference.Companion.invoke(android: AndroidDocumentReference): DocumentReference = DocumentReference(android)

/** The underlying Firebase Android SDK object. */
public val Query.android: AndroidQuery get() = compat

public operator fun Query.Companion.invoke(android: AndroidQuery): Query = Query(android)

/** The underlying Firebase Android SDK object. */
public val CollectionReference.android: AndroidCollectionReference get() = compat

public operator fun CollectionReference.Companion.invoke(android: AndroidCollectionReference): CollectionReference = CollectionReference(android)

/** The underlying Firebase Android SDK object. */
public val QuerySnapshot.android: AndroidQuerySnapshot get() = compat

/** The underlying Firebase Android SDK object. */
public val DocumentChange.android: AndroidDocumentChange get() = compat

/** The underlying Firebase Android SDK object. */
public val DocumentSnapshot.android: AndroidDocumentSnapshot get() = compat

public operator fun DocumentSnapshot.Companion.invoke(android: AndroidDocumentSnapshot): DocumentSnapshot = DocumentSnapshot(android)

/** The underlying Firebase Android SDK object. */
public val SnapshotMetadata.android: AndroidSnapshotMetadata get() = compat

/** The underlying Firebase Android SDK object. */
public val FieldPath.android: AndroidFieldPath get() = compat

/** The code of the exception; kept for binary compatibility with the former extension, the member is used now. */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
@Deprecated("The exception's own code property is used instead.", ReplaceWith("code"), DeprecationLevel.HIDDEN)
public val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code
