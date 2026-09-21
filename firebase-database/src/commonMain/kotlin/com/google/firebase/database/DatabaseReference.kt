/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.android.gms.tasks.Task

/**
 * A location in the database that can be read and written, as the Android SDK's `DatabaseReference`. Values are the
 * platform's natural types: `Map`, `List`, `String`, `Boolean`, `Long` and `Double` (and the platform SDK's own types on
 * Apple platforms and JS); custom classes are not mapped outside Android.
 */
public expect open class DatabaseReference : Query {
    /** The [FirebaseDatabase] this reference belongs to. */
    public val database: FirebaseDatabase

    /** The last segment of the path, or null at the root. */
    public val key: String?

    /** The parent location, or null at the root. */
    public val parent: DatabaseReference?

    /** The root of the database. */
    public val root: DatabaseReference

    /** A reference to [pathString] under this location. */
    public fun child(pathString: String): DatabaseReference

    /** A reference to a new child with a unique, chronologically ordered key. */
    public fun push(): DatabaseReference

    /** The disconnect operations of this location. */
    public fun onDisconnect(): OnDisconnect

    /** Writes [value] at this location, replacing the current data; null deletes it. */
    public fun setValue(value: Any?): Task<Nothing?>

    /** Writes [value] at this location and calls [listener] once the server has acknowledged it. */
    public fun setValue(value: Any?, listener: CompletionListener?)

    /** Writes [value] with [priority] at this location. */
    public fun setValue(value: Any?, priority: Any?): Task<Nothing?>

    /** Writes [value] with [priority] at this location and calls [listener] once the server has acknowledged it. */
    public fun setValue(value: Any?, priority: Any?, listener: CompletionListener?)

    /** Sets the priority of this location, a String, a Double or null. */
    public fun setPriority(priority: Any?): Task<Nothing?>

    /** Sets the priority of this location and calls [listener] once the server has acknowledged it. */
    public fun setPriority(priority: Any?, listener: CompletionListener?)

    /** Writes the children in [update] (paths to values; a null value deletes) without replacing the other children. */
    public fun updateChildren(update: Map<String, Any?>): Task<Nothing?>

    /** Writes the children in [update] and calls [listener] once the server has acknowledged it. */
    public fun updateChildren(update: Map<String, Any?>, listener: CompletionListener?)

    /** Deletes the data at this location. */
    public fun removeValue(): Task<Nothing?>

    /** Deletes the data at this location and calls [listener] once the server has acknowledged it. */
    public fun removeValue(listener: CompletionListener?)

    /** Runs [handler] as a transaction on this location, retrying it while the data changes; events fire for local updates. */
    public fun runTransaction(handler: Transaction.Handler)

    /** Runs [handler] as a transaction on this location; [fireLocalEvents] controls the events of intermediate states. */
    public fun runTransaction(handler: Transaction.Handler, fireLocalEvents: Boolean)

    /** Called once a write has been acknowledged by the server, or failed with [DatabaseError]. */
    public fun interface CompletionListener {
        public fun onComplete(error: DatabaseError?, ref: DatabaseReference)
    }

    public companion object {
        /** Disconnects every database from the backend until [goOnline]. */
        public fun goOffline()

        /** Reconnects every database to the backend after [goOffline]. */
        public fun goOnline()
    }
}
