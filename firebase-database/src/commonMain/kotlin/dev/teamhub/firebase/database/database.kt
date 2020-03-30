/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.teamhub.firebase.database

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.database.ChildEvent.Type.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy

/** Returns the [FirebaseDatabase] instance of the default [FirebaseApp]. */
@FlowPreview
@InternalSerializationApi
expect val Firebase.database: FirebaseDatabase

/** Returns the [FirebaseDatabase] instance for the specified [url]. */
@FlowPreview
@InternalSerializationApi
expect fun Firebase.database(url: String): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp]. */
@FlowPreview
@InternalSerializationApi
expect fun Firebase.database(app: FirebaseApp): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp] and [url]. */
@FlowPreview
@InternalSerializationApi
expect fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase

@FlowPreview
@InternalSerializationApi
expect class FirebaseDatabase {
    fun reference(path: String): DatabaseReference
    fun setPersistenceEnabled(enabled: Boolean)
    fun setLoggingEnabled(enabled: Boolean)
}

data class ChildEvent internal constructor(
    val snapshot: DataSnapshot,
    val type: Type,
    val previousChildName: String?
) {
    enum class Type {
        ADDED,
        CHANGED,
        MOVED,
        REMOVED
    }
}

@FlowPreview
expect open class Query {
    val valueEvents: Flow<DataSnapshot>
    fun childEvents(vararg types: ChildEvent.Type = arrayOf(ADDED, CHANGED, MOVED, REMOVED)): Flow<ChildEvent>
    fun orderByKey(): Query
    fun orderByChild(path: String): Query
    fun startAt(value: String, key: String? = null): Query
    fun startAt(value: Double, key: String? = null): Query
    fun startAt(value: Boolean, key: String? = null): Query
}

@FlowPreview
@InternalSerializationApi
expect class DatabaseReference : Query {
    val key: String?
    fun push(): DatabaseReference
    fun child(path: String): DatabaseReference
    fun onDisconnect(): OnDisconnect
    @ImplicitReflectionSerializer
    suspend fun setValue(value: Any?)
    suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T)
    @ImplicitReflectionSerializer
    suspend fun updateChildren(update: Map<String, Any?>)
    suspend fun removeValue()
}

expect class DataSnapshot {
    val exists: Boolean
    val key: String?
    @ImplicitReflectionSerializer
    inline fun <reified T> value(): T
    fun <T> value(strategy: DeserializationStrategy<T>): T
    fun child(path: String): DataSnapshot
    val children: Iterable<DataSnapshot>
}

object ServerValue {
    val TIMESTAMP = Double.POSITIVE_INFINITY
}

expect class DatabaseException : RuntimeException

@InternalSerializationApi
expect class OnDisconnect {
    suspend fun removeValue()
    suspend fun cancel()
    @ImplicitReflectionSerializer
    suspend fun setValue(value: Any)
    suspend fun <T> setValue(strategy: SerializationStrategy<T>, value: T)
    @ImplicitReflectionSerializer
    suspend fun updateChildren(update: Map<String, Any?>)
}

