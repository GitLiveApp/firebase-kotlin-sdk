/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

/** The base class of the exceptions thrown by Firebase Data Connect. */
public open class DataConnectException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Thrown when the execution of an operation fails: the server reported errors for it, or the data it returned could not
 * be decoded. [response] carries the errors, the raw data and the decoded data when decoding succeeded.
 */
public open class DataConnectOperationException(
    message: String,
    cause: Throwable? = null,
    public val response: DataConnectOperationFailureResponse<*>,
) : DataConnectException(message, cause)

/** The details of a failed operation, see [DataConnectOperationException.response]. */
public interface DataConnectOperationFailureResponse<Data> {
    /** The data returned by the server, as plain values, or `null` if none was returned. */
    public val rawData: Map<String, Any?>?

    /** The errors reported by the server; possibly empty when the failure was a decoding failure. */
    public val errors: List<ErrorInfo>

    /** The decoded data, or `null` if the server returned none or it could not be decoded. */
    public val data: Data?

    override fun toString(): String

    /** One error reported by the server. */
    public interface ErrorInfo {
        /** The error message. */
        public val message: String

        /** The path, into the data, of the field the error relates to; empty when the error is not about a field. */
        public val path: List<DataConnectPathSegment>

        override fun equals(other: Any?): Boolean

        override fun hashCode(): Int

        override fun toString(): String
    }
}
