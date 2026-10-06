/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import android.util.Log
import kotlin.reflect.KClass

internal actual fun javaClassNameOf(type: KClass<*>): String = type.java.name

internal actual fun logWarning(tag: String, message: String) {
    Log.w(tag, message)
}
