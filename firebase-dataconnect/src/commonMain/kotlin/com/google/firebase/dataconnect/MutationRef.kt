/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule

/** A reference to a mutation, see [FirebaseDataConnect.mutation]. */
public interface MutationRef<Data, Variables> : OperationRef<Data, Variables> {

    override suspend fun execute(): MutationResult<Data, Variables>

    @ExperimentalFirebaseDataConnect
    override fun copy(
        operationName: String,
        variables: Variables,
        dataDeserializer: DeserializationStrategy<Data>,
        variablesSerializer: SerializationStrategy<Variables>,
        callerSdkType: FirebaseDataConnect.CallerSdkType,
        dataSerializersModule: SerializersModule?,
        variablesSerializersModule: SerializersModule?,
    ): MutationRef<Data, Variables>

    @ExperimentalFirebaseDataConnect
    override fun <NewVariables> withVariablesSerializer(
        variables: NewVariables,
        variablesSerializer: SerializationStrategy<NewVariables>,
        variablesSerializersModule: SerializersModule?,
    ): MutationRef<Data, NewVariables>

    @ExperimentalFirebaseDataConnect
    override fun <NewData> withDataDeserializer(
        dataDeserializer: DeserializationStrategy<NewData>,
        dataSerializersModule: SerializersModule?,
    ): MutationRef<NewData, Variables>
}

/** The result of executing a [MutationRef]. */
public interface MutationResult<Data, Variables> : OperationResult<Data, Variables> {
    override val ref: MutationRef<Data, Variables>
}
