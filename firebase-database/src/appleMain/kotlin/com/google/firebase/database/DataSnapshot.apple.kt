/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRMutableData
import platform.Foundation.NSNull
import platform.Foundation.allObjects

/** @property ios The underlying Firebase iOS SDK object. */
public actual class DataSnapshot internal constructor(public val ios: FIRDataSnapshot) {
    public actual fun exists(): Boolean = ios.exists()
    public actual val key: String? get() = ios.key
    public actual val ref: DatabaseReference get() = DatabaseReference(ios.ref)
    public actual val value: Any? get() = ios.value.fromIos()

    public actual fun getValue(useExportFormat: Boolean): Any? = if (useExportFormat) ios.valueInExportFormat().fromIos() else value

    @Suppress("UNCHECKED_CAST")
    public actual fun <T> getValue(t: GenericTypeIndicator<T>): T? = value as T?

    public actual val priority: Any? get() = ios.priority.fromIos()

    public actual fun child(path: String): DataSnapshot = DataSnapshot(ios.childSnapshotForPath(path))

    public actual fun hasChild(path: String): Boolean = ios.hasChild(path)

    public actual fun hasChildren(): Boolean = ios.hasChildren()

    public actual val childrenCount: Long get() = ios.childrenCount.toLong()

    public actual val children: Iterable<DataSnapshot> get() = ios.children.allObjects.map { DataSnapshot(it as FIRDataSnapshot) }

    override fun toString(): String = "DataSnapshot(key=$key, value=$value)"
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual class MutableData internal constructor(public val ios: FIRMutableData) {
    public actual val key: String? get() = ios.key

    public actual var value: Any?
        get() = ios.value.fromIos()
        set(value) {
            ios.setValue(value)
        }

    @Suppress("UNCHECKED_CAST")
    public actual fun <T> getValue(t: GenericTypeIndicator<T>): T? = value as T?

    public actual var priority: Any?
        get() = ios.priority.fromIos()
        set(value) {
            ios.setPriority(value)
        }

    public actual fun child(path: String): MutableData = MutableData(ios.childDataByAppendingPath(path))

    public actual fun hasChild(path: String): Boolean = ios.hasChildAtPath(path)

    public actual fun hasChildren(): Boolean = ios.hasChildren()

    public actual val childrenCount: Long get() = ios.childrenCount.toLong()

    public actual val children: Iterable<MutableData> get() = ios.children.allObjects.map { MutableData(it as FIRMutableData) }

    override fun toString(): String = "MutableData(key=$key, value=$value)"
}

/** The iOS SDK's `NSNull` is the absence of data. */
private fun Any?.fromIos(): Any? = takeIf { it !is NSNull }
