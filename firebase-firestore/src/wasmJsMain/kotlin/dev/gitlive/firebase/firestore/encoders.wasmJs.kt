/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.fromSdkValue

/** The JS SDK's Timestamp, GeoPoint, DocumentReference and Bytes as the Android-SDK-shaped types; any other value as it is. */
internal actual fun Any.toCompatSpecialValue(): Any = fromSdkValue()
