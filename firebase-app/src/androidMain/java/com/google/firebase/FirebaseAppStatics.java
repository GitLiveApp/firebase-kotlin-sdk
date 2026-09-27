/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase;

import android.content.Context;
import java.util.List;

/**
 * Static members of the Firebase Android SDK that this module calls itself. Kotlin would call them through the
 * header stub's Companion object (which does not exist at runtime); javac emits a plain invokestatic that binds to
 * the real class. Shipped (see keepClasses in the build file), package-private.
 */
final class FirebaseAppStatics {
    private FirebaseAppStatics() {}

    static List<FirebaseApp> getApps(Context context) {
        return FirebaseApp.getApps(context);
    }

    static FirebaseOptions fromResource(Context context) {
        return FirebaseOptions.fromResource(context);
    }
}
