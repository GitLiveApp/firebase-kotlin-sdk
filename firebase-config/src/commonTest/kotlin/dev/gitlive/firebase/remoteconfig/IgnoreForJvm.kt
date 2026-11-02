/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.remoteconfig

/**
 * firebase-java-sdk does not support real-time config updates: registering a `ConfigUpdateListener` needs
 * `androidx.concurrent.futures`, which it does not ship, so the real-time update test is skipped on the JVM.
 */
expect annotation class IgnoreForJvm()
