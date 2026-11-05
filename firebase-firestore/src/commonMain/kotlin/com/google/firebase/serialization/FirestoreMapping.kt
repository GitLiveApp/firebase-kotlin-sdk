/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The SDK's write members map a custom class by reflection on Android; these call them with the data mapped by the format.
@file:Suppress("DEPRECATION_ERROR")

package com.google.firebase.serialization

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.firestoreFormat
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * The Cloud Firestore entry points of FirebaseFormat.Firestore (see firestoreFormat), which maps a `@Serializable` class on
 * every platform in place of the Android SDK's reflective mapping: `set(Any)`, `update(Map)` and `add(Any)` are deprecated
 * with an error in favour of `setData`/`updateData`/`addData`, which plain maps work through too, and the SDK's reified
 * `toObject`, `toObjects`, `getField` and `dataObjects` in favour of the ones here. The reads keep the SDK's names but live
 * in this package: on Android and the JVM a call in `com.google.firebase.firestore` resolves to the SDK's own `FirestoreKt`
 * facade (this module's is a stripped header stub), whose inline bodies map by reflection.
 */

/**
 * Writes [data], a map of field values or a `@Serializable` class, replacing the document (see `DocumentReference.set`).
 * The data is mapped by the format with its static type, so a generic class maps too; an `Any` maps by its runtime type.
 */
public inline fun <reified T : Any> DocumentReference.setData(data: T): Task<Nothing?> = set(plain(data))

/** Writes [data] (see [setData]) according to [options]. */
public inline fun <reified T : Any> DocumentReference.setData(data: T, options: SetOptions): Task<Nothing?> = set(plain(data), options)

/** Updates the fields in [data], each a field value or a `@Serializable` class mapped by the format (see `DocumentReference.update`). */
public fun DocumentReference.updateData(data: Map<String, Any?>): Task<Nothing?> = update(data.toPlain())

/** Adds a new document with an automatically generated id and [data], a map of field values or a `@Serializable` class mapped by the format (see `CollectionReference.add`). */
public inline fun <reified T : Any> CollectionReference.addData(data: T): Task<DocumentReference> = add(plain(data))

/** Writes [data] (see [DocumentReference.setData]) to [documentRef] in the transaction. */
public inline fun <reified T : Any> Transaction.setData(documentRef: DocumentReference, data: T): Transaction = set(documentRef, plain(data))

/** Writes [data] (see [DocumentReference.setData]) to [documentRef] in the transaction according to [options]. */
public inline fun <reified T : Any> Transaction.setData(documentRef: DocumentReference, data: T, options: SetOptions): Transaction = set(documentRef, plain(data), options)

/** Updates the fields in [data] (see [DocumentReference.updateData]) of [documentRef] in the transaction. */
public fun Transaction.updateData(documentRef: DocumentReference, data: Map<String, Any?>): Transaction = update(documentRef, data.toPlain())

/** Writes [data] (see [DocumentReference.setData]) to [documentRef] in the batch. */
public inline fun <reified T : Any> WriteBatch.setData(documentRef: DocumentReference, data: T): WriteBatch = set(documentRef, plain(data))

/** Writes [data] (see [DocumentReference.setData]) to [documentRef] in the batch according to [options]. */
public inline fun <reified T : Any> WriteBatch.setData(documentRef: DocumentReference, data: T, options: SetOptions): WriteBatch = set(documentRef, plain(data), options)

/** Updates the fields in [data] (see [DocumentReference.updateData]) of [documentRef] in the batch. */
public fun WriteBatch.updateData(documentRef: DocumentReference, data: Map<String, Any?>): WriteBatch = update(documentRef, data.toPlain())

/**
 * The value of [field] as [T], as the Android SDK's reified `getField`, through the serializer of [T] with the Android
 * SDK's rules (`FirebaseFormat.Firestore`, see firestoreFormat) on every platform; `Any` stands for the value as it is.
 * It takes the place of the SDK's reflective `get(String, Class)` and of the SDK's own reified `getField<T>()`
 * (`com.google.firebase.firestore.getField`), which is deprecated with an error.
 */
public inline fun <reified T> DocumentSnapshot.getField(field: String): T? = firestoreFormat.decodeFromPlainValue<T?>(get(field), reference)

/** The value of [field] as [T] (see [getField]). */
public inline fun <reified T> DocumentSnapshot.getField(field: String, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = firestoreFormat.decodeFromPlainValue<T?>(get(field, serverTimestampBehavior), reference)

/** The value of [fieldPath] as [T] (see [getField]). */
public inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath): T? = firestoreFormat.decodeFromPlainValue<T?>(get(fieldPath), reference)

/** The value of [fieldPath] as [T] (see [getField]). */
public inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = firestoreFormat.decodeFromPlainValue<T?>(get(fieldPath, serverTimestampBehavior), reference)

/**
 * The document as [T], as the Android SDK's reified `toObject`, through the serializer of [T] with the Android SDK's
 * rules (`FirebaseFormat.Firestore`, see firestoreFormat) on every platform, `@DocumentId` and `@ServerTimestamp`
 * included; null when the document does not exist. It takes the place of the SDK's reflective `toObject(Class)` and of
 * the SDK's own reified `toObject<T>()` (`com.google.firebase.firestore.toObject`), which is deprecated with an error.
 */
public inline fun <reified T> DocumentSnapshot.toObject(): T? = firestoreFormat.decodeFromPlainValue<T?>(data, reference)

/** The document as [T] (see [toObject]), with the server timestamps resolved as [serverTimestampBehavior] says. */
public inline fun <reified T> DocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = firestoreFormat.decodeFromPlainValue<T?>(getData(serverTimestampBehavior), reference)

/** The document as [T] (see [DocumentSnapshot.toObject]); a query document always exists. */
public inline fun <reified T> QueryDocumentSnapshot.toObject(): T = firestoreFormat.decodeFromPlainValue<T>(data, reference)

/** The document as [T] (see [DocumentSnapshot.toObject]), with the server timestamps resolved as [serverTimestampBehavior] says. */
public inline fun <reified T> QueryDocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T = firestoreFormat.decodeFromPlainValue<T>(getData(serverTimestampBehavior), reference)

/** The documents as [T] (see [DocumentSnapshot.toObject]), as the Android SDK's reified `toObjects`. */
public inline fun <reified T> QuerySnapshot.toObjects(): List<T> = map { it.toObject<T>() }

/** The documents as [T] (see [DocumentSnapshot.toObject]), with the server timestamps resolved as [serverTimestampBehavior] says. */
public inline fun <reified T> QuerySnapshot.toObjects(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): List<T> = map { it.toObject<T>(serverTimestampBehavior) }

/** The document data as [T] (see [toObject]) and every change to it as a [Flow], as the Android SDK's `dataObjects`. */
public inline fun <reified T> DocumentReference.dataObjects(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<T?> = snapshots(metadataChanges).map { it.toObject<T>() }

/** The query results as [T] (see [toObject]) and every change to them as a [Flow], as the Android SDK's `dataObjects`. */
public inline fun <reified T> Query.dataObjects(metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE): Flow<List<T>> = snapshots(metadataChanges).map { it.toObjects<T>() }

/** [data] mapped by the format with its static type, as the SDK member takes it (see [toSdkData]). */
@PublishedApi
internal inline fun <reified T : Any> plain(data: T): Any = (firestoreFormat.encodeToPlainValue<T>(data) ?: data).toSdkData()

/**
 * A value the format has mapped, as the SDK member takes it: itself on Android and the JVM, whose SDK would otherwise map
 * a class by reflection, and as a [PlainValue] on the other platforms, whose actuals map every value they write through
 * the format and pass one through.
 */
@PublishedApi
internal expect fun Any.toSdkData(): Any

/** The fields of an update, each mapped by the format (see [toSdkData]); the keys (field paths) are kept as they are. */
internal expect fun Map<String, Any?>.toPlain(): Map<String, Any?>
