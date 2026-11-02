/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.remoteconfig

import com.google.firebase.FirebaseException

public actual open class FirebaseRemoteConfigException : FirebaseException {
    public actual val code: Code

    public actual constructor(message: String) : this(message, null, Code.UNKNOWN)

    public actual constructor(message: String, code: Code) : this(message, null, code)

    public actual constructor(message: String, cause: Throwable?) : this(message, cause, Code.UNKNOWN)

    public actual constructor(message: String, cause: Throwable?, code: Code) : super(message, cause) {
        this.code = code
    }

    public actual enum class Code(private val value: Int) {
        UNKNOWN(0),
        CONFIG_UPDATE_STREAM_ERROR(1),
        CONFIG_UPDATE_MESSAGE_INVALID(2),
        CONFIG_UPDATE_NOT_FETCHED(3),
        CONFIG_UPDATE_UNAVAILABLE(4),
        ;

        public actual fun value(): Int = value
    }
}

public actual open class FirebaseRemoteConfigClientException : FirebaseRemoteConfigException {
    public actual constructor(message: String) : super(message)

    public actual constructor(message: String, code: Code) : super(message, code)

    public actual constructor(message: String, cause: Throwable?) : super(message, cause)

    public actual constructor(message: String, cause: Throwable?, code: Code) : super(message, cause, code)
}

public actual open class FirebaseRemoteConfigServerException : FirebaseRemoteConfigException {
    public actual val httpStatusCode: Int

    public actual constructor(httpStatusCode: Int, message: String) : this(httpStatusCode, message, null, Code.UNKNOWN)

    public actual constructor(httpStatusCode: Int, message: String, code: Code) : this(httpStatusCode, message, null, code)

    public actual constructor(httpStatusCode: Int, message: String, cause: Throwable?) : this(httpStatusCode, message, cause, Code.UNKNOWN)

    public actual constructor(message: String, code: Code) : this(-1, message, null, code)

    public actual constructor(message: String, cause: Throwable?, code: Code) : this(-1, message, cause, code)

    public actual constructor(httpStatusCode: Int, message: String, cause: Throwable?, code: Code) : super(message, cause, code) {
        this.httpStatusCode = httpStatusCode
    }
}

public actual open class FirebaseRemoteConfigFetchThrottledException actual constructor(
    public actual val throttleEndTimeMillis: Long,
) : FirebaseRemoteConfigException("Fetch was throttled.")
