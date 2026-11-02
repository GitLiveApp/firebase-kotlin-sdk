/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import com.google.firebase.functions.HttpsCallableResult
import platform.Foundation.NSNull

internal actual val HttpsCallableResult.platformData: Any? get() = ios.data().takeUnless { it is NSNull }
