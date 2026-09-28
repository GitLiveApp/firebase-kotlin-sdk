/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule

/**
 * A reference to an operation of a connector with a fixed set of [variables]: [execute] sends it and decodes its data.
 * A reference is immutable; [copy], [withVariablesSerializer] and [withDataDeserializer] derive new ones.
 */
public interface OperationRef<Data, Variables> {

    /** The instance this reference belongs to. */
    public val dataConnect: FirebaseDataConnect

    /** The name of the operation in the connector. */
    public val operationName: String

    /** The variables sent with the operation. */
    public val variables: Variables

    /** Decodes the data the operation returns. */
    public val dataDeserializer: DeserializationStrategy<Data>

    /** Encodes [variables]. */
    public val variablesSerializer: SerializationStrategy<Variables>

    /** Which SDK created this reference. */
    public val callerSdkType: FirebaseDataConnect.CallerSdkType

    /** The module [dataDeserializer] resolves contextual serializers from, if any. */
    public val dataSerializersModule: SerializersModule?

    /** The module [variablesSerializer] resolves contextual serializers from, if any. */
    public val variablesSerializersModule: SerializersModule?

    /** Executes the operation; throws [DataConnectOperationException] when the server reports errors. */
    public suspend fun execute(): OperationResult<Data, Variables>

    /** A copy of this reference with the given values replaced. */
    @ExperimentalFirebaseDataConnect
    public fun copy(
        operationName: String = this.operationName,
        variables: Variables = this.variables,
        dataDeserializer: DeserializationStrategy<Data> = this.dataDeserializer,
        variablesSerializer: SerializationStrategy<Variables> = this.variablesSerializer,
        callerSdkType: FirebaseDataConnect.CallerSdkType = this.callerSdkType,
        dataSerializersModule: SerializersModule? = this.dataSerializersModule,
        variablesSerializersModule: SerializersModule? = this.variablesSerializersModule,
    ): OperationRef<Data, Variables>

    /** A copy of this reference with other variables, and their serializer. */
    @ExperimentalFirebaseDataConnect
    public fun <NewVariables> withVariablesSerializer(
        variables: NewVariables,
        variablesSerializer: SerializationStrategy<NewVariables>,
        variablesSerializersModule: SerializersModule? = this.variablesSerializersModule,
    ): OperationRef<Data, NewVariables>

    /** A copy of this reference decoding its data with another deserializer. */
    @ExperimentalFirebaseDataConnect
    public fun <NewData> withDataDeserializer(
        dataDeserializer: DeserializationStrategy<NewData>,
        dataSerializersModule: SerializersModule? = this.dataSerializersModule,
    ): OperationRef<NewData, Variables>

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}

/** The result of executing an [OperationRef]: the decoded [data]. */
public interface OperationResult<Data, Variables> {
    /** The reference that was executed. */
    public val ref: OperationRef<Data, Variables>

    /** The decoded data. */
    public val data: Data

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}
