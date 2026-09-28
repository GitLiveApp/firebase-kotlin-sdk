/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

/**
 * Marks a Firebase Data Connect declaration as experimental, as the Firebase Android SDK does: its signature and/or
 * semantics may change in backwards-incompatible ways at any time without notice, up to and including complete removal.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This declaration is \"experimental\": its signature and/or semantics " +
        "may change in backwards-incompatible ways at any time without notice, " +
        "up to and including complete removal. " +
        "If you have a use case that relies on this declaration please open a " +
        "\"feature request\" issue at https://github.com/firebase/firebase-android-sdk " +
        "requesting this declaration's promotion from \"experimental\" to \"fully-supported\".",
)
public annotation class ExperimentalFirebaseDataConnect
