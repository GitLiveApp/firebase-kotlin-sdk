/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The SDK's facade, which the reified actuals (Firestore.android.kt) join.
@file:JvmName("FirestoreKt")
@file:JvmMultifileClass

package com.google.firebase.firestore

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/*
 * The Kotlin extensions of the Android SDK's firebase-firestore (FirestoreKt), as plain common code: on Android and the
 * JVM the facade is a header stub that is stripped, so the SDK's own binds. The reified functions map documents to
 * custom classes by reflection on Android and the JVM, and on the other platforms through the serializer of the class
 * with the same rules (`FirebaseFirestore.Firestore`, see firestoreFormat), so a `@Serializable` class reads the same everywhere.
 */

/** The [FirebaseFirestore] of the default [FirebaseApp]; the Android SDK's `Firebase.firestore`. */
public val Firebase.firestore: FirebaseFirestore
    get() = FirebaseFirestore.getInstance()

/** The [FirebaseFirestore] of the default [FirebaseApp] for the named [database]. */
public fun Firebase.firestore(database: String): FirebaseFirestore = FirebaseFirestore.getInstance(database)

/** The [FirebaseFirestore] of [app]. */
public fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore.getInstance(app)

/** The [FirebaseFirestore] of [app] for the named [database]. */
public fun Firebase.firestore(app: FirebaseApp, database: String): FirebaseFirestore = FirebaseFirestore.getInstance(app, database)

/** [FirebaseFirestoreSettings] built with [init], as the Android SDK's `firestoreSettings`. */
public fun firestoreSettings(init: FirebaseFirestoreSettings.Builder.() -> Unit): FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().apply(init).build()

/** [MemoryCacheSettings] built with [init], as the Android SDK's `memoryCacheSettings`. */
public fun memoryCacheSettings(init: MemoryCacheSettings.Builder.() -> Unit): MemoryCacheSettings = MemoryCacheSettings.newBuilder().apply(init).build()

/** [MemoryEagerGcSettings] built with [init], as the Android SDK's `memoryEagerGcSettings`. */
public fun memoryEagerGcSettings(init: MemoryEagerGcSettings.Builder.() -> Unit): MemoryEagerGcSettings = MemoryEagerGcSettings.newBuilder().apply(init).build()

/** [MemoryLruGcSettings] built with [init], as the Android SDK's `memoryLruGcSettings`. */
public fun memoryLruGcSettings(init: MemoryLruGcSettings.Builder.() -> Unit): MemoryLruGcSettings = MemoryLruGcSettings.newBuilder().apply(init).build()

/** [PersistentCacheSettings] built with [init], as the Android SDK's `persistentCacheSettings`. */
public fun persistentCacheSettings(init: PersistentCacheSettings.Builder.() -> Unit): PersistentCacheSettings = PersistentCacheSettings.newBuilder().apply(init).build()

/** The document and every change to it as a [Flow], which fails if the listener fails. */
public fun DocumentReference.snapshots(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener(metadataChanges) { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}

/** The query results and every change to them as a [Flow], which fails if the listener fails. */
public fun Query.snapshots(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener(metadataChanges) { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}

/** The document data as [T] (see [toObject]) and every change to it as a [Flow]. */
public inline fun <reified T> DocumentReference.dataObjects(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<T?> = snapshots(metadataChanges).map { it.toObject<T>() }

/** The query results as [T] (see [toObject]) and every change to them as a [Flow]. */
public inline fun <reified T> Query.dataObjects(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<List<T>> = snapshots(metadataChanges).map { it.toObjects<T>() }

/**
 * The value of [field] as [T], as the Android SDK's reified `getField`: the SDK's reflective mapping on Android and the JVM,
 * the same rules through the serializer of [T] elsewhere (`FirebaseFormat.Firestore`).
 */
public expect inline fun <reified T> DocumentSnapshot.getField(field: String): T?

/** The value of [field] as [T] (see [getField]). */
public expect inline fun <reified T> DocumentSnapshot.getField(field: String, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T?

/** The value of [fieldPath] as [T] (see [getField]). */
public expect inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath): T?

/** The value of [fieldPath] as [T] (see [getField]). */
public expect inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T?

/**
 * The document as [T], as the Android SDK's reified `toObject`: the SDK's reflective mapping on Android and the JVM, the
 * same rules through the serializer of [T] elsewhere (`FirebaseFormat.Firestore`), `@DocumentId` and `@ServerTimestamp`
 * included; null when the document does not exist.
 */
public expect inline fun <reified T> DocumentSnapshot.toObject(): T?

/** The document as [T] (see [toObject]), with the server timestamps resolved as [serverTimestampBehavior] says. */
public expect inline fun <reified T> DocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T?

/** The document as [T] (see [DocumentSnapshot.toObject]); a query document always exists. */
public expect inline fun <reified T> QueryDocumentSnapshot.toObject(): T

/** The document as [T] (see [DocumentSnapshot.toObject]), with the server timestamps resolved as [serverTimestampBehavior] says. */
public expect inline fun <reified T> QueryDocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T

/** The documents as [T] (see [DocumentSnapshot.toObject]). */
public expect inline fun <reified T> QuerySnapshot.toObjects(): List<T>

/** The documents as [T] (see [DocumentSnapshot.toObject]), with the server timestamps resolved as [serverTimestampBehavior] says. */
public expect inline fun <reified T> QuerySnapshot.toObjects(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): List<T>
