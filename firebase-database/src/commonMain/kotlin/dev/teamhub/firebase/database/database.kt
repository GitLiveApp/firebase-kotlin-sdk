package dev.teamhub.firebase.database

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

expect annotation class Exclude()
expect annotation class IgnoreExtraProperties()

/** Returns the [FirebaseDatabase] instance of the default [FirebaseApp]. */
expect val Firebase.database: FirebaseDatabase

/** Returns the [FirebaseDatabase] instance for the specified [url]. */
expect fun Firebase.database(url: String): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp]. */
expect fun Firebase.database(app: FirebaseApp): FirebaseDatabase

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp] and [url]. */
expect fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase

/**
 * Returns the content of the DataSnapshot converted to a POJO.
 *
 * Supports generics like List<> or Map<>. Use @JvmSuppressWildcards to force the compiler to
 * use the type `T`, and not `? extends T`.
 */
expect inline fun <reified T> DataSnapshot.getValue(): T?

expect enum class LoggerLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    NONE
}

expect class FirebaseDatabase {
    fun getReference(path: String): DatabaseReference
    fun setPersistenceEnabled(enabled: Boolean)
    fun setLogLevel(logLevel: LoggerLevel)
}


expect class DatabaseReference {
    fun push(): DatabaseReference
    fun onDisconnect(): OnDisconnect
    val valueEvents: Flow<DataSnapshot>
    fun addValueEventListener(listener: ValueEventListener): ValueEventListener
    fun addListenerForSingleValueEvent(listener: ValueEventListener)
    fun removeEventListener(listener: ValueEventListener)
    suspend fun setValue(value: Any?)
    suspend fun updateChildren(update: Map<String, Any?>)
    suspend fun removeValue()
}


expect interface ValueEventListener {
    fun onDataChange(data: DataSnapshot)
    fun onCancelled(error: DatabaseError)
}

expect class DataSnapshot

expect fun <T: Any> DataSnapshot.getValue(valueType: KClass<T>): T?
expect fun DataSnapshot.exists(): Boolean
expect fun DataSnapshot.getValue(): Any?
expect fun DataSnapshot.child(path: String): DataSnapshot
expect val DataSnapshot.children: Iterable<DataSnapshot>

expect val TIMESTAMP: Map<String, String>

expect class DatabaseException : RuntimeException

expect class DatabaseError

expect fun DatabaseError.toException(): DatabaseException

expect class OnDisconnect

expect suspend fun OnDisconnect.awaitRemoveValue()
expect suspend fun OnDisconnect.awaitCancel()
expect suspend fun OnDisconnect.awaitSetValue(value: Any?)
expect suspend fun OnDisconnect.awaitUpdateChildren(update: Map<String, Any?>)
