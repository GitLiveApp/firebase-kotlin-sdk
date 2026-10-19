/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("AndroidFunctions")
@file:JvmMultifileClass
@file:Suppress("DEPRECATION")

package dev.gitlive.firebase.functions

/** The underlying Firebase Android SDK object. */
public val FirebaseFunctions.android: com.google.firebase.functions.FirebaseFunctions get() = compat

/** The underlying Firebase Android SDK object. */
public val HttpsCallableReference.android: com.google.firebase.functions.HttpsCallableReference get() = compat

/** The underlying Firebase Android SDK object. */
public val HttpsCallableResult.android: com.google.firebase.functions.HttpsCallableResult get() = compat
