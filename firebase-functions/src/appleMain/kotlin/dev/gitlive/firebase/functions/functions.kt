/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import swiftPMImport.dev.gitlive.firebase.functions.FIRFunctions
import swiftPMImport.dev.gitlive.firebase.functions.FIRHTTPSCallable
import swiftPMImport.dev.gitlive.firebase.functions.FIRHTTPSCallableResult

/** The underlying Firebase iOS SDK object. */
public val FirebaseFunctions.ios: FIRFunctions get() = compat.ios

/** The underlying Firebase iOS SDK object. */
public val HttpsCallableReference.ios: FIRHTTPSCallable get() = compat.ios

/** The underlying Firebase iOS SDK object. */
public val HttpsCallableResult.ios: FIRHTTPSCallableResult get() = compat.ios
