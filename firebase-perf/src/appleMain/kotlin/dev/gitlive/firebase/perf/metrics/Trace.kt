/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf.metrics

import cocoapods.FirebasePerformance.FIRTrace

/** The underlying Firebase iOS SDK object; null when the iOS SDK refused the trace name. */
public val Trace.ios: FIRTrace? get() = compat.ios
