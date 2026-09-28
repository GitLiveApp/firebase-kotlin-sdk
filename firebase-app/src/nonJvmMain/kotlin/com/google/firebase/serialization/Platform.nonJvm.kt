/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlin.reflect.KClass

// The collections the Firebase SDKs return on Android.
internal actual fun javaClassNameOf(type: KClass<*>): String = when (type) {
    HashMap::class, LinkedHashMap::class -> "java.util.HashMap"
    ArrayList::class -> "java.util.ArrayList"
    else -> type.simpleName ?: "<anonymous>"
}

internal actual fun logWarning(tag: String, message: String) {
    println("W/$tag: $message")
}
