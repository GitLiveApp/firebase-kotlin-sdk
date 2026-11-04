/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The SDK's write members map a custom class by reflection on Android; these call them with the data mapped by the format.
@file:Suppress("DEPRECATION_ERROR")

package com.google.firebase.serialization

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.OnDisconnect
import com.google.firebase.database.Query
import com.google.firebase.database.databaseFormat
import com.google.firebase.database.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * The Realtime Database entry points of FirebaseFormat.Database (see databaseFormat), which maps a `@Serializable` class on
 * every platform in place of the Android SDK's reflective mapping: `setValue(Any?)`, `updateChildren(Map)` and
 * `MutableData.value` are deprecated with an error in favour of `setData`/`updateData`, which plain values and maps work
 * through too, and the SDK's reified `getValue<T>()` and `values<T>()` in favour of the ones here. The reads keep the
 * SDK's names but live in this package: on Android and the JVM a call in `com.google.firebase.database` resolves to the
 * SDK's own `DatabaseKt` facade (this module's is a stripped header stub), whose inline bodies map by reflection.
 */

/**
 * Writes [value], a plain value or a `@Serializable` class, to this location (see `DatabaseReference.setValue`). The value
 * is mapped by the format with its static type, so a generic class maps too; an `Any` maps by its runtime type.
 */
public inline fun <reified T> DatabaseReference.setData(value: T): Task<Nothing?> = setValue(plain(value))

/** Writes [value] (see [setData]) and calls [listener] with the outcome. */
public inline fun <reified T> DatabaseReference.setData(value: T, listener: DatabaseReference.CompletionListener?) {
    setValue(plain(value), listener)
}

/** Writes [value] (see [setData]) with [priority]. */
public inline fun <reified T> DatabaseReference.setData(value: T, priority: Any?): Task<Nothing?> = setValue(plain(value), priority)

/** Writes [value] (see [setData]) with [priority] and calls [listener] with the outcome. */
public inline fun <reified T> DatabaseReference.setData(value: T, priority: Any?, listener: DatabaseReference.CompletionListener?) {
    setValue(plain(value), priority, listener)
}

/** Updates the children in [update], each a plain value or a `@Serializable` class mapped by the format (see `DatabaseReference.updateChildren`). */
public fun DatabaseReference.updateData(update: Map<String, Any?>): Task<Nothing?> = updateChildren(update.toPlain())

/** Updates the children in [update] (see [updateData]) and calls [listener] with the outcome. */
public fun DatabaseReference.updateData(update: Map<String, Any?>, listener: DatabaseReference.CompletionListener?) {
    updateChildren(update.toPlain(), listener)
}

/** Writes [value], a plain value or a `@Serializable` class mapped by the format, when the client disconnects (see `OnDisconnect.setValue`). */
public inline fun <reified T> OnDisconnect.setData(value: T): Task<Nothing?> = setValue(plain(value))

/** Writes [value] on disconnect (see [setData]) and calls [listener] with the outcome. */
public inline fun <reified T> OnDisconnect.setData(value: T, listener: DatabaseReference.CompletionListener?) {
    setValue(plain(value), listener)
}

/** Writes [value] on disconnect (see [setData]) with [priority]. */
public inline fun <reified T> OnDisconnect.setData(value: T, priority: String?): Task<Nothing?> = setValue(plain(value), priority)

/** Writes [value] on disconnect (see [setData]) with [priority] and calls [listener] with the outcome. */
public inline fun <reified T> OnDisconnect.setData(value: T, priority: String?, listener: DatabaseReference.CompletionListener?) {
    setValue(plain(value), priority, listener)
}

/** Writes [value] on disconnect (see [setData]) with [priority]. */
public inline fun <reified T> OnDisconnect.setData(value: T, priority: Double): Task<Nothing?> = setValue(plain(value), priority)

/** Writes [value] on disconnect (see [setData]) with [priority] and calls [listener] with the outcome. */
public inline fun <reified T> OnDisconnect.setData(value: T, priority: Double, listener: DatabaseReference.CompletionListener?) {
    setValue(plain(value), priority, listener)
}

/** Writes [value] on disconnect (see [setData]) with [priority] and calls [listener] with the outcome. */
public inline fun <reified T> OnDisconnect.setData(value: T, priority: Map<*, *>?, listener: DatabaseReference.CompletionListener?) {
    setValue(plain(value), priority, listener)
}

/** Updates the children in [update] when the client disconnects (see [DatabaseReference.updateData]). */
public fun OnDisconnect.updateData(update: Map<String, Any?>): Task<Nothing?> = updateChildren(update.toPlain())

/** Updates the children in [update] on disconnect (see [updateData]) and calls [listener] with the outcome. */
public fun OnDisconnect.updateData(update: Map<String, Any?>, listener: DatabaseReference.CompletionListener?) {
    updateChildren(update.toPlain(), listener)
}

/** Replaces the data of the transaction with [value], a plain value or a `@Serializable` class mapped by the format (see `MutableData.value`). */
public inline fun <reified T> MutableData.setData(value: T) {
    this.value = plain(value)
}

/**
 * The data as [T], as the Android SDK's reified `getValue`, through the serializer of [T] with the Android SDK's rules
 * (`FirebaseFormat.Database`, see databaseFormat) on every platform: a `@Serializable` class, a `Map`, a `List` or a
 * primitive reads the same everywhere. It takes the place of the SDK's reflective `getValue(Class)` and of the SDK's own
 * reified `getValue<T>()` (`com.google.firebase.database.getValue`), which is deprecated with an error.
 */
public inline fun <reified T> DataSnapshot.getValue(): T? = databaseFormat.decodeFromPlainValue<T?>(value)

/** The data as [T], as the Android SDK's reified `getValue` (see [DataSnapshot.getValue]). */
public inline fun <reified T> MutableData.getValue(): T? = databaseFormat.decodeFromPlainValue<T?>(value)

/** The data at this location as [T] (see [DataSnapshot.getValue]) and every change to it as a [Flow], as the Android SDK's `values`. */
public inline fun <reified T> Query.values(): Flow<T?> = snapshots.map { it.getValue<T>() }

/** [value] mapped by the format with its static type, as the SDK member takes it (see [toSdkData]). */
@PublishedApi
internal inline fun <reified T> plain(value: T): Any? = (if (value == null) null else databaseFormat.encodeToPlainValue<T>(value)).toSdkData()

/**
 * A value the format has mapped, as the SDK member takes it: itself on Android and the JVM, whose SDK would otherwise map
 * a class by reflection, and as a [PlainValue] on the other platforms, whose actuals map every value they write through
 * the format and pass one through.
 */
@PublishedApi
internal expect fun Any?.toSdkData(): Any?

/** The children of an update, each mapped by the format (see [toSdkData]); the keys (paths) are kept as they are. */
internal expect fun Map<String, Any?>.toPlain(): Map<String, Any?>
