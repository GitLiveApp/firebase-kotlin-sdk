/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import kotlin.jvm.JvmInline

/** One segment of the path of a field in the data of an operation, see [DataConnectOperationFailureResponse.ErrorInfo.path]. */
public sealed interface DataConnectPathSegment {

    /** A named field. */
    @JvmInline
    public value class Field(public val field: String) : DataConnectPathSegment {
        override fun toString(): String = field
    }

    /** An index into a list. */
    @JvmInline
    public value class ListIndex(public val index: Int) : DataConnectPathSegment {
        override fun toString(): String = index.toString()
    }
}
