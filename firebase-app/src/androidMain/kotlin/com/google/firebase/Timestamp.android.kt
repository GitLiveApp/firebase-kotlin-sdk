/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import dev.gitlive.firebase.stub

/*
 * Header stub for com.google.firebase.Timestamp (see buildSrc utils/HeaderStubs.kt): compiled against, verified to match
 * the real class, and deleted from the output so the real class binds at runtime.
 */

public actual class Timestamp actual constructor(seconds: Long, nanoseconds: Int) : Comparable<Timestamp> {
    public actual val seconds: Long get() = stub()
    public actual val nanoseconds: Int get() = stub()

    actual override fun compareTo(other: Timestamp): Int = stub()

    public actual companion object {
        @JvmStatic
        public actual fun now(): Timestamp = stub()
    }
}
