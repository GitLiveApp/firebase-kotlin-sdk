/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

/** The failure of sending an upstream message, as the Android SDK's `SendException`; [errorCode] is one of the `ERROR_*` constants. */
public expect class SendException : Exception {
    /** One of the `ERROR_*` constants. */
    public val errorCode: Int

    public companion object {
        /** The message parameters were invalid. */
        public val ERROR_INVALID_PARAMETERS: Int

        /** The message was too large. */
        public val ERROR_SIZE: Int

        /** Too many messages were sent. */
        public val ERROR_TOO_MANY_MESSAGES: Int

        /** The message's time to live was exceeded. */
        public val ERROR_TTL_EXCEEDED: Int

        /** An unknown error. */
        public val ERROR_UNKNOWN: Int
    }
}
