/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect.generated

import com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
import com.google.firebase.dataconnect.OperationRef
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

/** The base of the operation classes that the Data Connect tooling generates: an operation with its serializers, without variables. */
public interface GeneratedOperation<Connector : GeneratedConnector<Connector>, Data, Variables> {
    /** The connector the operation belongs to. */
    public val connector: Connector

    /** The name of the operation. */
    public val operationName: String

    /** Decodes the data the operation returns. */
    public val dataDeserializer: DeserializationStrategy<Data>

    /** Encodes the operation's variables. */
    public val variablesSerializer: SerializationStrategy<Variables>

    /** A reference to the operation with the given [variables]. */
    public fun ref(variables: Variables): OperationRef<Data, Variables> = connector.dataConnect.mutation(operationName, variables, dataDeserializer, variablesSerializer)

    /** A copy of this operation with the given values replaced. */
    @ExperimentalFirebaseDataConnect
    public fun copy(
        connector: Connector = this.connector,
        operationName: String = this.operationName,
        dataDeserializer: DeserializationStrategy<Data> = this.dataDeserializer,
        variablesSerializer: SerializationStrategy<Variables> = this.variablesSerializer,
    ): GeneratedOperation<Connector, Data, Variables>

    /** A copy of this operation with another variables serializer. */
    @ExperimentalFirebaseDataConnect
    public fun <NewVariables> withVariablesSerializer(variablesSerializer: SerializationStrategy<NewVariables>): GeneratedOperation<Connector, Data, NewVariables>

    /** A copy of this operation with another data deserializer. */
    @ExperimentalFirebaseDataConnect
    public fun <NewData> withDataDeserializer(dataDeserializer: DeserializationStrategy<NewData>): GeneratedOperation<Connector, NewData, Variables>

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}
