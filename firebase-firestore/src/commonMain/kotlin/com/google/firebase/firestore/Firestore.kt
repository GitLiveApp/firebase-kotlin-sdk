/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.serialization.dataObjects as formatDataObjects
import com.google.firebase.serialization.getField as formatGetField
import com.google.firebase.serialization.toObject as formatToObject
import com.google.firebase.serialization.toObjects as formatToObjects
import dev.gitlive.firebase.firestore.REFLECTIVE_READ
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

/*
 * The Kotlin extensions of the Android SDK's firebase-firestore (FirestoreKt), as plain common code: on Android and the
 * JVM the facade is a header stub that is stripped, so the SDK's own binds. The reified reads are the SDK's reflective ones
 * there, so they are deprecated with an error towards the format's in com.google.firebase.serialization (FirestoreMapping.kt).
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

/*
 * The SDK's reified reads, which map a class by reflection on Android: deprecated with an error towards the same names in
 * com.google.firebase.serialization (FirestoreMapping.kt), which map through FirebaseFormat.Firestore on every platform.
 */

/** The SDK's `dataObjects` flow; deprecated towards [com.google.firebase.serialization.dataObjects]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("dataObjects<T>(metadataChanges)", "com.google.firebase.serialization.dataObjects"), DeprecationLevel.ERROR)
public inline fun <reified T> DocumentReference.dataObjects(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<T?> = formatDataObjects<T>(metadataChanges)

/** The SDK's `dataObjects` flow; deprecated towards [com.google.firebase.serialization.dataObjects]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("dataObjects<T>(metadataChanges)", "com.google.firebase.serialization.dataObjects"), DeprecationLevel.ERROR)
public inline fun <reified T> Query.dataObjects(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<List<T>> = formatDataObjects<T>(metadataChanges)

/** The SDK's reified `getField`; deprecated towards [com.google.firebase.serialization.getField]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("getField<T>(field)", "com.google.firebase.serialization.getField"), DeprecationLevel.ERROR)
public inline fun <reified T> DocumentSnapshot.getField(field: String): T? = formatGetField<T>(field)

/** The SDK's reified `getField`; deprecated towards [com.google.firebase.serialization.getField]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("getField<T>(field, serverTimestampBehavior)", "com.google.firebase.serialization.getField"), DeprecationLevel.ERROR)
public inline fun <reified T> DocumentSnapshot.getField(field: String, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = formatGetField<T>(field, serverTimestampBehavior)

/** The SDK's reified `getField`; deprecated towards [com.google.firebase.serialization.getField]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("getField<T>(fieldPath)", "com.google.firebase.serialization.getField"), DeprecationLevel.ERROR)
public inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath): T? = formatGetField<T>(fieldPath)

/** The SDK's reified `getField`; deprecated towards [com.google.firebase.serialization.getField]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("getField<T>(fieldPath, serverTimestampBehavior)", "com.google.firebase.serialization.getField"), DeprecationLevel.ERROR)
public inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = formatGetField<T>(fieldPath, serverTimestampBehavior)

/** The SDK's reified `toObject`; deprecated towards [com.google.firebase.serialization.toObject]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("toObject<T>()", "com.google.firebase.serialization.toObject"), DeprecationLevel.ERROR)
public inline fun <reified T> DocumentSnapshot.toObject(): T? = formatToObject<T>()

/** The SDK's reified `toObject`; deprecated towards [com.google.firebase.serialization.toObject]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("toObject<T>(serverTimestampBehavior)", "com.google.firebase.serialization.toObject"), DeprecationLevel.ERROR)
public inline fun <reified T> DocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = formatToObject<T>(serverTimestampBehavior)

/** The SDK's reified `toObject`; deprecated towards [com.google.firebase.serialization.toObject]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("toObject<T>()", "com.google.firebase.serialization.toObject"), DeprecationLevel.ERROR)
public inline fun <reified T> QueryDocumentSnapshot.toObject(): T = formatToObject<T>()

/** The SDK's reified `toObject`; deprecated towards [com.google.firebase.serialization.toObject]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("toObject<T>(serverTimestampBehavior)", "com.google.firebase.serialization.toObject"), DeprecationLevel.ERROR)
public inline fun <reified T> QueryDocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T = formatToObject<T>(serverTimestampBehavior)

/** The SDK's reified `toObjects`; deprecated towards [com.google.firebase.serialization.toObjects]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("toObjects<T>()", "com.google.firebase.serialization.toObjects"), DeprecationLevel.ERROR)
public inline fun <reified T> QuerySnapshot.toObjects(): List<T> = formatToObjects<T>()

/** The SDK's reified `toObjects`; deprecated towards [com.google.firebase.serialization.toObjects]. */
@Deprecated(REFLECTIVE_READ, ReplaceWith("toObjects<T>(serverTimestampBehavior)", "com.google.firebase.serialization.toObjects"), DeprecationLevel.ERROR)
public inline fun <reified T> QuerySnapshot.toObjects(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): List<T> = formatToObjects<T>(serverTimestampBehavior)
