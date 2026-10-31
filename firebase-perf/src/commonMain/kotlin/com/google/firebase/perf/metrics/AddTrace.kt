/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf.metrics

/**
 * Marks a method to be traced by the Firebase Performance Gradle plugin on Android, as the Android SDK's
 * `com.google.firebase.perf.metrics.AddTrace`. It is a plain annotation on the other platforms, where nothing reads it.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
public annotation class AddTrace(val name: String, val enabled: Boolean = true)
