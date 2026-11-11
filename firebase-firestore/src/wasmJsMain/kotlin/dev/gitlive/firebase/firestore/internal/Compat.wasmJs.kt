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
import com.google.firebase.firestore.SnapshotListenOptions
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.rethrow
import dev.gitlive.firebase.externals.awaitValue
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.map
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.JsReference
import kotlin.js.get
import kotlin.js.toJsReference

/** The encoded document as the Kotlin map it is; the layer converts the values to the JS SDK's (a Kotlin value cannot be stored in a JS object). */
internal actual fun EncodedObject.toCompatMap(): Map<String, Any?> = map

// The data as the JS SDK reads it: the decoders convert the SDK's value types themselves on this platform (see toCompatSpecialValue).

internal actual fun DocumentSnapshot.nativeData(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = nativeData(serverTimestampBehavior)

internal actual fun DocumentSnapshot.nativeGet(field: String, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = nativeGet(field, serverTimestampBehavior)

internal actual fun DocumentSnapshot.nativeGet(fieldPath: FieldPath, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): Any? = nativeGet(fieldPath, serverTimestampBehavior)

/** The result travels through the JS SDK's promise as a reference to the Kotlin value. */
@OptIn(DelicateCoroutinesApi::class)
@Suppress("UNCHECKED_CAST")
internal actual suspend fun <T> FirebaseFirestore.runSuspendTransaction(updateFunction: suspend (Transaction) -> T): T = rethrow {
    val result = runPromiseTransaction(null) { transaction -> GlobalScope.promise { updateFunction(transaction)?.toJsReference() } }.awaitValue()
    (result as? JsReference<Any>)?.get() as T
}

internal actual suspend fun Transaction.getAwait(documentRef: DocumentReference): DocumentSnapshot = rethrow { DocumentSnapshot(js.get(documentRef.js).awaitValue()) }

internal actual fun Query.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration = addSnapshotListener(metadataChanges, listener)

internal actual fun DocumentReference.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration = addSnapshotListener(metadataChanges, listener)

internal actual fun Query.wrapperSnapshotListener(listenOptions: SnapshotListenOptions, listener: EventListener<QuerySnapshot>): ListenerRegistration = addSnapshotListener(listenOptions, listener)

internal actual fun DocumentReference.wrapperSnapshotListener(listenOptions: SnapshotListenOptions, listener: EventListener<DocumentSnapshot>): ListenerRegistration = addSnapshotListener(listenOptions, listener)
