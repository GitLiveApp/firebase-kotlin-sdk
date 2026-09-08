/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.installations.internal

/** Receives the new installation id whenever it changes, mirroring `com.google.firebase.installations.internal.FidListener`. */
public expect fun interface FidListener {
    /** @param fid the newly generated installation id. */
    public fun onFidChanged(fid: String)
}

/** Unregisters a [FidListener], mirroring `com.google.firebase.installations.internal.FidListenerHandle`. */
public expect interface FidListenerHandle {
    /** Unregisters the listener; subsequent calls have no effect. */
    public fun unregister()
}
