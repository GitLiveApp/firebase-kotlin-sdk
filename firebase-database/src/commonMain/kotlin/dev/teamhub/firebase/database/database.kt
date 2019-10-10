@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")
package dev.teamhub.firebase.database

import kotlin.reflect.KClass

expect annotation class Exclude()
expect annotation class IgnoreExtraProperties()

expect fun getFirebaseDatabase(): FirebaseDatabase

expect enum class LoggerLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    NONE
}

expect class FirebaseDatabase

expect fun FirebaseDatabase.getReference(path: String): DatabaseReference
expect fun FirebaseDatabase.setPersistenceEnabled(enabled: Boolean)
expect fun FirebaseDatabase.setLogLevel(logLevel: LoggerLevel)

expect class DatabaseReference

expect fun DatabaseReference.push(): DatabaseReference
expect fun DatabaseReference.onDisconnect(): OnDisconnect
expect fun DatabaseReference.addValueEventListener(listener: ValueEventListener): ValueEventListener
expect fun DatabaseReference.addListenerForSingleValueEvent(listener: ValueEventListener)
expect fun DatabaseReference.removeEventListener(listener: ValueEventListener)

expect suspend fun DatabaseReference.awaitSetValue(value: Any?)
expect suspend fun DatabaseReference.awaitUpdateChildren(update: Map<String, Any?>)
expect suspend fun DatabaseReference.awaitRemoveValue()

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
