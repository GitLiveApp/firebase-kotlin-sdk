/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.perf.externals.PerformanceTrace

/** The underlying Firebase JS SDK object. */
public val Trace.js: PerformanceTrace get() = compat.js
