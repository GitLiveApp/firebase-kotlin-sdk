/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

import swiftPMImport.dev.gitlive.firebase.messaging.FIRMessaging

/** The underlying Firebase iOS SDK object. */
public val FirebaseMessaging.ios: FIRMessaging get() = compat.ios
