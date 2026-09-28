/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import kotlinx.coroutines.flow.Flow

/**
 * A subscription to the results of a [query]: [flow] emits the cached result, if any, when collected, then the result of
 * every execution of an equivalent query (same operation and variables) on the same [FirebaseDataConnect] instance.
 */
public interface QuerySubscription<Data, Variables> {
    /** The subscribed query. */
    public val query: QueryRef<Data, Variables>

    /** The results; collecting it also executes the query, so that a fresh result follows the cached one. */
    public val flow: Flow<QuerySubscriptionResult<Data, Variables>>

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}

/** One emission of [QuerySubscription.flow]: the [result] of an execution, successful or not. */
public interface QuerySubscriptionResult<Data, Variables> {
    /** The subscribed query. */
    public val query: QueryRef<Data, Variables>

    /** The result of the execution, or its failure. */
    public val result: Result<QueryResult<Data, Variables>>

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}
