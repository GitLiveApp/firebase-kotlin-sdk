/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Timestamp
import platform.Foundation.NSData
import swiftPMImport.dev.gitlive.firebase.app.FIRTimestamp
import swiftPMImport.dev.gitlive.firebase.firestore.FIRDocumentReference
import swiftPMImport.dev.gitlive.firebase.firestore.FIRGeoPoint

// The iOS SDK's classes for the layer's value types, which the format writes and reads (see firestoreFormat).

internal actual fun Any.toSdkValue(): Any = when (this) {
    is Timestamp -> FIRTimestamp(seconds, nanoseconds)
    is GeoPoint -> FIRGeoPoint(latitude, longitude)
    is DocumentReference -> ios
    is FieldValue -> toIosFieldValue()
    is Blob -> toBytes().toNSData()
    is FieldPath -> toIos()
    else -> this
}

internal actual fun Any.fromSdkValue(): Any = when (this) {
    is FIRTimestamp -> Timestamp(seconds, nanoseconds)
    is FIRGeoPoint -> GeoPoint(latitude, longitude)
    is FIRDocumentReference -> DocumentReference(this)
    is NSData -> Blob.fromBytes(toByteArray())
    else -> this
}
