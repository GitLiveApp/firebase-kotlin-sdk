/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore.internal

// The constants of the com.google.firebase.firestore layer, kept outside its package so that no file facade is compiled
// there (facades under com.google.firebase are header stubs verified against the Android SDK).

/** A cache size that disables garbage collection. */
internal const val CACHE_SIZE_UNLIMITED: Long = -1L

/** The smallest cache size accepted. */
internal const val MINIMUM_CACHE_BYTES: Long = 1024 * 1024

/** The default cache size. */
internal const val DEFAULT_CACHE_SIZE_BYTES: Long = 100 * 1024 * 1024

/** The default backend host. */
internal const val DEFAULT_HOST: String = "firestore.googleapis.com"

/** The default gRPC flow control window. */
internal const val DEFAULT_GRPC_FLOW_CONTROL_WINDOW: Int = 256 * 1024

/** The number of attempts of a transaction by default. */
internal const val DEFAULT_MAX_ATTEMPTS: Int = 5
