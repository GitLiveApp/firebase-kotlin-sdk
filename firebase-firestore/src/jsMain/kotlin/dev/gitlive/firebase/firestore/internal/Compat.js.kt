/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore.internal

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
import com.google.firebase.firestore.objectKeys
import com.google.firebase.firestore.rethrow
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.js
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise

internal actual fun EncodedObject.toCompatMap(): Map<String, Any?> = js.let { json -> objectKeys(json).associateWith { json[it] } }

internal actual fun DocumentSnapshot.nativeData(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = nativeData(serverTimestampBehavior)

internal actual fun DocumentSnapshot.nativeGet(field: String, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = nativeGet(field, serverTimestampBehavior)

internal actual fun DocumentSnapshot.nativeGet(fieldPath: FieldPath, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = nativeGet(fieldPath, serverTimestampBehavior)

@OptIn(DelicateCoroutinesApi::class)
internal actual suspend fun <T> FirebaseFirestore.runSuspendTransaction(updateFunction: suspend (Transaction) -> T): T = rethrow {
    runPromiseTransaction(null) { transaction -> GlobalScope.promise { updateFunction(transaction) } }.await()
}

internal actual suspend fun Transaction.getAwait(documentRef: DocumentReference): DocumentSnapshot = rethrow { DocumentSnapshot(js.get(documentRef.js).await()) }

internal actual fun Query.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration = addSnapshotListener(metadataChanges, listener)

internal actual fun DocumentReference.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration = addSnapshotListener(metadataChanges, listener)
