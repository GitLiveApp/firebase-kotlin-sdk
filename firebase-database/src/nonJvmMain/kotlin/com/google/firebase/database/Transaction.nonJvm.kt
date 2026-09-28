/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

public actual class Transaction actual constructor() {
    public actual interface Handler {
        public actual fun doTransaction(currentData: MutableData): Result
        public actual fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?)
    }

    public actual class Result internal constructor(public actual val isSuccess: Boolean, internal val resultData: MutableData?) {
        override fun toString(): String = if (isSuccess) "Transaction.Result(success)" else "Transaction.Result(abort)"
    }

    public actual companion object {
        public actual fun abort(): Result = Result(false, null)

        public actual fun success(resultData: MutableData): Result = Result(true, resultData)
    }
}
