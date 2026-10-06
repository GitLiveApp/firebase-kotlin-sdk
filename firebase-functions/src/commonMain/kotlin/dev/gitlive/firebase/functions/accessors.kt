/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("AndroidFunctions")
@file:JvmMultifileClass
@file:Suppress("DEPRECATION")

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import com.google.firebase.functions.functions as compatFunctions

// The Android-SDK-shaped entry points are reached through the `Firebase.functions` extensions (FunctionsKt), which are
// real static methods on every platform, rather than the companion object of the header stub.

/** Returns the [FirebaseFunctions] instance of the default [FirebaseApp]. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.functions", "com.google.firebase.functions.functions"))
public val Firebase.functions: FirebaseFunctions
    get() = FirebaseFunctions(com.google.firebase.Firebase.compatFunctions)

/** Returns the [FirebaseFunctions] instance of a given [region]. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.functions(region)", "com.google.firebase.functions.functions"))
public fun Firebase.functions(region: String): FirebaseFunctions = FirebaseFunctions(com.google.firebase.Firebase.compatFunctions(region))

/** Returns the [FirebaseFunctions] instance of a given [FirebaseApp]. */
public fun Firebase.functions(app: FirebaseApp): FirebaseFunctions = FirebaseFunctions(com.google.firebase.Firebase.compatFunctions(app.compat))

/** Returns the [FirebaseFunctions] instance of a given [FirebaseApp] and [region]. */
public fun Firebase.functions(app: FirebaseApp, region: String): FirebaseFunctions = FirebaseFunctions(com.google.firebase.Firebase.compatFunctions(app.compat, region))
