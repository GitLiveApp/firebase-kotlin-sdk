/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.serialization.decodeFromPlainValue

// Reads through the serializer of T with the Android SDK's rules (see firestoreFormat); `Any` stands for the value as it is.

public actual inline fun <reified T> DocumentSnapshot.getField(field: String): T? = firestoreFormat.decodeFromPlainValue<T?>(get(field), reference)

public actual inline fun <reified T> DocumentSnapshot.getField(field: String, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = firestoreFormat.decodeFromPlainValue<T?>(get(field, serverTimestampBehavior), reference)

public actual inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath): T? = firestoreFormat.decodeFromPlainValue<T?>(get(fieldPath), reference)

public actual inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = firestoreFormat.decodeFromPlainValue<T?>(get(fieldPath, serverTimestampBehavior), reference)

public actual inline fun <reified T> DocumentSnapshot.toObject(): T? = firestoreFormat.decodeFromPlainValue<T?>(data, reference)

public actual inline fun <reified T> DocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = firestoreFormat.decodeFromPlainValue<T?>(getData(serverTimestampBehavior), reference)

public actual inline fun <reified T> QueryDocumentSnapshot.toObject(): T = firestoreFormat.decodeFromPlainValue<T>(data, reference)

public actual inline fun <reified T> QueryDocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T = firestoreFormat.decodeFromPlainValue<T>(getData(serverTimestampBehavior), reference)

public actual inline fun <reified T> QuerySnapshot.toObjects(): List<T> = map { it.toObject<T>() }

public actual inline fun <reified T> QuerySnapshot.toObjects(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): List<T> = map { it.toObject<T>(serverTimestampBehavior) }
