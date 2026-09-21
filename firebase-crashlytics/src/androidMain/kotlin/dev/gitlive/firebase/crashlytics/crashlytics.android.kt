/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("CrashlyticsKt")
@file:JvmMultifileClass

package dev.gitlive.firebase.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics as AndroidFirebaseCrashlytics

/** The underlying Firebase Android SDK object. */
@Suppress("DEPRECATION")
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat"))
public val FirebaseCrashlytics.android: AndroidFirebaseCrashlytics get() = compat
