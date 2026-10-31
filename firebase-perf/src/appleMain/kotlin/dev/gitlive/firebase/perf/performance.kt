/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

import swiftPMImport.dev.gitlive.firebase.perf.FIRPerformance

/** The underlying Firebase iOS SDK object. */
public val FirebasePerformance.ios: FIRPerformance get() = compat.ios
