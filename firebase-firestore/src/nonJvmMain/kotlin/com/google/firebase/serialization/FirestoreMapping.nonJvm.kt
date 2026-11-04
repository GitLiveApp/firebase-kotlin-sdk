/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import com.google.firebase.firestore.firestoreFormat

// The Apple, JS and wasmJs actuals map every value they write through the format (see toIos/toJs), which passes a PlainValue through.

internal actual fun Any.toSdkData(): Any = PlainValue(this)

internal actual fun Map<String, Any?>.toPlain(): Map<String, Any?> = mapValues { (_, value) -> PlainValue(firestoreFormat.convertToPlainValue(value)) }
