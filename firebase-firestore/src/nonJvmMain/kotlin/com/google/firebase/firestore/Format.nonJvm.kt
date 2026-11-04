/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.serialization.FirebaseFormat
import com.google.firebase.serialization.MappingAnnotations
import kotlin.reflect.KClass

/**
 * The format through which the layer exchanges values with the platform SDK where the Android SDK would map a custom
 * class by reflection: the data of `set`, `update` and `add` goes through [FirebaseFormat.convertToPlainValue] and the
 * reified `toObject`, `toObjects` and `getField` read through [FirebaseFormat.decodeFromPlainValue], with the Android
 * SDK's Firestore rules, this module's mapping annotations and the platform SDK's own value types (see [FirestoreBindings]).
 */
@PublishedApi
internal val firestoreFormat: FirebaseFormat.Firestore = FirebaseFormat.Firestore(FirestoreBindings)

/** The module's side of [FirebaseFormat.Firestore]: its annotations and value types, and the platform SDK's classes for the latter. */
internal object FirestoreBindings : FirebaseFormat.Firestore.Bindings {
    override val annotations: MappingAnnotations = MappingAnnotations(
        propertyName = { (it as? PropertyName)?.value },
        exclude = Exclude::class,
        ignoreExtraProperties = IgnoreExtraProperties::class,
        throwOnExtraProperties = ThrowOnExtraProperties::class,
        serverTimestamp = ServerTimestamp::class,
        documentId = DocumentId::class,
    )

    override val nativeTypes: List<KClass<*>> = listOf(GeoPoint::class, Blob::class, DocumentReference::class, FieldValue::class, FieldPath::class)

    override val documentReferenceType: KClass<*> = DocumentReference::class

    override fun documentId(documentReference: Any): String = (documentReference as DocumentReference).id

    override fun documentPath(documentReference: Any): String = (documentReference as DocumentReference).path

    override fun serverTimestamp(): Any = FieldValue.serverTimestamp()

    override fun toSdkValue(value: Any): Any = value.toSdkValue()

    override fun fromSdkValue(value: Any): Any = value.fromSdkValue()
}

/** The platform SDK's value for one of the layer's value types (a `Timestamp`, [GeoPoint], [Blob], [DocumentReference], [FieldValue] or [FieldPath]). */
internal expect fun Any.toSdkValue(): Any

/** The layer's value for one of the platform SDK's value types, and any other value as it is. */
internal expect fun Any.fromSdkValue(): Any
