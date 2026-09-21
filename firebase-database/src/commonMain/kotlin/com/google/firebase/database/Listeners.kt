/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/*
 * The listener interfaces of the Android SDK, as plain common code: on Android and the JVM they are header stubs that are
 * stripped, so the SDK's own interfaces bind.
 */

/** Receives the data at a location and its changes; see [Query.addValueEventListener]. */
public interface ValueEventListener {
    /** Called with the data at the location, and again after every change. */
    public fun onDataChange(snapshot: DataSnapshot)

    /** Called when the listener is cancelled, for example because the client lost read permission. */
    public fun onCancelled(error: DatabaseError)
}

/** Receives the changes to the children of a location; see [Query.addChildEventListener]. */
public interface ChildEventListener {
    /** Called for every existing child and then for each new one; [previousChildName] orders it after its sibling. */
    public fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)

    /** Called when the data of a child changed. */
    public fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)

    /** Called when a child is removed. */
    public fun onChildRemoved(snapshot: DataSnapshot)

    /** Called when a child changed position because its priority changed. */
    public fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?)

    /** Called when the listener is cancelled, for example because the client lost read permission. */
    public fun onCancelled(error: DatabaseError)
}
