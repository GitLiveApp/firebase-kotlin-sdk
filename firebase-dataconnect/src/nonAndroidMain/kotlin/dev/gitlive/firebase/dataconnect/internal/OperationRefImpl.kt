/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.dataconnect.DataSource
import com.google.firebase.dataconnect.FirebaseDataConnect
import com.google.firebase.dataconnect.MutationRef
import com.google.firebase.dataconnect.MutationResult
import com.google.firebase.dataconnect.OperationRef
import com.google.firebase.dataconnect.OperationResult
import com.google.firebase.dataconnect.QueryRef
import com.google.firebase.dataconnect.QueryResult
import com.google.firebase.dataconnect.QuerySubscription
import com.google.firebase.dataconnect.QuerySubscriptionResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule

internal abstract class OperationRefImpl<Data, Variables>(
    override val dataConnect: FirebaseDataConnectImpl,
    override val operationName: String,
    override val variables: Variables,
    override val dataDeserializer: DeserializationStrategy<Data>,
    override val variablesSerializer: SerializationStrategy<Variables>,
    override val callerSdkType: FirebaseDataConnect.CallerSdkType,
    override val dataSerializersModule: SerializersModule?,
    override val variablesSerializersModule: SerializersModule?,
) : OperationRef<Data, Variables> {

    internal val key: QueryKey by lazy { QueryKey(operationName, encodeVariables(variables, variablesSerializer, variablesSerializersModule)) }

    internal fun decode(data: JsonElement): Data = decodeData(data, dataDeserializer, dataSerializersModule)

    /** Decodes the data the SDK returned, or maps the failure it reported, into this reference's types. */
    internal fun decodeResult(result: Result<JsonElement>): Data = result.fold(
        onSuccess = { decodeData(operationName, it, ::decode) },
        onFailure = { throw if (it is NativeOperationFailure) it.toException(operationName, ::decode) else it },
    )

    protected abstract val kind: String

    private val identity
        get() = listOf(dataConnect, operationName, variables, dataDeserializer, variablesSerializer, callerSdkType, dataSerializersModule, variablesSerializersModule)

    override fun equals(other: Any?): Boolean = other is OperationRefImpl<*, *> && other.kind == kind && other.identity == identity

    override fun hashCode(): Int = identity.hashCode()

    override fun toString(): String = "$kind(dataConnect=$dataConnect, operationName=$operationName, variables=$variables, dataDeserializer=$dataDeserializer, " +
        "variablesSerializer=$variablesSerializer, callerSdkType=$callerSdkType, dataSerializersModule=$dataSerializersModule, " +
        "variablesSerializersModule=$variablesSerializersModule)"
}

internal class QueryRefImpl<Data, Variables>(
    dataConnect: FirebaseDataConnectImpl,
    operationName: String,
    variables: Variables,
    dataDeserializer: DeserializationStrategy<Data>,
    variablesSerializer: SerializationStrategy<Variables>,
    callerSdkType: FirebaseDataConnect.CallerSdkType,
    dataSerializersModule: SerializersModule?,
    variablesSerializersModule: SerializersModule?,
) : OperationRefImpl<Data, Variables>(dataConnect, operationName, variables, dataDeserializer, variablesSerializer, callerSdkType, dataSerializersModule, variablesSerializersModule),
    QueryRef<Data, Variables> {

    override val kind: String get() = "QueryRef"

    override suspend fun execute(): QueryResult<Data, Variables> = execute(QueryRef.FetchPolicy.PREFER_CACHE)

    override suspend fun execute(fetchPolicy: QueryRef.FetchPolicy): QueryResult<Data, Variables> {
        val result = runCatching { dataConnect.executeQuery(key, fetchPolicy) }
        return QueryResultImpl(this, decodeResult(result.map { it.data }), result.getOrThrow().source)
    }

    override fun subscribe(): QuerySubscription<Data, Variables> = QuerySubscriptionImpl(this)

    override fun copy(
        operationName: String,
        variables: Variables,
        dataDeserializer: DeserializationStrategy<Data>,
        variablesSerializer: SerializationStrategy<Variables>,
        callerSdkType: FirebaseDataConnect.CallerSdkType,
        dataSerializersModule: SerializersModule?,
        variablesSerializersModule: SerializersModule?,
    ): QueryRef<Data, Variables> = QueryRefImpl(dataConnect, operationName, variables, dataDeserializer, variablesSerializer, callerSdkType, dataSerializersModule, variablesSerializersModule)

    override fun <NewVariables> withVariablesSerializer(
        variables: NewVariables,
        variablesSerializer: SerializationStrategy<NewVariables>,
        variablesSerializersModule: SerializersModule?,
    ): QueryRef<Data, NewVariables> = QueryRefImpl(dataConnect, operationName, variables, dataDeserializer, variablesSerializer, callerSdkType, dataSerializersModule, variablesSerializersModule)

    override fun <NewData> withDataDeserializer(dataDeserializer: DeserializationStrategy<NewData>, dataSerializersModule: SerializersModule?): QueryRef<NewData, Variables> = QueryRefImpl(dataConnect, operationName, variables, dataDeserializer, variablesSerializer, callerSdkType, dataSerializersModule, variablesSerializersModule)
}

internal class MutationRefImpl<Data, Variables>(
    dataConnect: FirebaseDataConnectImpl,
    operationName: String,
    variables: Variables,
    dataDeserializer: DeserializationStrategy<Data>,
    variablesSerializer: SerializationStrategy<Variables>,
    callerSdkType: FirebaseDataConnect.CallerSdkType,
    dataSerializersModule: SerializersModule?,
    variablesSerializersModule: SerializersModule?,
) : OperationRefImpl<Data, Variables>(dataConnect, operationName, variables, dataDeserializer, variablesSerializer, callerSdkType, dataSerializersModule, variablesSerializersModule),
    MutationRef<Data, Variables> {

    override val kind: String get() = "MutationRef"

    override suspend fun execute(): MutationResult<Data, Variables> = MutationResultImpl(this, decodeResult(runCatching { dataConnect.executeMutation(key) }))

    override fun copy(
        operationName: String,
        variables: Variables,
        dataDeserializer: DeserializationStrategy<Data>,
        variablesSerializer: SerializationStrategy<Variables>,
        callerSdkType: FirebaseDataConnect.CallerSdkType,
        dataSerializersModule: SerializersModule?,
        variablesSerializersModule: SerializersModule?,
    ): MutationRef<Data, Variables> = MutationRefImpl(dataConnect, operationName, variables, dataDeserializer, variablesSerializer, callerSdkType, dataSerializersModule, variablesSerializersModule)

    override fun <NewVariables> withVariablesSerializer(
        variables: NewVariables,
        variablesSerializer: SerializationStrategy<NewVariables>,
        variablesSerializersModule: SerializersModule?,
    ): MutationRef<Data, NewVariables> = MutationRefImpl(dataConnect, operationName, variables, dataDeserializer, variablesSerializer, callerSdkType, dataSerializersModule, variablesSerializersModule)

    override fun <NewData> withDataDeserializer(dataDeserializer: DeserializationStrategy<NewData>, dataSerializersModule: SerializersModule?): MutationRef<NewData, Variables> = MutationRefImpl(dataConnect, operationName, variables, dataDeserializer, variablesSerializer, callerSdkType, dataSerializersModule, variablesSerializersModule)
}

internal abstract class OperationResultImpl<Data, Variables>(override val data: Data) : OperationResult<Data, Variables> {
    protected abstract val kind: String

    override fun equals(other: Any?): Boolean = other is OperationResultImpl<*, *> && other.kind == kind && other.ref == ref && other.data == data

    override fun hashCode(): Int = listOf(ref, data).hashCode()

    override fun toString(): String = "$kind(data=$data, ref=$ref)"
}

internal class QueryResultImpl<Data, Variables>(
    override val ref: QueryRef<Data, Variables>,
    data: Data,
    override val dataSource: DataSource,
) : OperationResultImpl<Data, Variables>(data),
    QueryResult<Data, Variables> {
    override val kind: String get() = "QueryResult"
}

internal class MutationResultImpl<Data, Variables>(
    override val ref: MutationRef<Data, Variables>,
    data: Data,
) : OperationResultImpl<Data, Variables>(data),
    MutationResult<Data, Variables> {
    override val kind: String get() = "MutationResult"
}

/**
 * Emits the cached result when collected, then triggers an execution on the server and emits its result and that of
 * every later execution of an equivalent query on the same instance, as the Android SDK's subscription does.
 */
internal class QuerySubscriptionImpl<Data, Variables>(override val query: QueryRefImpl<Data, Variables>) : QuerySubscription<Data, Variables> {

    override val flow: Flow<QuerySubscriptionResult<Data, Variables>> = channelFlow {
        val dataConnect = query.dataConnect
        val key = query.key
        val collector = launch {
            dataConnect.events
                .onSubscription {
                    runCatching { dataConnect.executeQuery(key, QueryRef.FetchPolicy.CACHE_ONLY) }
                        .onSuccess { cached -> send(QuerySubscriptionResultImpl(query, runCatching { QueryResultImpl(query, query.decodeResult(Result.success(cached.data)), DataSource.CACHE) })) }
                    launch { runCatching { dataConnect.executeQuery(key, QueryRef.FetchPolicy.SERVER_ONLY) } }
                }
                .filter { it.key == key }
                .collect { event ->
                    send(QuerySubscriptionResultImpl(query, runCatching { QueryResultImpl(query, query.decodeResult(event.data), DataSource.SERVER) }))
                }
        }
        awaitClose { collector.cancel() }
    }

    override fun equals(other: Any?): Boolean = other is QuerySubscriptionImpl<*, *> && other.query == query

    override fun hashCode(): Int = query.hashCode()

    override fun toString(): String = "QuerySubscription(query=$query)"
}

internal class QuerySubscriptionResultImpl<Data, Variables>(
    override val query: QueryRef<Data, Variables>,
    override val result: Result<QueryResult<Data, Variables>>,
) : QuerySubscriptionResult<Data, Variables> {
    override fun equals(other: Any?): Boolean = other is QuerySubscriptionResultImpl<*, *> && other.query == query && other.result == result

    override fun hashCode(): Int = listOf(query, result).hashCode()

    override fun toString(): String = "QuerySubscriptionResult(query=$query, result=$result)"
}
