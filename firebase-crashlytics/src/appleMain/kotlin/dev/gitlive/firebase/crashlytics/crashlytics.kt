/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION")

package dev.gitlive.firebase.crashlytics

import cocoapods.FirebaseCrashlytics.FIRCrashlytics

/** The underlying Firebase iOS SDK object. */
public val FirebaseCrashlytics.ios: FIRCrashlytics get() = compat.ios
