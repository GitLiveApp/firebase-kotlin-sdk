/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlin.reflect.KClass

/** The class's name as `Class.getName()` returns it on Android, for messages; elsewhere the closest name available. */
internal expect fun javaClassNameOf(type: KClass<*>): String

/** Logs a warning as the Android SDK's mappers do, with `android.util.Log.w` on Android. */
internal expect fun logWarning(tag: String, message: String)
