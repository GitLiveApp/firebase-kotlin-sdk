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
import com.google.firebase.firestore.get
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.ios
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

@Suppress("UNCHECKED_CAST")
internal actual fun EncodedObject.toCompatMap(): Map<String, Any?> = ios as Map<String, Any?>

internal actual fun DocumentSnapshot.nativeData(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = getData(serverTimestampBehavior)

internal actual fun DocumentSnapshot.nativeGet(field: String, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = get(field, serverTimestampBehavior)

internal actual fun DocumentSnapshot.nativeGet(fieldPath: FieldPath, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = get(fieldPath, serverTimestampBehavior)

internal actual suspend fun <T> FirebaseFirestore.runSuspendTransaction(updateFunction: suspend (Transaction) -> T): T = runBlockTransaction(null) { transaction -> runBlocking { updateFunction(transaction) } }.await()

internal actual suspend fun Transaction.getAwait(documentRef: DocumentReference): DocumentSnapshot = get(documentRef)

internal actual fun Query.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration = addSnapshotListener(metadataChanges, listener)

internal actual fun DocumentReference.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration = addSnapshotListener(metadataChanges, listener)
