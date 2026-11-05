/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.serialization.FirebaseFormat
import kotlin.reflect.KClass

/**
 * The format that maps a `@Serializable` class to and from a document's values on every platform, with the Android SDK's
 * Firestore rules, the format's own mapping annotations and the platform SDK's value types (see [FirebaseFormat.Firestore]
 * and [FirestoreBindings]): the `setData`/`updateData`/`addData` extensions write through
 * [FirebaseFormat.convertToPlainValue] and the reified `toObject`, `toObjects` and `getField` read through
 * [FirebaseFormat.decodeFromPlainValue] (all in `com.google.firebase.serialization`, see FirestoreMapping.kt). It takes the
 * place of the Android SDK's reflective mapping, whose members are deprecated with an error.
 */
@PublishedApi
internal val firestoreFormat: FirebaseFormat.Firestore = FirebaseFormat.Firestore(FirestoreBindings)

/** The module's side of [FirebaseFormat.Firestore]: its value types, and the platform SDK's classes for them. */
internal object FirestoreBindings : FirebaseFormat.Firestore.Bindings {
    override val nativeTypes: List<KClass<*>> = listOf(GeoPoint::class, Blob::class, VectorValue::class, DocumentReference::class, FieldValue::class, FieldPath::class)

    override val documentReferenceType: KClass<*> = DocumentReference::class

    override fun documentId(documentReference: Any): String = (documentReference as DocumentReference).id

    override fun documentPath(documentReference: Any): String = (documentReference as DocumentReference).path

    // Through the internal static (not the companion, which the real Android SDK class lacks; see FirestoreInternals.kt).
    override fun serverTimestamp(): Any = fieldValueServerTimestamp()

    override fun toSdkValue(value: Any): Any = value.toSdkValue()

    override fun fromSdkValue(value: Any): Any = value.fromSdkValue()
}

/**
 * The platform SDK's value for one of the layer's value types (a `Timestamp`, [GeoPoint], [Blob], [DocumentReference],
 * [FieldValue] or [FieldPath]); the value itself on Android and the JVM, whose SDKs the classes belong to.
 */
internal expect fun Any.toSdkValue(): Any

/** The layer's value for one of the platform SDK's value types, and any other value as it is. */
internal expect fun Any.fromSdkValue(): Any
