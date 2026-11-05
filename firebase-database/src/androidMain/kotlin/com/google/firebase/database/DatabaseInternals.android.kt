/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("DatabaseInternalsKt")

package com.google.firebase.database

// Shipped (see keepClasses in the build file): the SDK's static members this module's own code needs are reached through
// DatabaseStatics.java, which javac compiles after the header stubs are stripped, so it binds to the real classes.

internal actual fun serverTimestamp(): Map<String, String> = DatabaseStatics.timestamp()

internal actual fun serverIncrement(delta: Double): Any = DatabaseStatics.increment(delta)

internal actual fun transactionSuccess(resultData: MutableData): Transaction.Result = DatabaseStatics.success(resultData)

internal actual fun transactionAbort(): Transaction.Result = DatabaseStatics.abort()
