package dev.teamhub.firebase.database

import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.OnDisconnect
import com.google.firebase.database.ValueEventListener
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await

import kotlin.reflect.KClass

actual val Firebase.database
    get() = FirebaseDatabase.getInstance()

actual fun Firebase.database(url: String) =
    FirebaseDatabase.getInstance(url)

actual fun Firebase.database(app: FirebaseApp) =
    FirebaseDatabase.getInstance(app.android)

actual fun Firebase.database(app: FirebaseApp, url: String) =
    FirebaseDatabase.getInstance(app.android, url)

actual inline fun <reified T> DataSnapshot.getValue(): T? {
    return getValue(object : GenericTypeIndicator<T>() {})
}

actual typealias LoggerLevel = Logger.Level

actual typealias FirebaseDatabase = FirebaseDatabase

actual typealias DatabaseReference = DatabaseReference

actual suspend fun DatabaseReference.awaitSetValue(value: Any?) = setValue(value).await().run { Unit }

actual suspend fun DatabaseReference.awaitUpdateChildren(update: Map<String, Any?>) = updateChildren(update).await().run { Unit }

actual typealias ValueEventListener = ValueEventListener

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

actual suspend fun OnDisconnect.awaitRemoveValue() = removeValue().await().run { Unit }

actual suspend fun OnDisconnect.awaitCancel() = cancel().await().run { Unit }

actual suspend fun OnDisconnect.awaitSetValue(value: Any?) = setValue(value).await().run { Unit }

actual suspend fun OnDisconnect.awaitUpdateChildren(update: Map<String, Any?>) = updateChildren(update).await().run { Unit }

actual val TIMESTAMP = ServerValue.TIMESTAMP

actual suspend fun DatabaseReference.awaitRemoveValue() = removeValue().await().run { Unit }

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

