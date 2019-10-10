package dev.teamhub.firebase.firestore

import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.common.fromJson
import dev.teamhub.firebase.common.toJson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.js.json
import kotlin.reflect.KClass

internal val firestore = js("require('firebase/firestore')")

actual fun getFirebaseFirestore() = rethrow { firebase.firestore() }

actual typealias FirebaseFirestore = firebase.firestore.Firestore
actual typealias QuerySnapshot = firebase.firestore.QuerySnapshot
actual typealias DocumentSnapshot = firebase.firestore.DocumentSnapshot
actual typealias Query = firebase.firestore.Query
actual typealias DocumentReference = firebase.firestore.DocumentReference
actual typealias WriteBatch = firebase.firestore.WriteBatch
actual typealias Transaction = firebase.firestore.Transaction
actual typealias SetOptions = NewOptions
actual typealias CollectionReference = firebase.firestore.CollectionReference
actual typealias FieldPath = firebase.firestore.FieldPath
actual typealias FieldValue = firebase.firestore.FieldValue

actual data class FirebaseFirestoreSettings internal constructor(
    val cacheSizeBytes: Number? = undefined,
    val host: String? = undefined,
    val ssl: Boolean? = undefined,
    var timestampsInSnapshots: Boolean? = undefined,
    var enablePersistence: Boolean = false
)

actual class FirebaseFirestoreSettingsBuilder actual constructor(internal var settings: FirebaseFirestoreSettings) {
    actual constructor() : this(FirebaseFirestoreSettings())
}

actual fun FirebaseFirestoreSettingsBuilder.setPersistenceEnabled(enabled: Boolean) = rethrow {
    settings.copy( enablePersistence = enabled ).let { settings = it }.let{ this }
}

actual fun FirebaseFirestoreSettingsBuilder.setTimestampsInSnapshotsEnabled(enabled: Boolean) = rethrow {
    settings.copy( timestampsInSnapshots = enabled ).let { settings = it }.let { this }
}

actual fun FirebaseFirestoreSettingsBuilder.build() = rethrow { settings }


actual fun FirebaseFirestore.getFirestoreSettings() = rethrow { _th_settings ?: FirebaseFirestoreSettings() }

actual fun FirebaseFirestore.setFirestoreSettings(settings: FirebaseFirestoreSettings) = rethrow {
    _th_settings = settings
    settings(json(
            "cacheSizeBytes" to settings.cacheSizeBytes,
            "host" to settings.host,
            "ssl" to settings.ssl,
            "timestampsInSnapshots" to settings.timestampsInSnapshots
    ))
    if(settings.enablePersistence) enablePersistence()
}

actual class FirebaseFirestoreException(message: String?, code: FirestoreExceptionCode) : FirebaseException(code.toString(), message)

actual val QuerySnapshot.documents: List<DocumentSnapshot>
    get() = rethrow { docs.toList() }

@Suppress("UNCHECKED_CAST")
actual fun <T : Any> DocumentSnapshot.toObject(valueType: KClass<T>): T = fromJson(data(), valueType)  as T

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val DocumentSnapshot.id: String
    get() = rethrow { id }

actual val DocumentSnapshot.reference: DocumentReference
    get() = rethrow { ref }

actual interface ListenerRegistration


actual interface EventListener<T> {
    actual fun onEvent(snapshot: T?, exception: FirebaseFirestoreException?)
}

actual fun fieldPathOf(vararg fieldNames: String) = rethrow { FieldPath(fieldNames) }

actual fun Query.addSnapshotListener(listener: (snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) -> Unit) = rethrow {
    onSnapshot(
            { listener(it, undefined) },
            { listener(undefined, FirebaseFirestoreException(it.message as String, FirestoreExceptionCode.UNKNOWN)) }
    )
            .also { it.asDynamic().remove = { it() } }
            .asDynamic()
}

actual fun Query.whereEqualTo(field: String, value: Any?) = rethrow { where(field, "==", value) }

actual fun Query.whereEqualTo(path: FieldPath, value: Any?) = rethrow { where(path, "==", value) }

actual fun Query.whereLessThan(field: String, value: Any) = rethrow { where(field, "<", value) }

actual fun Query.whereLessThan(path: FieldPath, value: Any) = rethrow { where(path, "<", value) }

actual fun Query.whereGreaterThan(field: String, value: Any) = rethrow { where(field, ">", value) }

actual fun Query.whereGreaterThan(path: FieldPath, value: Any) = rethrow { where(path, ">", value) }

actual fun Query.whereArrayContains(field: String, value: Any) = rethrow { where(field, "array-contains", value) }

actual fun Query.whereArrayContains(path: FieldPath, value: Any) = rethrow { where(path, "array-contains", value) }

actual fun Query.addSnapshotListener(listener: EventListener<QuerySnapshot>) = rethrow {
    onSnapshot(
            { listener.onEvent(snapshot = it, exception = undefined) },
            { listener.onEvent(snapshot = undefined, exception = FirebaseFirestoreException(it.message as String, FirestoreExceptionCode.UNKNOWN)) })
            .also { it.asDynamic().remove = { it() } }
            .asDynamic()
}


class NewOptions {
    val merge: Boolean = true
}

actual fun mergeSetOptions() = rethrow { NewOptions() }

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val DocumentReference.id: String
    get() = rethrow { id }

actual fun DocumentReference.addSnapshotListener(listener: (snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) -> Unit) = rethrow {
    onSnapshot({ listener(it, undefined) }, { listener(undefined, FirebaseFirestoreException(it.message as String, FirestoreExceptionCode.UNKNOWN)) })
            .also { it.asDynamic().remove = { it() } }
            .asDynamic()
}

actual val FirebaseFirestoreException.code: FirestoreExceptionCode
    get() = code

actual enum class FirestoreExceptionCode {
    OK,
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED
}

actual suspend fun <T> FirebaseFirestore.awaitRunTransaction(func: suspend (transaction: Transaction) -> T) =
        rethrow { runTransaction { GlobalScope.promise { func(it) } }.await() }


actual suspend fun WriteBatch.awaitCommit() = rethrow { commit().await() }

actual suspend fun Query.awaitGet() = rethrow { get().await() }

actual suspend fun DocumentReference.awaitGet() = rethrow { get().await() }

actual suspend fun DocumentReference.awaitSet(data: Map<String, Any>) = rethrow {  set(toJson(data)!!).await() }

actual suspend fun DocumentReference.awaitSet(pojo: Any) = rethrow {  set(toJson(pojo)!!).await() }

actual suspend fun DocumentReference.awaitSet(data: Map<String, Any>, options: SetOptions) = rethrow { set(toJson(data)!!, options).await() }

actual suspend fun DocumentReference.awaitSet(pojo: Any, options: SetOptions) = rethrow { set(toJson(pojo)!!, options).await() }

actual suspend fun DocumentReference.awaitUpdate(data: Map<String, Any>) = rethrow { update(toJson(data)!!).await() }

actual suspend fun DocumentReference.awaitDelete() = rethrow { delete().await() }

actual suspend fun CollectionReference.awaitAdd(data: Map<String, Any>) = rethrow { add(toJson(data)!!).await() }

actual suspend fun CollectionReference.awaitAdd(pojo: Any) = rethrow { add(toJson(pojo)!!).await() }

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestore.collection(collectionPath: String) = rethrow { collection(collectionPath) }

actual fun FirebaseFirestore.document(documentPath: String) = rethrow { doc(documentPath) }

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestore.batch() = rethrow { batch() }

actual fun FirebaseFirestore.setLoggingEnabled(loggingEnabled: Boolean) = rethrow { firebase.firestore.setLogLevel( if(loggingEnabled) "error" else "silent") }

actual fun Transaction.set(documentRef: DocumentReference, data: Map<String, Any>) = rethrow { asDynamic().set(documentRef, toJson(data)!!).unsafeCast<Transaction>() }

actual fun Transaction.set(documentRef: DocumentReference, data: Map<String, Any>, options: SetOptions) = rethrow { asDynamic().set(documentRef, toJson(data)!!, options).unsafeCast<Transaction>() }

actual fun Transaction.set(documentRef: DocumentReference, pojo: Any) = rethrow { asDynamic().set(documentRef, toJson(pojo)!!).unsafeCast<Transaction>() }

actual fun Transaction.set(documentRef: DocumentReference, pojo: Any, options: SetOptions) = rethrow { asDynamic().set(documentRef, toJson(pojo)!!, options).unsafeCast<Transaction>() }

actual fun Transaction.update(documentRef: DocumentReference, data: Map<String, Any>) = rethrow { asDynamic().update(documentRef, toJson(data)).unsafeCast<Transaction>() }

actual fun Transaction.update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any) = rethrow { asDynamic().update.apply(this, arrayOf(documentRef, field, toJson(value)) + moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any) }).unsafeCast<Transaction>() }

actual fun Transaction.update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) = rethrow { asDynamic().update.apply(this, arrayOf(documentRef, fieldPath, toJson(value)) + moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any) }).unsafeCast<Transaction>() }


actual fun Transaction.delete(documentRef: DocumentReference) = rethrow { asDynamic().delete(documentRef).unsafeCast<Transaction>() }

actual suspend fun Transaction.awaitGet(documentRef: DocumentReference) = rethrow { get(documentRef).await() }


actual fun WriteBatch.set(documentRef: DocumentReference, data: Map<String, Any>) = rethrow { asDynamic().set(documentRef, toJson(data)).unsafeCast<WriteBatch>() }

actual fun WriteBatch.set(documentRef: DocumentReference, data: Map<String, Any>, options: SetOptions) = rethrow { asDynamic().set(documentRef, toJson(data), options).unsafeCast<WriteBatch>() }

actual fun WriteBatch.set(documentRef: DocumentReference, pojo: Any) = rethrow { asDynamic().set(documentRef, toJson(pojo)!!).unsafeCast<WriteBatch>() }

actual fun WriteBatch.set(documentRef: DocumentReference, pojo: Any, options: SetOptions) = rethrow { asDynamic().set(documentRef, toJson(pojo), options).unsafeCast<WriteBatch>() }

actual fun WriteBatch.update(documentRef: DocumentReference, data: Map<String, Any>) = rethrow { asDynamic().update(documentRef, toJson(data)).unsafeCast<WriteBatch>() }

actual fun WriteBatch.update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any) = rethrow { asDynamic().update.apply(this, arrayOf(documentRef, field, toJson(value)) + moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any) }).unsafeCast<WriteBatch>() }

actual fun WriteBatch.update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) = rethrow { asDynamic().update.apply(this, arrayOf(documentRef, fieldPath, toJson(value)) + moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any) }).unsafeCast<WriteBatch>() }

actual fun WriteBatch.delete(documentRef: DocumentReference) = rethrow { asDynamic().delete(documentRef).unsafeCast<WriteBatch>() }

actual fun DocumentReference.addSnapshotListener(listener: EventListener<DocumentSnapshot>) = rethrow {
    onSnapshot(
            { listener.onEvent(snapshot = it, exception = undefined) },
            { listener.onEvent(snapshot = undefined, exception = FirebaseFirestoreException(it.message as String, FirestoreExceptionCode.UNKNOWN)) })
            .also { it.asDynamic().remove = { it() } }
            .asDynamic()
}

actual fun DocumentSnapshot.get(field: String) = rethrow { get(field) }

actual fun DocumentSnapshot.getString(field: String) = rethrow { get(field) as String? }

actual fun DocumentSnapshot.contains(field: String) = rethrow { get(field) != undefined }

actual fun ListenerRegistration.remove() {
    TODO("Not implemented")
}


actual annotation class IgnoreExtraProperties

actual annotation class Exclude

actual fun deleteFieldValue() = rethrow { FieldValue.delete() }

actual fun DocumentSnapshot.exists() = rethrow { exists }

actual fun arrayUnionFieldValue(vararg elements: Any) = rethrow { FieldValue.arrayUnion(elements) }

actual fun arrayRemoveFieldValue(vararg elements: Any) = rethrow { FieldValue.arrayRemove(elements) }

actual suspend fun DocumentReference.awaitUpdate(field: String, value: Any?, vararg moreFieldsAndValues: Any) = rethrow { asDynamic().update.apply(this, arrayOf(field, toJson(value)) + moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any) }).unsafeCast<Promise<Unit>>().await() }

actual suspend fun DocumentReference.awaitUpdate(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) = rethrow {  asDynamic().update.apply(this, arrayOf(fieldPath, toJson(value)) + moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any) }).unsafeCast<Promise<Unit>>().await() }

private inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.teamhub.firebase.firestore.rethrow { function() }

private inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: Throwable) {
        throw errorToException(e)
    }
}

private fun errorToException(e: Throwable) = when(val code = e.asDynamic().code as String?) {
    "cancelled" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.CANCELLED)
    "invalid-argument" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.INVALID_ARGUMENT)
    "deadline-exceeded" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.DEADLINE_EXCEEDED)
    "not-found" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.NOT_FOUND)
    "already-exists" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.ALREADY_EXISTS)
    "permission-denied" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.PERMISSION_DENIED)
    "resource-exhausted" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.RESOURCE_EXHAUSTED)
    "failed-precondition" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.FAILED_PRECONDITION)
    "aborted" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.ABORTED)
    "out-of-range" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.OUT_OF_RANGE)
    "unimplemented" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.UNIMPLEMENTED)
    "internal" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.INTERNAL)
    "unavailable" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.UNAVAILABLE)
    "data-loss" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.DATA_LOSS)
    "unauthenticated" -> FirebaseFirestoreException(e.message, FirestoreExceptionCode.UNAUTHENTICATED)
//    "unknown" ->
    else -> FirebaseFirestoreException(code, FirestoreExceptionCode.UNKNOWN)
}