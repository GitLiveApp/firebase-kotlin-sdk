/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = GlobalScope.promise { test() }.unsafeCast<Unit>()