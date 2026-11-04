/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database;

import java.util.Map;

/**
 * The Android SDK's static members for the shipped Kotlin code of this module: Kotlin reaches the stubs' companion
 * members through a {@code Companion} object the real classes do not have, while javac runs after the header stubs are
 * stripped, so this binds to the real classes.
 */
final class DatabaseStatics {
    private DatabaseStatics() {}

    static Map<String, String> timestamp() {
        return ServerValue.TIMESTAMP;
    }

    static Object increment(double delta) {
        return ServerValue.increment(delta);
    }

    static Transaction.Result success(MutableData resultData) {
        return Transaction.success(resultData);
    }

    static Transaction.Result abort() {
        return Transaction.abort();
    }
}
