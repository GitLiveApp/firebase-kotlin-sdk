/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule

/** A reference to a query, see [FirebaseDataConnect.query]; its results can be cached and [subscribe]d to. */
public interface QueryRef<Data, Variables> : OperationRef<Data, Variables> {

    /** Executes the query with [FetchPolicy.PREFER_CACHE]. */
    public override suspend fun execute(): QueryResult<Data, Variables>

    /** Executes the query, taking the result from the cache or the server according to [fetchPolicy]. */
    public suspend fun execute(fetchPolicy: FetchPolicy): QueryResult<Data, Variables>

    /** Where [execute] takes a query's result from. */
    public enum class FetchPolicy {
        /** The cached result if there is one, otherwise the server's. */
        PREFER_CACHE,

        /** The cached result; fails if there is none. */
        CACHE_ONLY,

        /** The server's result. */
        SERVER_ONLY,
    }

    /** A subscription to the results of this query. */
    public fun subscribe(): QuerySubscription<Data, Variables>

    @ExperimentalFirebaseDataConnect
    override fun copy(
        operationName: String,
        variables: Variables,
        dataDeserializer: DeserializationStrategy<Data>,
        variablesSerializer: SerializationStrategy<Variables>,
        callerSdkType: FirebaseDataConnect.CallerSdkType,
        dataSerializersModule: SerializersModule?,
        variablesSerializersModule: SerializersModule?,
    ): QueryRef<Data, Variables>

    @ExperimentalFirebaseDataConnect
    override fun <NewVariables> withVariablesSerializer(
        variables: NewVariables,
        variablesSerializer: SerializationStrategy<NewVariables>,
        variablesSerializersModule: SerializersModule?,
    ): QueryRef<Data, NewVariables>

    @ExperimentalFirebaseDataConnect
    override fun <NewData> withDataDeserializer(
        dataDeserializer: DeserializationStrategy<NewData>,
        dataSerializersModule: SerializersModule?,
    ): QueryRef<NewData, Variables>
}

/** The result of executing a [QueryRef]. */
public interface QueryResult<Data, Variables> : OperationResult<Data, Variables> {
    override val ref: QueryRef<Data, Variables>

    /** Whether the data came from the cache or the server. */
    public val dataSource: DataSource
}

/** Where a [QueryResult]'s data came from. */
public enum class DataSource {
    CACHE,
    SERVER,
}
