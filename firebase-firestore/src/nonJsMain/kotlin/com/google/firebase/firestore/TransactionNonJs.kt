/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

/**
 * Reads [documentRef] inside the transaction, as the Android SDK's `Transaction.get`. Only available where the SDK reads
 * synchronously (Android, the JVM and Apple platforms); the JS SDK reads asynchronously, so code that also targets JS
 * uses the suspending `dev.gitlive.firebase.firestore.Transaction.get` instead.
 */
public expect fun Transaction.get(documentRef: DocumentReference): DocumentSnapshot
