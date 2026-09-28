/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/*
 * The static members of the layer that this module's own (shipped) code uses. Common code calling the companion members
 * would bind to a Companion object that the real Android SDK classes do not have, so these go through a Java helper on
 * Android and the JVM (DatabaseInternals.android.kt) and call the companions directly elsewhere.
 */

internal expect fun serverTimestamp(): Map<String, String>

internal expect fun serverIncrement(delta: Double): Any

internal expect fun transactionSuccess(resultData: MutableData): Transaction.Result

internal expect fun transactionAbort(): Transaction.Result
