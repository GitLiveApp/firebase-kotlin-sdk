/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Timestamp
import dev.gitlive.firebase.firestore.externals.Bytes
import dev.gitlive.firebase.firestore.externals.DocumentReference as JsDocumentReference
import dev.gitlive.firebase.firestore.externals.GeoPoint as JsGeoPoint
import dev.gitlive.firebase.firestore.externals.Timestamp as JsTimestamp
import dev.gitlive.firebase.firestore.externals.VectorValue as JsVectorValue
import dev.gitlive.firebase.firestore.externals.vector as jsVector

// The JS SDK's classes for the layer's value types, which the format writes and reads (see firestoreFormat).

internal actual fun Any.toSdkValue(): Any = when (this) {
    is Timestamp -> JsTimestamp(seconds.toDouble(), nanoseconds.toDouble())
    is GeoPoint -> JsGeoPoint(latitude, longitude)
    is VectorValue -> jsVector(toArray().toTypedArray())
    is DocumentReference -> js
    is FieldValue -> toJsFieldValue()
    is Blob -> Bytes.fromUint8Array(toBytes().toUint8Array())
    is FieldPath -> toJs()
    else -> this
}

internal actual fun Any.fromSdkValue(): Any = when {
    this is JsTimestamp -> Timestamp(seconds.toLong(), nanoseconds.toInt())
    this is JsGeoPoint -> GeoPoint(latitude, longitude)
    this is JsVectorValue -> VectorValue.of(toArray().toDoubleArray())
    this is JsDocumentReference -> DocumentReference(this)
    this is Bytes -> Blob.fromBytes(toUint8Array().toByteArray())
    else -> this
}
