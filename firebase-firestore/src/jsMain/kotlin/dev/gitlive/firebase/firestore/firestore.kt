/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.promise
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.js.json

actual val Firebase.firestore get() =
    rethrow { dev.gitlive.firebase.firestore; FirebaseFirestore(firebase.firestore()) }

actual fun Firebase.firestore(app: FirebaseApp) =
    rethrow { dev.gitlive.firebase.firestore; FirebaseFirestore(firebase.app().firestore()) }

actual class FirebaseFirestore(val js: firebase.firestore.Firestore) {

//    actual var settings: FirebaseFirestoreSettings
//        get() = js.settings().run { FirebaseFirestoreSettings(js.isPersistenceEnabled) }
//        set(value) {
//            js.settings() = value.run { Builder().setPersistenceEnabled(persistenceEnabled).build() }
//        }

    actual fun collection(collectionPath: String) = rethrow { CollectionReference(js.collection(collectionPath)) }

    actual fun document(documentPath: String) = rethrow { DocumentReference(js.doc(documentPath)) }

    actual fun batch() = rethrow { WriteBatch(js.batch()) }

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        rethrow { firebase.firestore.setLogLevel( if(loggingEnabled) "error" else "silent") }

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        rethrow { js.runTransaction { GlobalScope.promise { Transaction(it).func() } }.await() }

    actual suspend fun clearPersistence() =
        rethrow { js.clearPersistence().await() }
}

actual class WriteBatch(val js: firebase.firestore.WriteBatch) {

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("merge" to merge)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("merge" to merge)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths)) }
            .let { this }

    actual fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) =
        rethrow { js.update(documentRef.js, encode(data, encodeDefaults)!!) }
            .let { this }

    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        rethrow { js.update(documentRef.js, encode(strategy, data, encodeDefaults)!!) }
            .let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) = rethrow {
        js.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.js,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value, true) })
                }.toTypedArray()
            )
    }.let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) = rethrow {
        js.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.js,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value, true) })
                }.toTypedArray()
            )
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        rethrow { js.delete(documentRef.js) }
            .let { this }

    actual suspend fun commit() = rethrow { js.commit().await() }

}

actual class Transaction(val js: firebase.firestore.Transaction) {

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("merge" to merge)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("merge" to merge)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths)) }
            .let { this }

    actual fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) =
        rethrow { js.update(documentRef.js, encode(data, encodeDefaults)!!) }
            .let { this }

    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        rethrow { js.update(documentRef.js, encode(strategy, data, encodeDefaults)!!) }
            .let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) = rethrow {
        js.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.js,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(it, true) })
                }.toTypedArray()
            )
    }.let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) = rethrow {
        js.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.js,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.flatMap { (field, value) ->
                    listOf(field, value?.let { encode(it, true)!! })
                }.toTypedArray()
            )
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        rethrow { js.delete(documentRef.js) }
            .let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        rethrow { DocumentSnapshot(js.get(documentRef.js).await()) }
}

actual class DocumentReference(val js: firebase.firestore.DocumentReference) {

    actual val id: String
        get() = rethrow { js.id }

    actual val path: String
        get() = rethrow { js.path }

    actual suspend fun set(data: Any, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(encode(data, encodeDefaults)!!, json("merge" to merge)).await() }

    actual suspend fun set(data: Any, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(encode(data, encodeDefaults)!!, json("mergeFields" to mergeFields)).await() }

    actual suspend fun set(data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(encode(data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths)).await() }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(encode(strategy, data, encodeDefaults)!!, json("merge" to merge)).await() }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFields)).await() }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths)).await() }

    actual suspend fun update(data: Any, encodeDefaults: Boolean) =
        rethrow { js.update(encode(data, encodeDefaults)!!).await() }

    actual suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        rethrow { js.update(encode(strategy, data, encodeDefaults)!!).await() }

    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) = rethrow {
        js.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(it, true) })
                }.toTypedArray()
            )
            ?.await()
    }.run { Unit }

    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) = rethrow {
        js.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.flatMap { (field, value) ->
                    listOf(field, value?.let { encode(it, true)!! })
                }.toTypedArray()
            )
            ?.await()
    }.run { Unit }

    actual suspend fun delete() = rethrow { js.delete().await() }

    actual suspend fun get() = rethrow { DocumentSnapshot(js.get().await()) }

    actual val snapshots get() = callbackFlow {
        val unsubscribe = js.onSnapshot(
            { offer(DocumentSnapshot(it)) },
            { close(errorToException(it)) }
        )
        awaitClose { unsubscribe() }
    }
}

actual open class Query(open val js: firebase.firestore.Query) {

    actual suspend fun get() =  rethrow { QuerySnapshot(js.get().await()) }

    actual fun limit(limit: Number) = Query(js.limit(limit.toDouble()))

    internal actual fun _where(field: String, equalTo: Any?) = rethrow { Query(js.where(field, "==", equalTo)) }
    internal actual fun _where(path: FieldPath, equalTo: Any?) = rethrow { Query(js.where(path, "==", equalTo)) }

    internal actual fun _where(field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = rethrow {
        Query(
            (lessThan?.let {js.where(field, "<", it) } ?: js).let { js2 ->
                (greaterThan?.let { js2.where(field, ">", it) } ?: js2).let { js3 ->
                    arrayContains?.let { js3.where(field, "array-contains", it) } ?: js3
                }
            }
        )
    }

    internal actual fun _where(path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = rethrow {
        Query(
            (lessThan?.let {js.where(path, "<", it) } ?: js).let { js2 ->
                (greaterThan?.let { js2.where(path, ">", it) } ?: js2).let { js3 ->
                    arrayContains?.let { js3.where(path, "array-contains", it) } ?: js3
                }
            }
        )
    }

    internal actual fun _where(field: String, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { js.where(field, "in", it.toTypedArray()) } ?: js).let { js2 ->
            arrayContainsAny?.let { js2.where(field, "array-contains-any", it.toTypedArray()) } ?: js2
        }
    )

    internal actual fun _where(path: FieldPath, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { js.where(path, "in", it.toTypedArray()) } ?: js).let { js2 ->
            arrayContainsAny?.let { js2.where(path, "array-contains-any", it.toTypedArray()) } ?: js2
        }
    )

    actual val snapshots get() = callbackFlow {
        val unsubscribe = rethrow {
            js.onSnapshot(
                { offer(QuerySnapshot(it)) },
                { close(errorToException(it)) }
            )
        }
        awaitClose { rethrow { unsubscribe() } }
    }
}

actual class CollectionReference(override val js: firebase.firestore.CollectionReference) : Query(js) {

    actual val path: String
        get() =  rethrow { js.path }

    actual suspend fun add(data: Any, encodeDefaults: Boolean) =
        rethrow { DocumentReference(js.add(encode(data, encodeDefaults)!!).await()) }

    actual suspend fun <T> add(data: T, strategy: SerializationStrategy<T>, encodeDefaults: Boolean) =
        rethrow { DocumentReference(js.add(encode(strategy, data, encodeDefaults)!!).await()) }
}

actual class FirebaseFirestoreException(cause: Throwable, val code: FirestoreExceptionCode) : FirebaseException(code.toString(), cause)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual class QuerySnapshot(val js: firebase.firestore.QuerySnapshot) {
    actual val documents
        get() = js.docs.map { DocumentSnapshot(it) }
}

actual class DocumentSnapshot(val js: firebase.firestore.DocumentSnapshot) {

    actual val id get() = rethrow { js.id }
    actual val reference get() = rethrow { DocumentReference(js.ref) }

    actual inline fun <reified T: Any> data(): T =
        rethrow { decode<T>(value = js.data()) }

    actual fun <T> data(strategy: DeserializationStrategy<T>): T =
        rethrow { decode(strategy, js.data()) }

    actual inline fun <reified T> get(field: String) =
        rethrow { decode<T>(value = js.get(field)) }

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>) =
        rethrow { decode(strategy, js.get(field)) }

    actual fun contains(field: String) = rethrow { js.get(field) != undefined }
    actual val exists get() =  rethrow { js.exists }
}

actual typealias FieldPath = Any

actual fun FieldPath(vararg fieldNames: String): FieldPath = rethrow {
    js("Reflect").construct(firebase.firestore.asDynamic().FieldPath, fieldNames).unsafeCast<FieldPath>()
}

actual object FieldValue {
    actual fun delete(): Any = rethrow { firebase.firestore.FieldValue.delete() }
    actual fun arrayUnion(vararg elements: Any): Any = rethrow { firebase.firestore.FieldValue.arrayUnion(*elements) }
    actual fun arrayRemove(vararg elements: Any): Any = rethrow { firebase.firestore.FieldValue.arrayRemove(*elements) }
}

//actual data class FirebaseFirestoreSettings internal constructor(
//    val cacheSizeBytes: Number? = undefined,
//    val host: String? = undefined,
//    val ssl: Boolean? = undefined,
//    var timestampsInSnapshots: Boolean? = undefined,
//    var enablePersistence: Boolean = false
//)

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

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.firestore.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: Throwable) {
        throw errorToException(e)
    }
}

fun errorToException(e: Throwable) = when(e.asDynamic().code as String?) {
    "cancelled" -> FirebaseFirestoreException(e, FirestoreExceptionCode.CANCELLED)
    "invalid-argument" -> FirebaseFirestoreException(e, FirestoreExceptionCode.INVALID_ARGUMENT)
    "deadline-exceeded" -> FirebaseFirestoreException(e, FirestoreExceptionCode.DEADLINE_EXCEEDED)
    "not-found" -> FirebaseFirestoreException(e, FirestoreExceptionCode.NOT_FOUND)
    "already-exists" -> FirebaseFirestoreException(e, FirestoreExceptionCode.ALREADY_EXISTS)
    "permission-denied" -> FirebaseFirestoreException(e, FirestoreExceptionCode.PERMISSION_DENIED)
    "resource-exhausted" -> FirebaseFirestoreException(e, FirestoreExceptionCode.RESOURCE_EXHAUSTED)
    "failed-precondition" -> FirebaseFirestoreException(e, FirestoreExceptionCode.FAILED_PRECONDITION)
    "aborted" -> FirebaseFirestoreException(e, FirestoreExceptionCode.ABORTED)
    "out-of-range" -> FirebaseFirestoreException(e, FirestoreExceptionCode.OUT_OF_RANGE)
    "unimplemented" -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNIMPLEMENTED)
    "internal" -> FirebaseFirestoreException(e, FirestoreExceptionCode.INTERNAL)
    "unavailable" -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNAVAILABLE)
    "data-loss" -> FirebaseFirestoreException(e, FirestoreExceptionCode.DATA_LOSS)
    "unauthenticated" -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNAUTHENTICATED)
//    "unknown" ->
    else -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNKNOWN)
}
