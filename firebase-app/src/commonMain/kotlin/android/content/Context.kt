/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package android.content

/**
 * Stands in for `android.content.Context` so that the Firebase Android SDK members taking or returning one
 * (`FirebaseApp.initializeApp`, `FirebaseApp.getApps`, `FirebaseApp.getApplicationContext`,
 * `FirebaseOptions.fromResource` and `Firebase.initialize`) keep their exact signatures in common code, which an
 * Android app's common code passes its context through.
 *
 * On Android and the JVM the class is a header stub that is stripped from the output (see buildSrc
 * utils/HeaderStubs.kt), so code there uses the platform's own: Android's, or firebase-java-sdk's on the JVM. Apple
 * platforms, JS and wasmJs have no context, so this placeholder is shipped there: platform code creates one with
 * `Context()` to pass where the SDK takes a `Context`, and the SDK ignores it.
 */
public open class Context
