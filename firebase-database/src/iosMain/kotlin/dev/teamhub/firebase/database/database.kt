package dev.teamhub.firebase.database

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer

/** Returns the [FirebaseDatabase] instance of the default [FirebaseApp]. */
actual val Firebase.database: FirebaseDatabase
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

/** Returns the [FirebaseDatabase] instance for the specified [url]. */
actual fun Firebase.database(url: String): FirebaseDatabase {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp]. */
actual fun Firebase.database(app: FirebaseApp): FirebaseDatabase {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

/** Returns the [FirebaseDatabase] instance of the given [FirebaseApp] and [url]. */
actual fun Firebase.database(
    app: FirebaseApp,
    url: String
): FirebaseDatabase {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual class FirebaseDatabase {
    actual fun reference(path: String): DatabaseReference {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setPersistenceEnabled(enabled: Boolean) {
    }

    actual fun setLoggingEnabled(enabled: Boolean) {
    }
}

actual open class Query {
    actual val valueEvents: Flow<DataSnapshot>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun childEvents(vararg types: ChildEvent.Type): Flow<ChildEvent> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun orderByKey(): Query {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun orderByChild(path: String): Query {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun startAt(value: String, key: String?): Query {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun startAt(value: Double, key: String?): Query {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun startAt(value: Boolean, key: String?): Query {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual class DatabaseReference : Query() {
    actual val key: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun push(): DatabaseReference {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun child(path: String): DatabaseReference {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun onDisconnect(): OnDisconnect {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ImplicitReflectionSerializer
    actual suspend fun setValue(value: Any?) {
    }

    actual suspend inline fun <reified T> setValue(strategy: SerializationStrategy<T>, value: T) {
    }

    @ImplicitReflectionSerializer
    actual suspend fun updateChildren(update: Map<String, Any?>) {
    }

    actual suspend fun removeValue() {
    }
}

actual class DataSnapshot {
    actual val exists: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val key: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    @ImplicitReflectionSerializer
    actual inline fun <reified T> value(): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> value(strategy: DeserializationStrategy<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun child(path: String): DataSnapshot {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual val children: Iterable<DataSnapshot>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

actual class DatabaseException : RuntimeException()
actual class OnDisconnect {
    actual suspend fun removeValue() {
    }

    actual suspend fun cancel() {
    }

    @ImplicitReflectionSerializer
    actual suspend inline fun <reified T : Any> setValue(value: T) {
    }

    actual suspend inline fun <reified T> setValue(strategy: SerializationStrategy<T>, value: T) {
    }

    @ImplicitReflectionSerializer
    actual suspend fun updateChildren(update: Map<String, Any?>) {
    }
}