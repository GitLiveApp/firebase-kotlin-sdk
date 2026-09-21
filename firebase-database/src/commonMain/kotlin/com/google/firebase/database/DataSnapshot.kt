/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/**
 * An immutable copy of the data at a location, as the Android SDK's `DataSnapshot`. `value` is the natural type of the
 * data: a `Map`, a `List`, a `String`, a `Boolean`, a `Long`, a `Double` or null. Mapping to custom classes through
 * `getValue(Class)` is Android-only; the [GenericTypeIndicator] form casts the value on the other platforms.
 */
public expect class DataSnapshot {
    /** Whether there is data at the location. */
    public fun exists(): Boolean

    /** The last segment of the path, or null at the root. */
    public val key: String?

    /** The reference to the location of this snapshot. */
    public val ref: DatabaseReference

    /** The data at the location as its natural type, or null. */
    public val value: Any?

    /** The data with priorities included (`.value` / `.priority` objects) when [useExportFormat] is true. */
    public fun getValue(useExportFormat: Boolean): Any?

    /** The data as [T]; on Apple platforms and JS a cast of [value], which maps and lists satisfy. */
    public fun <T> getValue(t: GenericTypeIndicator<T>): T?

    /** The priority of the location: a String, a Double or null. */
    public val priority: Any?

    /** The snapshot of [path] under this location. */
    public fun child(path: String): DataSnapshot

    /** Whether there is data at [path] under this location. */
    public fun hasChild(path: String): Boolean

    /** Whether the location has children. */
    public fun hasChildren(): Boolean

    /** The number of children. */
    public val childrenCount: Long

    /** The snapshots of the children, in query order. */
    public val children: Iterable<DataSnapshot>
}

/**
 * Names the type [T] a value is mapped to, as the Android SDK's `GenericTypeIndicator`, for `getValue` calls such as
 * `snapshot.getValue(object : GenericTypeIndicator<Map<String, Long>>() {})`.
 */
public abstract class GenericTypeIndicator<T>
