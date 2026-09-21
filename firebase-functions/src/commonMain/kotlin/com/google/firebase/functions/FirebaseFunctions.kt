/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import com.google.firebase.FirebaseApp

/**
 * The entry point for Cloud Functions for Firebase, mirroring `com.google.firebase.functions.FirebaseFunctions` from the
 * Firebase Android SDK: creates references to callable HTTPS functions.
 *
 * Not mirrored: `useFunctionsEmulator` (deprecated in the Android SDK, use [useEmulator]).
 */
public expect class FirebaseFunctions {
    /** Returns a reference to the callable HTTPS trigger with the given [name]. */
    public fun getHttpsCallable(name: String): HttpsCallableReference

    /** Returns a reference to the callable HTTPS trigger with the given [name], called with [options]. */
    public fun getHttpsCallable(name: String, options: HttpsCallableOptions): HttpsCallableReference

    /**
     * Modifies this instance to communicate with the Cloud Functions emulator at [host]:[port]. Call it before using the
     * instance for any function call.
     */
    public fun useEmulator(host: String, port: Int)

    public companion object {
        /** The [FirebaseFunctions] instance of the default [FirebaseApp], for the default region `us-central1`. */
        public fun getInstance(): FirebaseFunctions

        /** The [FirebaseFunctions] instance of [app], for the default region `us-central1`. */
        public fun getInstance(app: FirebaseApp): FirebaseFunctions

        /**
         * The [FirebaseFunctions] instance of [app] for [regionOrCustomDomain]: a region such as `europe-west1`, or a
         * custom domain such as `https://mydomain.com` (anything starting with `http://` or `https://`).
         */
        public fun getInstance(app: FirebaseApp, regionOrCustomDomain: String): FirebaseFunctions

        /** The [FirebaseFunctions] instance of the default [FirebaseApp] for [regionOrCustomDomain]; see above. */
        public fun getInstance(regionOrCustomDomain: String): FirebaseFunctions
    }
}
