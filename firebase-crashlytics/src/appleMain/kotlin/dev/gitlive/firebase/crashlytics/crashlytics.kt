/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.crashlytics

import cocoapods.FirebaseCrashlytics.FIRCrashlytics

/** The underlying Firebase iOS SDK object. */
@Suppress("DEPRECATION")
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.ios"))
public val FirebaseCrashlytics.ios: FIRCrashlytics get() = compat.ios
