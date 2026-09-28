/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect.generated

import com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
import com.google.firebase.dataconnect.FirebaseDataConnect
import com.google.firebase.dataconnect.MutationRef
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

/** A generated mutation, see [GeneratedOperation]. */
public interface GeneratedMutation<Connector : GeneratedConnector<Connector>, Data, Variables> : GeneratedOperation<Connector, Data, Variables> {
    override fun ref(variables: Variables): MutationRef<Data, Variables> = connector.dataConnect.mutation(operationName, variables, dataDeserializer, variablesSerializer) {
        callerSdkType = FirebaseDataConnect.CallerSdkType.Generated
    }

    @ExperimentalFirebaseDataConnect
    override fun copy(
        connector: Connector,
        operationName: String,
        dataDeserializer: DeserializationStrategy<Data>,
        variablesSerializer: SerializationStrategy<Variables>,
    ): GeneratedMutation<Connector, Data, Variables>

    @ExperimentalFirebaseDataConnect
    override fun <NewVariables> withVariablesSerializer(variablesSerializer: SerializationStrategy<NewVariables>): GeneratedMutation<Connector, Data, NewVariables>

    @ExperimentalFirebaseDataConnect
    override fun <NewData> withDataDeserializer(dataDeserializer: DeserializationStrategy<NewData>): GeneratedMutation<Connector, NewData, Variables>
}
