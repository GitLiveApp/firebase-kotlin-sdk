/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

/**
 * Marks a member of the `com.google.firebase` layer that the Firebase JS SDK does not provide: on JS it does nothing
 * (or fails, where it must return a value), as its documentation says. The marker is on the JS actual only, so the
 * warning appears when code is compiled for a JS target, in common code included; opt in with
 * `@OptIn(UnsupportedOnJs::class)` where the JS behaviour is acceptable.
 */
@RequiresOptIn(
    message = "Not provided by the Firebase JS SDK: on JS this member does nothing, or fails where it must return a value (see its documentation). " +
        "Opt in with @OptIn(UnsupportedOnJs::class) if that is acceptable.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
public annotation class UnsupportedOnJs
