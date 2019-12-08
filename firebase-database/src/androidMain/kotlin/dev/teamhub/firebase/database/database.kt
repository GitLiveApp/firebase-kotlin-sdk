package dev.teamhub.firebase.database

import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.OnDisconnect
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.tasks.asDeferred

import kotlin.reflect.KClass

suspend fun <T> Task<T>.awaitWhileOnline(): T {
    val notConnected = CompletableDeferred<Unit>()
    val reference = getFirebaseDatabase().getReference(".info/connected")
    val listener = reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                notConnected.completeExceptionally(error.toException())
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == false) notConnected.complete(Unit)
            }
        })

    try {
        return select {
            asDeferred().onAwait { it }
            notConnected.onAwait { throw DatabaseException("Database not connected") }
        }
    } finally {
        reference.removeEventListener(listener)
    }
}

actual fun getFirebaseDatabase() = FirebaseDatabase.getInstance()

actual typealias LoggerLevel = Logger.Level

actual typealias FirebaseDatabase = FirebaseDatabase

actual typealias DatabaseReference = DatabaseReference

actual suspend fun DatabaseReference.awaitSetValue(value: Any?) = setValue(value).awaitWhileOnline().run { Unit }

actual suspend fun DatabaseReference.awaitUpdateChildren(update: Map<String, Any?>) = updateChildren(update).awaitWhileOnline().run { Unit }

actual typealias ValueEventListener = com.google.firebase.database.ValueEventListener

actual typealias DataSnapshot = DataSnapshot

actual fun <T: Any> DataSnapshot.getValue(valueType: KClass<T>) = getValue(valueType.java)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DataSnapshot.exists(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DataSnapshot.getValue(): Any? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val DataSnapshot.children: Iterable<DataSnapshot>
    get() = TODO("not implemented")

actual typealias DatabaseException = DatabaseException

actual typealias DatabaseError = DatabaseError

actual typealias OnDisconnect = OnDisconnect

actual suspend fun OnDisconnect.awaitRemoveValue() = removeValue().awaitWhileOnline().run { Unit }

actual suspend fun OnDisconnect.awaitCancel() = cancel().awaitWhileOnline().run { Unit }

actual suspend fun OnDisconnect.awaitSetValue(value: Any?) = setValue(value).awaitWhileOnline().run { Unit }

actual suspend fun OnDisconnect.awaitUpdateChildren(update: Map<String, Any?>) = updateChildren(update).awaitWhileOnline().run { Unit }

actual val TIMESTAMP = ServerValue.TIMESTAMP

actual suspend fun DatabaseReference.awaitRemoveValue() = removeValue().awaitWhileOnline().run { Unit }

actual fun FirebaseDatabase.getReference(path: String) = getReference(path)

actual fun FirebaseDatabase.setPersistenceEnabled(enabled: Boolean) = setPersistenceEnabled(enabled)

actual fun FirebaseDatabase.setLogLevel(logLevel: LoggerLevel) = setLogLevel(logLevel)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DatabaseReference.push(): DatabaseReference {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DatabaseReference.onDisconnect(): OnDisconnect {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DatabaseReference.addValueEventListener(listener: ValueEventListener): ValueEventListener {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DatabaseReference.removeEventListener(listener: ValueEventListener) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DatabaseError.toException(): DatabaseException {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DatabaseReference.addListenerForSingleValueEvent(listener: ValueEventListener) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DataSnapshot.child(path: String): DataSnapshot {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual typealias Exclude = Exclude
actual typealias IgnoreExtraProperties = IgnoreExtraProperties

