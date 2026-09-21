/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION")

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.functions.externals.Functions
import dev.gitlive.firebase.functions.externals.HttpsCallable
import dev.gitlive.firebase.functions.externals.HttpsCallableResult as JsHttpsCallableResult

/** The underlying Firebase JS SDK object. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.js"))
public val FirebaseFunctions.js: Functions get() = compat.js

/** The underlying Firebase JS SDK callable, created with the reference's current timeout. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.js"))
public val HttpsCallableReference.js: HttpsCallable get() = compat.js

/** The underlying Firebase JS SDK object. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.js"))
public val HttpsCallableResult.js: JsHttpsCallableResult get() = compat.js
