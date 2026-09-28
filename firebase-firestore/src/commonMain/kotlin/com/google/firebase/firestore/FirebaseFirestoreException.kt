/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.FirebaseException
import kotlin.jvm.JvmStatic

/**
 * A failed Firestore operation, mirroring `com.google.firebase.firestore.FirebaseFirestoreException` from the Firebase
 * Android SDK; [code] is the gRPC-style [Code] of the failure.
 */
public class FirebaseFirestoreException : FirebaseException {
    /** The gRPC-style code of the failure. */
    public val code: Code

    public constructor(message: String, code: Code) : super(message) {
        this.code = code
    }

    public constructor(message: String, code: Code, cause: Throwable) : super(message, cause) {
        this.code = code
    }

    /** The error codes of Firestore, with the numeric [value] of the gRPC status they correspond to. */
    public enum class Code(private val value: Int) {
        OK(0),
        CANCELLED(1),
        UNKNOWN(2),
        INVALID_ARGUMENT(3),
        DEADLINE_EXCEEDED(4),
        NOT_FOUND(5),
        ALREADY_EXISTS(6),
        PERMISSION_DENIED(7),
        RESOURCE_EXHAUSTED(8),
        FAILED_PRECONDITION(9),
        ABORTED(10),
        OUT_OF_RANGE(11),
        UNIMPLEMENTED(12),
        INTERNAL(13),
        UNAVAILABLE(14),
        DATA_LOSS(15),
        UNAUTHENTICATED(16),
        ;

        /** The numeric gRPC status of this code. */
        public fun value(): Int = value

        public companion object {
            /** The [Code] of the gRPC status [value], or [UNKNOWN]. */
            @JvmStatic
            public fun fromValue(value: Int): Code = entries.firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}
