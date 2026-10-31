/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions;

import java.util.concurrent.TimeUnit;

/**
 * The TimeUnit members of the Firebase Android SDK that the shipped HttpsCallableTimeoutKt facade calls. javac compiles
 * after the header stubs are stripped, so these calls bind to the real class without going through the error-deprecated
 * stubs. Shipped (see keepClasses in the build file), package-private.
 */
final class FunctionsJvmApi {
    private FunctionsJvmApi() {}

    static void setTimeout(HttpsCallableReference reference, long millis) {
        reference.setTimeout(millis, TimeUnit.MILLISECONDS);
    }

    static HttpsCallableReference withTimeout(HttpsCallableReference reference, long millis) {
        return reference.withTimeout(millis, TimeUnit.MILLISECONDS);
    }
}
