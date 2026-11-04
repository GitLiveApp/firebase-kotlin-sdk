/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("FirestoreKt")
@file:JvmMultifileClass

package com.google.firebase.firestore

// The SDK's own reified extensions, inlined into their callers: they pass the class to the SDK's reflective mapper
// (so the facade part is only a header stub).

public actual inline fun <reified T> DocumentSnapshot.getField(field: String): T? = get(field, T::class.java)

public actual inline fun <reified T> DocumentSnapshot.getField(field: String, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = get(field, T::class.java, serverTimestampBehavior)

public actual inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath): T? = get(fieldPath, T::class.java)

public actual inline fun <reified T> DocumentSnapshot.getField(fieldPath: FieldPath, serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = get(fieldPath, T::class.java, serverTimestampBehavior)

public actual inline fun <reified T> DocumentSnapshot.toObject(): T? = toObject(T::class.java)

public actual inline fun <reified T> DocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T? = toObject(T::class.java, serverTimestampBehavior)

public actual inline fun <reified T> QueryDocumentSnapshot.toObject(): T = toObject(T::class.java)

public actual inline fun <reified T> QueryDocumentSnapshot.toObject(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): T = toObject(T::class.java, serverTimestampBehavior)

public actual inline fun <reified T> QuerySnapshot.toObjects(): List<T> = toObjects(T::class.java)

public actual inline fun <reified T> QuerySnapshot.toObjects(serverTimestampBehavior: DocumentSnapshot.ServerTimestampBehavior): List<T> = toObjects(T::class.java, serverTimestampBehavior)
