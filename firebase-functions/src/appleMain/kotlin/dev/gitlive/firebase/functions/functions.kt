/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION")

package dev.gitlive.firebase.functions

import cocoapods.FirebaseFunctions.FIRFunctions
import cocoapods.FirebaseFunctions.FIRHTTPSCallable
import cocoapods.FirebaseFunctions.FIRHTTPSCallableResult

/** The underlying Firebase iOS SDK object. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.ios"))
public val FirebaseFunctions.ios: FIRFunctions get() = compat.ios

/** The underlying Firebase iOS SDK object. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.ios"))
public val HttpsCallableReference.ios: FIRHTTPSCallable get() = compat.ios

/** The underlying Firebase iOS SDK object. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.ios"))
public val HttpsCallableResult.ios: FIRHTTPSCallableResult get() = compat.ios
