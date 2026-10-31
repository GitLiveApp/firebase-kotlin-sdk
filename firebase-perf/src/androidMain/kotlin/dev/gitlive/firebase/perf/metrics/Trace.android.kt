/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("TraceKt")
@file:JvmMultifileClass

package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.perf.session.PerfSession

/** The underlying Firebase Android SDK object. */
public val Trace.android: com.google.firebase.perf.metrics.Trace get() = compat

/** Android-only: associates this trace with a [PerfSession]. */
public fun Trace.updateSession(session: PerfSession) {
    compat.updateSession(session.android)
}
