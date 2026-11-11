/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import android.util.Log
import kotlin.reflect.KClass

// Class.toString() is "class <name>" or "interface <name>" (a primitive is just its name): the Java name without calling
// Class.getName(), which this module's compilation cannot see because common code declares an error-deprecated java.lang.Class
// stand-in that shadows the JDK's class here (and a KClass.java stand-in next to the standard library's, hence javaObjectType).
// The runtime object is the JDK's, so toString() dispatches to it.
internal actual fun javaClassNameOf(type: KClass<*>): String = type.javaObjectType.toString().substringAfter(' ')

internal actual fun logWarning(tag: String, message: String) {
    Log.w(tag, message)
}

// The Android SDK and firebase-java-sdk exchange Kotlin values.
internal actual fun platformLeaf(value: Any): Any? = value

internal actual fun toPlatformLeaf(value: Any?): Any? = value
