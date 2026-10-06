/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

import cocoapods.FirebasePerformance.FIRPerformance

/** The underlying Firebase iOS SDK object. */
public val FirebasePerformance.ios: FIRPerformance get() = compat.ios
