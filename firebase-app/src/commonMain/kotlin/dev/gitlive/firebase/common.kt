/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// Kept in its own file so the JVM facade stays CommonKt, as published before the com.google.firebase layer.
@file:JvmMultifileClass
@file:JvmName("CommonKt")

package dev.gitlive.firebase

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import com.google.firebase.options as compatOptions

/** Returns options of default FirebaseApp */
@Suppress("UnusedReceiverParameter", "DEPRECATION")
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.options", "com.google.firebase.options"))
public val Firebase.options: FirebaseOptions
    get() = com.google.firebase.Firebase.compatOptions.toPublic()
