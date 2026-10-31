/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf;

import com.google.firebase.FirebaseApp;

/**
 * The Android SDK's per-app {@link FirebasePerformance} for the shipped Kotlin code of this module: javac runs after the
 * header stubs are stripped, so this binds to the real classes.
 */
final class PerformanceStatics {
    private PerformanceStatics() {}

    static FirebasePerformance getInstance(FirebaseApp app) {
        return app.get(FirebasePerformance.class);
    }
}
