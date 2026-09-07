/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import java.util.List;

/**
 * Static members of the Firebase Android SDK that this module calls itself. Kotlin would call them through the
 * header stub's Companion object (which does not exist at runtime); javac emits a plain invokestatic that binds to
 * the real class.
 */
final class FirebaseAppStatics {
    private FirebaseAppStatics() {}

    static List<FirebaseApp> getApps(Context context) {
        return FirebaseApp.getApps(context);
    }
}
