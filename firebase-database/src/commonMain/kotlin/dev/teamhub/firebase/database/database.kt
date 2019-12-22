package dev.teamhub.firebase.database

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.database.ChildEvent.Type.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerializationStrategy

/** Returns the [FirebaseDatabase] instance of the default [FirebaseApp]. */
expect val Firebase.database: FirebaseDatabase

/** Returns the [FirebaseDatabase] instance for the specified [url]. */
expect fun Firebase.database(url: String): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp]. */
expect fun Firebase.database(app: FirebaseApp): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp] and [url]. */
expect fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase

expect class FirebaseDatabase {
    fun reference(path: String): DatabaseReference
    fun setPersistenceEnabled(enabled: Boolean)
    fun setLoggingEnabled(enabled: Boolean)
}

data class ChildEvent internal constructor(val type: Type, val snapshot: DataSnapshot, val previousChildName: String?) {
    enum class Type {
        ADDED,
        CHANGED,
        MOVED,
        REMOVED
    }
}

expect open class Query {
    val valueEvents: Flow<DataSnapshot>
    fun childEvents(vararg types: ChildEvent.Type = arrayOf(ADDED, CHANGED, MOVED, REMOVED)): Flow<ChildEvent>
    fun orderByChild(path: String): Query
}

expect class DatabaseReference : Query {
    fun push(): DatabaseReference
    fun onDisconnect(): OnDisconnect
    @ImplicitReflectionSerializer
    suspend fun setValue(value: Any?)
    suspend inline fun <reified T> setValue(strategy: SerializationStrategy<T>, value: T)
    @ImplicitReflectionSerializer
    suspend fun updateChildren(update: Map<String, Any?>)
    suspend fun removeValue()
}

expect class DataSnapshot {
    val exists: Boolean
    @ImplicitReflectionSerializer
    inline fun <reified T> value(): T
    inline fun <reified T> value(strategy: DeserializationStrategy<T>): T
    fun child(path: String): DataSnapshot
    val children: Iterable<DataSnapshot>
}

expect object ServerValue {
    val TIMESTAMP: Map<String, String>
}

expect class DatabaseException : RuntimeException

expect class OnDisconnect {
    suspend fun removeValue()
    suspend fun cancel()
    @ImplicitReflectionSerializer
    suspend inline fun <reified T: Any> setValue(value: T)
    suspend inline fun <reified T> setValue(strategy: SerializationStrategy<T>, value: T)
    @ImplicitReflectionSerializer
    suspend fun updateChildren(update: Map<String, Any?>)
}

