/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("databaseAndroid")
@file:JvmMultifileClass

package dev.gitlive.firebase.database

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import com.google.firebase.database.database as compatDatabase

// The Android-SDK-shaped entry points are reached through the `Firebase.database` extensions (DatabaseKt), which are
// real static methods on every platform, rather than the companion object of the header stub.

/** Returns the [FirebaseDatabase] instance of the default [FirebaseApp]. */
public val Firebase.database: FirebaseDatabase
    get() = FirebaseDatabase(com.google.firebase.Firebase.compatDatabase)

/** Returns the [FirebaseDatabase] instance for the specified [url]. */
public fun Firebase.database(url: String): FirebaseDatabase = FirebaseDatabase(com.google.firebase.Firebase.compatDatabase(url))

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp]. */
public fun Firebase.database(app: FirebaseApp): FirebaseDatabase = FirebaseDatabase(com.google.firebase.Firebase.compatDatabase(app.compat))

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp] and [url]. */
public fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase = FirebaseDatabase(com.google.firebase.Firebase.compatDatabase(app.compat, url))
