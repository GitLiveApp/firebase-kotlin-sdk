/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.installations.internal

/*
 * Plain common declarations: they are identical on every platform, so no expect/actual is needed. On Android/JVM they
 * are still compiled, verified against firebase-installations-interop and stripped like the header stubs (see buildSrc
 * utils/HeaderStubs.kt).
 */

/** Receives the new installation id whenever it changes, mirroring `com.google.firebase.installations.internal.FidListener`. */
public fun interface FidListener {
    /** @param fid the newly generated installation id. */
    public fun onFidChanged(fid: String)
}

/** Unregisters a [FidListener], mirroring `com.google.firebase.installations.internal.FidListenerHandle`. */
public interface FidListenerHandle {
    /** Unregisters the listener; subsequent calls have no effect. */
    public fun unregister()
}
