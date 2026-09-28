/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.FirebaseApp
import com.google.firebase.dataconnect.ConnectorConfig
import com.google.firebase.dataconnect.DataConnectSettings
import com.google.firebase.dataconnect.FirebaseDataConnect
import com.google.firebase.dataconnect.MutationRef
import com.google.firebase.dataconnect.QueryRef
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** The instances, one per app and connector, as the Android SDK's FirebaseDataConnectFactory keeps them. */
internal object DataConnectInstances {
    private val instances = mutableMapOf<Pair<String, ConnectorConfig>, FirebaseDataConnectImpl>()

    fun get(app: FirebaseApp, config: ConnectorConfig, settings: DataConnectSettings): FirebaseDataConnect {
        val key = app.name to config
        instances[key]?.let { existing ->
            require(existing.settings == settings) {
                "The settings of the FirebaseDataConnect instance for app ${app.name} and $config are ${existing.settings}, " +
                    "which differ from the requested settings $settings; close() the instance first to change them"
            }
            return existing
        }
        return FirebaseDataConnectImpl(app, config, settings) { instances.remove(key) }.also { instances[key] = it }
    }
}

/** A query's identity for the cache and the subscriptions: the operation and its encoded variables. */
internal data class QueryKey(val operationName: String, val variables: JsonObject)

/** An execution of a query on the server, as seen by the subscriptions to equivalent queries. */
internal class QueryEvent(val key: QueryKey, val data: Result<JsonElement>)

internal class FirebaseDataConnectImpl(
    override val app: FirebaseApp,
    override val config: ConnectorConfig,
    override val settings: DataConnectSettings,
    private val onClose: () -> Unit,
) : FirebaseDataConnect {

    private var emulator: Pair<String, Int>? = null
    private var executed = false
    private var closed = false

    /** The data the server last returned for each query, kept undecoded so that any deserializer can read it. */
    internal val cache = mutableMapOf<QueryKey, JsonElement>()

    /** Every execution of a query on the server, for the subscriptions. */
    internal val events = MutableSharedFlow<QueryEvent>()

    internal val baseUrl: String
        get() = emulator?.let { (host, port) -> "http://$host:$port" } ?: "${if (settings.sslEnabled) "https" else "http"}://${settings.host}"

    override fun useEmulator(host: String, port: Int) {
        check(!executed) { "useEmulator() must be called before any operation is executed" }
        emulator = host to port
    }

    override fun <Data, Variables> query(
        operationName: String,
        variables: Variables,
        dataDeserializer: DeserializationStrategy<Data>,
        variablesSerializer: SerializationStrategy<Variables>,
        optionsBuilder: (FirebaseDataConnect.QueryRefOptionsBuilder<Data, Variables>.() -> Unit)?,
    ): QueryRef<Data, Variables> {
        val options = OptionsBuilderImpl<Data, Variables>().apply { optionsBuilder?.invoke(this) }
        return QueryRefImpl(
            dataConnect = this,
            operationName = operationName,
            variables = variables,
            dataDeserializer = dataDeserializer,
            variablesSerializer = variablesSerializer,
            callerSdkType = options.callerSdkType ?: FirebaseDataConnect.CallerSdkType.Base,
            dataSerializersModule = options.dataSerializersModule,
            variablesSerializersModule = options.variablesSerializersModule,
        )
    }

    override fun <Data, Variables> mutation(
        operationName: String,
        variables: Variables,
        dataDeserializer: DeserializationStrategy<Data>,
        variablesSerializer: SerializationStrategy<Variables>,
        optionsBuilder: (FirebaseDataConnect.MutationRefOptionsBuilder<Data, Variables>.() -> Unit)?,
    ): MutationRef<Data, Variables> {
        val options = OptionsBuilderImpl<Data, Variables>().apply { optionsBuilder?.invoke(this) }
        return MutationRefImpl(
            dataConnect = this,
            operationName = operationName,
            variables = variables,
            dataDeserializer = dataDeserializer,
            variablesSerializer = variablesSerializer,
            callerSdkType = options.callerSdkType ?: FirebaseDataConnect.CallerSdkType.Base,
            dataSerializersModule = options.dataSerializersModule,
            variablesSerializersModule = options.variablesSerializersModule,
        )
    }

    /** Runs an operation on the server; queries update the cache and notify their subscriptions. */
    internal suspend fun execute(key: QueryKey, mutation: Boolean): OperationResponse {
        check(!closed) { "FirebaseDataConnect instance has been closed" }
        executed = true
        val response = runCatching { executeOnServer(key.operationName, key.variables, mutation) }
        if (!mutation) {
            val data = response.mapCatching { it.data?.takeIf { _ -> it.errors.isEmpty() } ?: throw it.toException(key.operationName) }
            data.onSuccess { cache[key] = it }
            events.emit(QueryEvent(key, data))
        }
        return response.getOrThrow()
    }

    override fun close() {
        closed = true
        cache.clear()
        onClose()
    }

    override suspend fun suspendingClose() {
        close()
    }

    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = listOf(app.name, config).hashCode()

    override fun toString(): String = "FirebaseDataConnect(app=${app.name}, config=$config, settings=$settings)"

    private class OptionsBuilderImpl<Data, Variables> :
        FirebaseDataConnect.QueryRefOptionsBuilder<Data, Variables>,
        FirebaseDataConnect.MutationRefOptionsBuilder<Data, Variables> {
        override var callerSdkType: FirebaseDataConnect.CallerSdkType? = null
        override var variablesSerializersModule: kotlinx.serialization.modules.SerializersModule? = null
        override var dataSerializersModule: kotlinx.serialization.modules.SerializersModule? = null
    }
}

/** The exception a response with errors, or without data, decodes to (used for the cache and subscriptions, which keep the raw JSON). */
private fun OperationResponse.toException(operationName: String): Throwable = runCatching { decodeOrThrow(operationName) { it } }.exceptionOrNull() ?: IllegalStateException("operation $operationName returned no data")
