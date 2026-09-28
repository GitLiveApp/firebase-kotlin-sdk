/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfig

/** The underlying Firebase iOS SDK object. */
public val FirebaseRemoteConfig.ios: FIRRemoteConfig get() = compat.ios
