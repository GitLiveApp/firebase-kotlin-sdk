/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

/**
 * firebase-java-sdk registers no Remote Config component, so `FirebaseRemoteConfig.getInstance()` fails on the JVM
 * (the JVM is not a test target of this module); the tests that need an instance are skipped there.
 */
expect annotation class IgnoreForJvm()
