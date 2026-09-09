/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// Kept in its own file so the JVM facade stays CommonKt, as published before the com.google.firebase layer.
@file:JvmMultifileClass
@file:JvmName("CommonKt")

package dev.gitlive.firebase

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/** Returns options of default FirebaseApp */
@Suppress("UnusedReceiverParameter")
public val Firebase.options: FirebaseOptions
    get() = Firebase.app.options
