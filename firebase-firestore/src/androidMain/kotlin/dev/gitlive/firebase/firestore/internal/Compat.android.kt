/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore.internal

import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.android
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor

// Shipped code compiled against the header stubs: it binds to the real Android SDK members at runtime, so it only uses
// instance members, the Executor overloads the stubs declare as extra members, and Transaction.get.

/** The executor configured through the settings per instance; the Android SDK calls listeners on the main thread otherwise. */
internal val callbackExecutors = ConcurrentHashMap<FirebaseFirestore, Executor>()

private val FirebaseFirestore.callbackExecutor: Executor get() = callbackExecutors[this] ?: TaskExecutors.MAIN_THREAD

internal actual fun EncodedObject.toCompatMap(): Map<String, Any?> = android

internal actual fun DocumentSnapshot.nativeData(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = getData(serverTimestampBehavior)

internal actual fun DocumentSnapshot.nativeGet(field: String, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = get(field, serverTimestampBehavior)

internal actual fun DocumentSnapshot.nativeGet(fieldPath: FieldPath, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = get(fieldPath, serverTimestampBehavior)

internal actual suspend fun <T> FirebaseFirestore.runSuspendTransaction(updateFunction: suspend (Transaction) -> T): T = runTransaction { transaction -> runBlocking { updateFunction(transaction) } }.await()

internal actual suspend fun Transaction.getAwait(documentRef: DocumentReference): DocumentSnapshot = get(documentRef)

internal actual fun Query.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration = addSnapshotListener(firestore.callbackExecutor, metadataChanges, listener)

internal actual fun DocumentReference.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration = addSnapshotListener(firestore.callbackExecutor, metadataChanges, listener)
