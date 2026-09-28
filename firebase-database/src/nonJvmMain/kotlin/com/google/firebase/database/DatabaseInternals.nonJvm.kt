/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

internal actual fun serverTimestamp(): Map<String, String> = ServerValue.TIMESTAMP

internal actual fun serverIncrement(delta: Double): Any = ServerValue.increment(delta)

internal actual fun transactionSuccess(resultData: MutableData): Transaction.Result = Transaction.success(resultData)

internal actual fun transactionAbort(): Transaction.Result = Transaction.abort()
