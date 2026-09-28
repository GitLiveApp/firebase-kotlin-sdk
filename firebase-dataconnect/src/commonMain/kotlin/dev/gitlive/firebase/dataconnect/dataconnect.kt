/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect

import com.google.firebase.dataconnect.FirebaseDataConnect
import com.google.firebase.dataconnect.MutationRef
import com.google.firebase.dataconnect.QueryRef
import kotlinx.serialization.serializer

/*
 * The dev.gitlive layer of Data Connect is deliberately thin: the com.google.firebase.dataconnect API is already
 * coroutine- and kotlinx-serialization-based, so it is used as is from common code. Only the pieces below add something.
 */

/** [FirebaseDataConnect.query] with the serializers of [Data] and [Variables] looked up by the compiler. */
public inline fun <reified Data, reified Variables> FirebaseDataConnect.query(
    operationName: String,
    variables: Variables,
    noinline optionsBuilder: (FirebaseDataConnect.QueryRefOptionsBuilder<Data, Variables>.() -> Unit)? = null,
): QueryRef<Data, Variables> = query(operationName, variables, serializer<Data>(), serializer<Variables>(), optionsBuilder)

/** [FirebaseDataConnect.mutation] with the serializers of [Data] and [Variables] looked up by the compiler. */
public inline fun <reified Data, reified Variables> FirebaseDataConnect.mutation(
    operationName: String,
    variables: Variables,
    noinline optionsBuilder: (FirebaseDataConnect.MutationRefOptionsBuilder<Data, Variables>.() -> Unit)? = null,
): MutationRef<Data, Variables> = mutation(operationName, variables, serializer<Data>(), serializer<Variables>(), optionsBuilder)
