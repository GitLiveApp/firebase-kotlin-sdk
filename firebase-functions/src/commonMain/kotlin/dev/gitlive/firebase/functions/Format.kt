/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import com.google.firebase.serialization.FirebaseFormat

/**
 * The format through which the platform actuals exchange call data and results with the platform SDK, with the
 * Android SDK's rules for call data (see [FirebaseFormat.Functions]).
 */
internal val functionsFormat: FirebaseFormat.Functions = FirebaseFormat.Functions()
