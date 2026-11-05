/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

/** Base class for all Firebase exceptions, mirroring `com.google.firebase.FirebaseException`. */
public expect open class FirebaseException : Exception {
    public constructor(message: String)
    public constructor(message: String, cause: Throwable)
}

/** Exception thrown when a request to a Firebase service fails due to a network error. */
public expect class FirebaseNetworkException(message: String) : FirebaseException

/** Exception thrown when a request to a Firebase service has been blocked due to having received too many requests. */
public expect open class FirebaseTooManyRequestsException(message: String) : FirebaseException

/** Exception thrown when a Firebase API is not available on the current platform or configuration. */
public expect open class FirebaseApiNotAvailableException(message: String) : FirebaseException
