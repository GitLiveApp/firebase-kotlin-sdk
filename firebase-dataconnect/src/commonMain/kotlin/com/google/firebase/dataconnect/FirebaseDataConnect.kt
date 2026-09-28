/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule

/**
 * The entry point of Firebase Data Connect: creates [QueryRef]s and [MutationRef]s for the operations of the connector
 * [config] of [app]. Obtained with [FirebaseDataConnect.Companion.getInstance]; one instance exists per app and connector
 * until it is [close]d.
 *
 * Every operation takes the value to send followed by the serialization strategies to encode it and decode the result
 * with (the Android SDK's pattern, with no reflection-based fallback), and an optional trailing builder for the
 * remaining options.
 */
public interface FirebaseDataConnect : AutoCloseable {

    /** The app this instance belongs to. */
    public val app: FirebaseApp

    /** The connector this instance executes the operations of. */
    public val config: ConnectorConfig

    /** The settings this instance was created with. */
    public val settings: DataConnectSettings

    /**
     * Connects to the Data Connect emulator instead of the production service; must be called before any operation is
     * executed. The default host is the one the Android emulator reaches its host machine with; the other platforms'
     * tests use `localhost`.
     */
    public fun useEmulator(host: String = "10.0.2.2", port: Int = 9399)

    /** The options of a [QueryRef] beyond the required ones, set in the trailing lambda of [query]. */
    public interface QueryRefOptionsBuilder<Data, Variables> {
        /** Which SDK is creating the reference; `null` selects [CallerSdkType.Base]. */
        public var callerSdkType: CallerSdkType?

        /** The module the variables serializer resolves contextual serializers from. */
        public var variablesSerializersModule: SerializersModule?

        /** The module the data deserializer resolves contextual serializers from. */
        public var dataSerializersModule: SerializersModule?
    }

    /** Creates a reference to the query [operationName] with the given [variables]. */
    public fun <Data, Variables> query(
        operationName: String,
        variables: Variables,
        dataDeserializer: DeserializationStrategy<Data>,
        variablesSerializer: SerializationStrategy<Variables>,
        optionsBuilder: (QueryRefOptionsBuilder<Data, Variables>.() -> Unit)? = null,
    ): QueryRef<Data, Variables>

    /** The options of a [MutationRef] beyond the required ones, set in the trailing lambda of [mutation]. */
    public interface MutationRefOptionsBuilder<Data, Variables> {
        /** Which SDK is creating the reference; `null` selects [CallerSdkType.Base]. */
        public var callerSdkType: CallerSdkType?

        /** The module the variables serializer resolves contextual serializers from. */
        public var variablesSerializersModule: SerializersModule?

        /** The module the data deserializer resolves contextual serializers from. */
        public var dataSerializersModule: SerializersModule?
    }

    /** Creates a reference to the mutation [operationName] with the given [variables]. */
    public fun <Data, Variables> mutation(
        operationName: String,
        variables: Variables,
        dataDeserializer: DeserializationStrategy<Data>,
        variablesSerializer: SerializationStrategy<Variables>,
        optionsBuilder: (MutationRefOptionsBuilder<Data, Variables>.() -> Unit)? = null,
    ): MutationRef<Data, Variables>

    /** Releases this instance; a later [getInstance] for the same app and connector creates a new one. */
    override fun close()

    /** [close], waiting for the release to complete. */
    public suspend fun suspendingClose()

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String

    /** Which SDK created a reference: user code ([Base]) or a generated connector SDK ([Generated]). */
    public enum class CallerSdkType {
        Base,
        Generated,
    }

    public companion object
}

/**
 * The instance for [app] and [config], created with [settings] if it does not exist yet. Throws
 * [IllegalArgumentException] if an instance for the same app and connector exists with different settings.
 */
public expect fun FirebaseDataConnect.Companion.getInstance(
    app: FirebaseApp,
    config: ConnectorConfig,
    settings: DataConnectSettings = DataConnectSettings(),
): FirebaseDataConnect

/** The instance for the default app and [config], see [getInstance]. */
public expect fun FirebaseDataConnect.Companion.getInstance(config: ConnectorConfig, settings: DataConnectSettings = DataConnectSettings()): FirebaseDataConnect

/** The log level of every instance; set it to change it. */
public expect val FirebaseDataConnect.Companion.logLevel: MutableStateFlow<LogLevel>
