/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

/*
 * The Kotlin extensions of the Android SDK's firebase-database (DatabaseKt, ChildEvent), as plain common code: on
 * Android and the JVM the facade and the classes are header stubs that are stripped, so the SDK's own bind.
 */

/** The [FirebaseDatabase] of the default [FirebaseApp]; the Android SDK's `Firebase.database`. */
public val Firebase.database: FirebaseDatabase
    get() = FirebaseDatabase.getInstance()

/** The [FirebaseDatabase] of the default [FirebaseApp] at [url]. */
public fun Firebase.database(url: String): FirebaseDatabase = FirebaseDatabase.getInstance(url)

/** The [FirebaseDatabase] of [app]. */
public fun Firebase.database(app: FirebaseApp): FirebaseDatabase = FirebaseDatabase.getInstance(app)

/** The [FirebaseDatabase] of [app] at [url]. */
public fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase = FirebaseDatabase.getInstance(app, url)

/** The data as [T], as the Android SDK's reified `getValue`; on Apple platforms and JS a cast of [DataSnapshot.value]. */
public inline fun <reified T> DataSnapshot.getValue(): T? = getValue(object : GenericTypeIndicator<T>() {})

/** The data as [T], as the Android SDK's reified `getValue`; on Apple platforms and JS a cast of [MutableData.value]. */
public inline fun <reified T> MutableData.getValue(): T? = getValue(object : GenericTypeIndicator<T>() {})

/** A change to a child of a location, emitted by [Query.childEvents]. */
public sealed class ChildEvent {
    /** A child was added; [previousChildName] orders it after its sibling. */
    public data class Added(val snapshot: DataSnapshot, val previousChildName: String?) : ChildEvent()

    /** The data of a child changed. */
    public data class Changed(val snapshot: DataSnapshot, val previousChildName: String?) : ChildEvent()

    /** A child was removed. */
    public data class Removed(val snapshot: DataSnapshot) : ChildEvent()

    /** A child changed position because its priority changed. */
    public data class Moved(val snapshot: DataSnapshot, val previousChildName: String?) : ChildEvent()
}

/** The data at this location and every change to it as a [Flow], which fails if the listener is cancelled. */
public val Query.snapshots: Flow<DataSnapshot>
    get() = callbackFlow {
        val listener = addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trySend(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            },
        )
        awaitClose { removeEventListener(listener) }
    }

/** The changes to the children of this location as a [Flow], which fails if the listener is cancelled. */
public val Query.childEvents: Flow<ChildEvent>
    get() = callbackFlow {
        val listener = addChildEventListener(
            object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    trySend(ChildEvent.Added(snapshot, previousChildName))
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    trySend(ChildEvent.Changed(snapshot, previousChildName))
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    trySend(ChildEvent.Removed(snapshot))
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    trySend(ChildEvent.Moved(snapshot, previousChildName))
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            },
        )
        awaitClose { removeEventListener(listener) }
    }

/** The data at this location and every change to it as [T] (see [getValue]). */
public inline fun <reified T> Query.values(): Flow<T?> = snapshots.map { it.getValue<T>() }
