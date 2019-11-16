package dev.teamhub.firebase.database

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

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

expect class DatabaseReference {
    fun push(): DatabaseReference
    fun onDisconnect(): OnDisconnect
    val valueEvents: Flow<DataSnapshot>
    val singleValueEvent: Deferred<DataSnapshot>
    suspend fun setValue(value: Any?)
    suspend fun updateChildren(update: Map<String, Any?>)
    suspend fun removeValue()
}

expect class DataSnapshot {
    val exists: Boolean
    /**
     * Returns the content of the DataSnapshot converted to a POJO.
     *
     * Supports generics like List<> or Map<>. Use @JvmSuppressWildcards to force the compiler to
     * use the type `T`, and not `? extends T`.
     */
    inline fun <reified T : Any> value(): T?
    inline fun <reified T : Any> values(): List<T>?
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
    suspend fun setValue(value: Any?)
    suspend fun updateChildren(update: Map<String, Any?>)
}

