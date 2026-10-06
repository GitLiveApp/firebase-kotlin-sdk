/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

import dev.gitlive.firebase.perf.externals.FirebasePerformance as JsFirebasePerformance

/** The underlying Firebase JS SDK object. */
public val FirebasePerformance.js: JsFirebasePerformance get() = compat.js

/** JS-only: whether the JS SDK's automatic instrumentation is enabled. */
public fun FirebasePerformance.isInstrumentationEnabled(): Boolean = compat.js.instrumentationEnabled

/** JS-only: enables or disables the JS SDK's automatic instrumentation. */
public fun FirebasePerformance.setInstrumentationEnabled(enable: Boolean) {
    compat.js.instrumentationEnabled = enable
}
