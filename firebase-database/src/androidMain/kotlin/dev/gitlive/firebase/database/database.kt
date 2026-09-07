/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("databaseAndroid")

package dev.gitlive.firebase.database

import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.android.database.ChildEventListener
import dev.gitlive.firebase.android.database.DatabaseError
import dev.gitlive.firebase.android.database.Logger
import dev.gitlive.firebase.android.database.MutableData
import dev.gitlive.firebase.android.database.Transaction
import dev.gitlive.firebase.android.database.ValueEventListener
import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android
import dev.gitlive.firebase.database.ChildEvent.Type
import dev.gitlive.firebase.database.android as publicAndroid
import dev.gitlive.firebase.internal.android
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.reencodeTransformation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import java.util.WeakHashMap

public val FirebaseDatabase.android: dev.gitlive.firebase.android.database.FirebaseDatabase get() = dev.gitlive.firebase.android.database.FirebaseDatabase.getInstance()

public actual val Firebase.database: FirebaseDatabase
    by lazy { FirebaseDatabase.getInstance(dev.gitlive.firebase.android.database.FirebaseDatabase.getInstance()) }

public actual fun Firebase.database(url: String): FirebaseDatabase = FirebaseDatabase.getInstance(dev.gitlive.firebase.android.database.FirebaseDatabase.getInstance(url))

public actual fun Firebase.database(app: FirebaseApp): FirebaseDatabase = FirebaseDatabase.getInstance(dev.gitlive.firebase.android.database.FirebaseDatabase.getInstance(app.android))

public actual fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase = FirebaseDatabase.getInstance(dev.gitlive.firebase.android.database.FirebaseDatabase.getInstance(app.android, url))

public actual class FirebaseDatabase internal constructor(internal val android: dev.gitlive.firebase.android.database.FirebaseDatabase) {

    public companion object {
        private val instances = WeakHashMap<dev.gitlive.firebase.android.database.FirebaseDatabase, FirebaseDatabase>()

        internal fun getInstance(
            android: dev.gitlive.firebase.android.database.FirebaseDatabase,
        ) = instances.getOrPut(android) { FirebaseDatabase(android) }
    }

    private var persistenceEnabled = true

    public actual fun reference(path: String): DatabaseReference = DatabaseReference(NativeDatabaseReference(android.getReference(path), persistenceEnabled))

    public actual fun reference(): DatabaseReference = DatabaseReference(NativeDatabaseReference(android.reference, persistenceEnabled))

    public actual fun setPersistenceEnabled(enabled: Boolean) {
        android.setPersistenceEnabled(enabled)
        persistenceEnabled = enabled
    }

    public actual fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long) {
        android.setPersistenceCacheSizeBytes(cacheSizeInBytes)
    }

    public actual fun setLoggingEnabled(enabled: Boolean) {
        android.setLogLevel(Logger.Level.DEBUG.takeIf { enabled } ?: Logger.Level.NONE)
    }

    public actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
    }

    public actual fun goOffline() {
        android.goOffline()
    }

    public actual fun goOnline() {
        android.goOnline()
    }

    public actual fun purgeOutstandingWrites() {
        android.purgeOutstandingWrites()
    }
}

internal actual open class NativeQuery(
    open val android: dev.gitlive.firebase.android.database.Query,
    val persistenceEnabled: Boolean,
)

public val Query.android: dev.gitlive.firebase.android.database.Query get() = nativeQuery.android

public actual open class Query internal actual constructor(
    internal val nativeQuery: NativeQuery,
) {

    internal constructor(
        android: dev.gitlive.firebase.android.database.Query,
        persistenceEnabled: Boolean,
    ) : this(NativeQuery(android, persistenceEnabled))

    internal open val android: com.google.firebase.database.Query = nativeQuery.android

    @Deprecated("Writes no longer depend on the persistence setting, so this accessor is unused; it will be removed in the next major version.")
    public val persistenceEnabled: Boolean = nativeQuery.persistenceEnabled

    public actual fun orderByKey(): Query = Query(android.orderByKey(), nativeQuery.persistenceEnabled)

    public actual fun orderByValue(): Query = Query(android.orderByValue(), nativeQuery.persistenceEnabled)

    public actual fun orderByChild(path: String): Query = Query(android.orderByChild(path), nativeQuery.persistenceEnabled)

    public actual fun startAt(value: String, key: String?): Query = Query(android.startAt(value, key), nativeQuery.persistenceEnabled)

    public actual fun startAt(value: Double, key: String?): Query = Query(android.startAt(value, key), nativeQuery.persistenceEnabled)

    public actual fun startAt(value: Boolean, key: String?): Query = Query(android.startAt(value, key), nativeQuery.persistenceEnabled)

    public actual fun endAt(value: String, key: String?): Query = Query(android.endAt(value, key), nativeQuery.persistenceEnabled)

    public actual fun endAt(value: Double, key: String?): Query = Query(android.endAt(value, key), nativeQuery.persistenceEnabled)

    public actual fun endAt(value: Boolean, key: String?): Query = Query(android.endAt(value, key), nativeQuery.persistenceEnabled)

    public actual fun limitToFirst(limit: Int): Query = Query(android.limitToFirst(limit), nativeQuery.persistenceEnabled)

    public actual fun limitToLast(limit: Int): Query = Query(android.limitToLast(limit), nativeQuery.persistenceEnabled)

    public actual fun equalTo(value: String, key: String?): Query = Query(android.equalTo(value, key), nativeQuery.persistenceEnabled)

    public actual fun equalTo(value: Double, key: String?): Query = Query(android.equalTo(value, key), nativeQuery.persistenceEnabled)

    public actual fun equalTo(value: Boolean, key: String?): Query = Query(android.equalTo(value, key), nativeQuery.persistenceEnabled)

    public actual val valueEvents: Flow<DataSnapshot>
        get() = callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    trySendBlocking(DataSnapshot(snapshot, nativeQuery.persistenceEnabled))
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            android.addValueEventListener(listener)
            awaitClose { android.removeEventListener(listener) }
        }

    public actual fun childEvents(vararg types: Type): Flow<ChildEvent> = callbackFlow {
        val listener = object : ChildEventListener {

            val moved by lazy { types.contains(Type.MOVED) }
            override fun onChildMoved(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if (moved) trySend(ChildEvent(DataSnapshot(snapshot, nativeQuery.persistenceEnabled), Type.MOVED, previousChildName))
            }

            val changed by lazy { types.contains(Type.CHANGED) }
            override fun onChildChanged(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if (changed) trySend(ChildEvent(DataSnapshot(snapshot, nativeQuery.persistenceEnabled), Type.CHANGED, previousChildName))
            }

            val added by lazy { types.contains(Type.ADDED) }
            override fun onChildAdded(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                if (added) trySend(ChildEvent(DataSnapshot(snapshot, nativeQuery.persistenceEnabled), Type.ADDED, previousChildName))
            }

            val removed by lazy { types.contains(Type.REMOVED) }
            override fun onChildRemoved(snapshot: com.google.firebase.database.DataSnapshot) {
                if (removed) trySend(ChildEvent(DataSnapshot(snapshot, nativeQuery.persistenceEnabled), Type.REMOVED, null))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        android.addChildEventListener(listener)
        awaitClose { android.removeEventListener(listener) }
    }

    public actual suspend fun get(): DataSnapshot {
        val deferred = CompletableDeferred<DataSnapshot>()
        android.get()
            .addOnSuccessListener { snapshot ->
                deferred.complete(DataSnapshot(snapshot, nativeQuery.persistenceEnabled))
            }
            .addOnFailureListener { exception ->
                deferred.completeExceptionally(exception)
            }
            .addOnCanceledListener {
                deferred.cancel()
            }
        return deferred.await()
    }

    override fun toString(): String = android.toString()
}

internal actual class NativeDatabaseReference internal constructor(
    override val android: dev.gitlive.firebase.android.database.DatabaseReference,
    persistenceEnabled: Boolean,
) : NativeQuery(android, persistenceEnabled) {

    actual val key get() = android.key
    val database = FirebaseDatabase(android.database)

    actual fun child(path: String) = NativeDatabaseReference(android.child(path), persistenceEnabled)

    actual fun push() = NativeDatabaseReference(android.push(), persistenceEnabled)
    actual fun onDisconnect() = NativeOnDisconnect(android.onDisconnect(), persistenceEnabled, database)

    actual suspend fun setValueEncoded(encodedValue: Any?) {
        android.setValue(encodedValue).await()
    }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) {
        android.updateChildren(encodedUpdate.android).await()
    }

    actual suspend fun removeValue() {
        android.removeValue().await()
    }

    @OptIn(ExperimentalSerializationApi::class)
    actual suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit, transactionUpdate: (currentData: T) -> T): DataSnapshot {
        val deferred = CompletableDeferred<DataSnapshot>()
        android.runTransaction(object : Transaction.Handler {

            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val valueToReencode = currentData.value
                // Value may be null initially, so only reencode if this is allowed
                if (strategy.descriptor.isNullable || valueToReencode != null) {
                    currentData.value = reencodeTransformation(
                        strategy,
                        valueToReencode,
                        buildSettings,
                        transactionUpdate,
                    )
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: dev.gitlive.firebase.android.database.DataSnapshot?,
            ) {
                if (error != null) {
                    deferred.completeExceptionally(error.toException())
                } else {
                    deferred.complete(DataSnapshot(snapshot!!, persistenceEnabled))
                }
            }
        })
        return deferred.await()
    }
}

public val DatabaseReference.android: dev.gitlive.firebase.android.database.DatabaseReference get() = nativeReference.android
public val DataSnapshot.android: dev.gitlive.firebase.android.database.DataSnapshot get() = android

public actual class DataSnapshot internal constructor(
    internal val android: dev.gitlive.firebase.android.database.DataSnapshot,
    private val persistenceEnabled: Boolean,
) {

    public actual val exists: Boolean get() = android.exists()

    public actual val key: String? get() = android.key

    public actual val ref: DatabaseReference get() = DatabaseReference(NativeDatabaseReference(android.ref, persistenceEnabled))

    public actual val value: Any? get() = android.value

    public actual inline fun <reified T> value(): T = decode<T>(value = publicAndroid.value)

    public actual inline fun <T> value(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit): T = decode(strategy, publicAndroid.value, buildSettings)

    public actual fun child(path: String): DataSnapshot = DataSnapshot(android.child(path), persistenceEnabled)
    public actual val hasChildren: Boolean get() = android.hasChildren()
    public actual val children: Iterable<DataSnapshot> get() = android.children.map { DataSnapshot(it, persistenceEnabled) }
}

internal actual class NativeOnDisconnect internal constructor(
    val android: dev.gitlive.firebase.android.database.OnDisconnect,
    val persistenceEnabled: Boolean,
    val database: FirebaseDatabase,
) {

    actual suspend fun removeValue() {
        android.removeValue().await()
    }

    actual suspend fun cancel() {
        android.cancel().await()
    }

    actual suspend fun setEncodedValue(encodedValue: Any?) {
        android.setValue(encodedValue).await()
    }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) {
        android.updateChildren(encodedUpdate.android).await()
    }
}

public val OnDisconnect.android: com.google.firebase.database.OnDisconnect get() = native.android

@Deprecated("Writes no longer depend on the persistence setting, so this accessor is unused; it will be removed in the next major version.")
public val OnDisconnect.persistenceEnabled: Boolean get() = native.persistenceEnabled

@Deprecated("Unused; it will be removed in the next major version.")
public val OnDisconnect.database: FirebaseDatabase get() = native.database

public actual typealias DatabaseException = dev.gitlive.firebase.android.database.DatabaseException
