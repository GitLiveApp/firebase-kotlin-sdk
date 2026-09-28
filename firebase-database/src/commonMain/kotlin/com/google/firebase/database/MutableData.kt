/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/**
 * The data of a location inside a transaction, as the Android SDK's `MutableData`: [value] is read and then set to the
 * new data before the [Transaction.Handler] returns [Transaction.success].
 */
public expect class MutableData {
    /** The last segment of the path, or null at the root. */
    public val key: String?

    /** The current data as its natural type (see [DataSnapshot]); setting it replaces the data of the transaction. */
    public var value: Any?

    /** The data as [T]; on Apple platforms and JS a cast of [value]. */
    public fun <T> getValue(t: GenericTypeIndicator<T>): T?

    /** The priority of the location; setting it changes the priority written by the transaction. */
    public var priority: Any?

    /** The data of [path] under this location; changes to it are part of the transaction. */
    public fun child(path: String): MutableData

    /** Whether there is data at [path] under this location. */
    public fun hasChild(path: String): Boolean

    /** Whether the location has children. */
    public fun hasChildren(): Boolean

    /** The number of children. */
    public val childrenCount: Long

    /** The children of this location. */
    public val children: Iterable<MutableData>
}

/** A transaction on a location, as the Android SDK's `Transaction`: [Handler.doTransaction] returns [success] or [abort]. */
public expect class Transaction() {
    /** Runs the transaction and receives its outcome. */
    public interface Handler {
        /** Reads and updates [currentData]; returns [success] to commit the new data or [abort] to leave it unchanged. */
        public fun doTransaction(currentData: MutableData): Result

        /** Called once the transaction is committed ([committed] with the final [currentData]), aborted, or failed with [error]. */
        public fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?)
    }

    /** The outcome of [Handler.doTransaction]. */
    public class Result {
        /** Whether the transaction commits [MutableData]'s data. */
        public val isSuccess: Boolean
    }

    public companion object {
        /** Leaves the data unchanged. */
        public fun abort(): Result

        /** Commits the data of [resultData]. */
        public fun success(resultData: MutableData): Result
    }
}
