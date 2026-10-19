/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")
@file:JvmMultifileClass

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import com.google.firebase.firestore.firestore as compatFirestore

// The Android-SDK-shaped entry points are reached through the `Firebase.firestore` extensions (FirestoreKt), which are
// real static methods on every platform, rather than the companion object of the header stub.

/** Returns the [FirebaseFirestore] instance of the default [FirebaseApp]. */
public val Firebase.firestore: FirebaseFirestore
    get() = FirebaseFirestore(com.google.firebase.Firebase.compatFirestore)

/** Returns the [FirebaseFirestore] instance of a given [FirebaseApp], and of the named database when [databaseId] is given. */
public fun Firebase.firestore(app: FirebaseApp, databaseId: String? = null): FirebaseFirestore = FirebaseFirestore(databaseId?.let { com.google.firebase.Firebase.compatFirestore(app.compat, it) } ?: com.google.firebase.Firebase.compatFirestore(app.compat))
