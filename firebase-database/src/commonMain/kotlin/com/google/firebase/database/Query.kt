/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.android.gms.tasks.Task

/**
 * A read of a location with ordering and filtering, as the Android SDK's `Query`. Listeners are called on the thread the
 * platform SDK delivers events on. `keepSynced` is in nonJsMain (the JS SDK has no equivalent).
 */
public expect open class Query {
    /** The reference to the location of this query. */
    public val ref: DatabaseReference

    /** Adds [listener] for the data at this location and its changes; returns it for [removeEventListener]. */
    public fun addValueEventListener(listener: ValueEventListener): ValueEventListener

    /** Adds [listener] for the changes to the children of this location; returns it for [removeEventListener]. */
    public fun addChildEventListener(listener: ChildEventListener): ChildEventListener

    /** Calls [listener] once with the data at this location. */
    public fun addListenerForSingleValueEvent(listener: ValueEventListener)

    /** Removes a listener added with [addValueEventListener]. */
    public fun removeEventListener(listener: ValueEventListener)

    /** Removes a listener added with [addChildEventListener]. */
    public fun removeEventListener(listener: ChildEventListener)

    /** Reads the data at this location from the server, falling back to the cache when offline. */
    public fun get(): Task<DataSnapshot>

    /** Orders the children by the value at [path] within each of them. */
    public fun orderByChild(path: String): Query

    /** Orders the children by their keys. */
    public fun orderByKey(): Query

    /** Orders the children by their priorities. */
    public fun orderByPriority(): Query

    /** Orders the children by their values. */
    public fun orderByValue(): Query

    /** Limits the results to the first [limit] children. */
    public fun limitToFirst(limit: Int): Query

    /** Limits the results to the last [limit] children. */
    public fun limitToLast(limit: Int): Query

    /** Limits the results to the children whose ordered value is at least [value]. */
    public fun startAt(value: String?): Query

    /** Limits the results to the children whose ordered value is at least [value], breaking ties by [key]. */
    public fun startAt(value: String?, key: String?): Query
    public fun startAt(value: Double): Query
    public fun startAt(value: Double, key: String?): Query
    public fun startAt(value: Boolean): Query
    public fun startAt(value: Boolean, key: String?): Query

    /** Limits the results to the children whose ordered value is greater than [value]. */
    public fun startAfter(value: String?): Query

    /** Limits the results to the children whose ordered value is greater than [value], breaking ties by [key]. */
    public fun startAfter(value: String?, key: String?): Query
    public fun startAfter(value: Double): Query
    public fun startAfter(value: Double, key: String?): Query
    public fun startAfter(value: Boolean): Query
    public fun startAfter(value: Boolean, key: String?): Query

    /** Limits the results to the children whose ordered value is at most [value]. */
    public fun endAt(value: String?): Query

    /** Limits the results to the children whose ordered value is at most [value], breaking ties by [key]. */
    public fun endAt(value: String?, key: String?): Query
    public fun endAt(value: Double): Query
    public fun endAt(value: Double, key: String?): Query
    public fun endAt(value: Boolean): Query
    public fun endAt(value: Boolean, key: String?): Query

    /** Limits the results to the children whose ordered value is less than [value]. */
    public fun endBefore(value: String?): Query

    /** Limits the results to the children whose ordered value is less than [value], breaking ties by [key]. */
    public fun endBefore(value: String?, key: String?): Query
    public fun endBefore(value: Double): Query
    public fun endBefore(value: Double, key: String?): Query
    public fun endBefore(value: Boolean): Query
    public fun endBefore(value: Boolean, key: String?): Query

    /** Limits the results to the children whose ordered value equals [value]. */
    public fun equalTo(value: String?): Query

    /** Limits the results to the child whose ordered value equals [value] and whose key is [key]. */
    public fun equalTo(value: String?, key: String?): Query
    public fun equalTo(value: Double): Query
    public fun equalTo(value: Double, key: String?): Query
    public fun equalTo(value: Boolean): Query
    public fun equalTo(value: Boolean, key: String?): Query
}
