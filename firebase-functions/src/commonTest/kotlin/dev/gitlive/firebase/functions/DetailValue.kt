/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

// In its own file: a top-level function in functions.kt would give the test APK a FunctionsKt facade that the module's
// own functions.kt facade shadows on Android.

/** [details] is a `Map` on every platform; its value for [key]. */
fun detailValue(details: Any?, key: String): Any? = (details as Map<*, *>)[key]
