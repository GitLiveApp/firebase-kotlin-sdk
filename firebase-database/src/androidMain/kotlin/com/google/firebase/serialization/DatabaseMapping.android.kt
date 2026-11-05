/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import com.google.firebase.database.databaseFormat

// The Android SDK (and firebase-java-sdk) would map a class by reflection, so the SDK member receives the mapped data itself.

internal actual fun Any?.toSdkData(): Any? = this

internal actual fun Map<String, Any?>.toPlain(): Map<String, Any?> = mapValues { (_, value) -> databaseFormat.convertToPlainValue(value) }
