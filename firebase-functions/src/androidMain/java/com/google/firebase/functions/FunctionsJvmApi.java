/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * The members of the Firebase Android SDK with JVM-only parameter types that the shipped FunctionsUrlKt and
 * HttpsCallableTimeoutKt facades call. javac compiles after the header stubs are stripped, so these calls bind to the
 * real classes without the stubs having to declare the members (which would make them part of the compatibility report
 * although common code cannot call them). Shipped (see keepClasses in the build file), package-private.
 */
final class FunctionsJvmApi {
    private FunctionsJvmApi() {}

    static HttpsCallableReference getHttpsCallableFromUrl(FirebaseFunctions functions, String url) {
        return functions.getHttpsCallableFromUrl(toUrl(url));
    }

    static HttpsCallableReference getHttpsCallableFromUrl(FirebaseFunctions functions, String url, HttpsCallableOptions options) {
        return functions.getHttpsCallableFromUrl(toUrl(url), options);
    }

    static void setTimeout(HttpsCallableReference reference, long millis) {
        reference.setTimeout(millis, TimeUnit.MILLISECONDS);
    }

    static HttpsCallableReference withTimeout(HttpsCallableReference reference, long millis) {
        return reference.withTimeout(millis, TimeUnit.MILLISECONDS);
    }

    private static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Not a valid URL: " + url, e);
        }
    }
}
