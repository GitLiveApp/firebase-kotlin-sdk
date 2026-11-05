/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/**
 * An immutable copy of the data at a location, as the Android SDK's `DataSnapshot`. `value` is the natural type of the
 * data: a `Map`, a `List`, a `String`, a `Boolean`, a `Long`, a `Double` or null. A custom class is read with the
 * reified `getValue<T>()` extension, which maps a `@Serializable` class with the Android SDK's rules on every platform;
 * the SDK's reflective `getValue(Class)` and `getValue(GenericTypeIndicator)` are deprecated with an error on Android.
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
 * The Android SDK's `GenericTypeIndicator`, kept so that its `getValue(GenericTypeIndicator)` members compile (they are
 * deprecated with an error towards the reified `getValue<T>()`, which maps through the format on every platform).
 */
public abstract class GenericTypeIndicator<T>
