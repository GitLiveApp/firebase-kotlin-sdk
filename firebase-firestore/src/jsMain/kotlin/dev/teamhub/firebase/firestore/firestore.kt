package dev.teamhub.firebase.firestore

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.common.firebase
import dev.teamhub.firebase.common.fromJson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.promise
import kotlin.js.json

fun toJson(data: Any?): Any? = when(data) {
    is firebase.firestore.FieldValue -> data
    else -> dev.teamhub.firebase.common.toJson(data)
}

actual val Firebase.firestore get() =
    rethrow { dev.teamhub.firebase.common.firestore; FirebaseFirestore(firebase.firestore()) }

actual fun Firebase.firestore(app: FirebaseApp) =
    rethrow { dev.teamhub.firebase.common.firestore; FirebaseFirestore(firebase.app().firestore()) }

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
}

actual class WriteBatch(val js: firebase.firestore.WriteBatch) {

    actual fun set(documentRef: DocumentReference, data: Any, merge: Boolean) =
        rethrow { js.set(documentRef.js, toJson(data)!!, json("merge" to merge)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, toJson(data)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath) =
        rethrow { js.set(documentRef.js, toJson(data)!!, json("mergeFields" to mergeFieldsPaths)) }
            .let { this }

    actual fun update(documentRef: DocumentReference, data: Map<String, Any>) =
        rethrow { js.update(documentRef.js, toJson(data)!!) }
            .let { this }

    actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any) =
        rethrow { js.update(documentRef.js, field, toJson(value)!!, *moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any)!! }.toTypedArray()) }
            .let { this }

    actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) =
        rethrow { js.update(documentRef.js, fieldPath, toJson(value)!!, *moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any)!! }.toTypedArray()) }
            .let { this }

    actual fun delete(documentRef: DocumentReference) =
        rethrow { js.delete(documentRef.js) }
            .let { this }

    actual suspend fun commit() = rethrow { js.commit().await() }

}

actual class Transaction(val js: firebase.firestore.Transaction) {

    actual fun set(documentRef: DocumentReference, data: Any, merge: Boolean) =
        rethrow { js.set(documentRef.js, toJson(data)!!, json("merge" to merge)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, toJson(data)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath) =
        rethrow { js.set(documentRef.js, toJson(data)!!, json("mergeFields" to mergeFieldsPaths)) }
            .let { this }

    actual fun update(documentRef: DocumentReference, data: Map<String, Any>) =
        rethrow { js.update(documentRef.js, toJson(data)!!) }
            .let { this }

    actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any) =
        rethrow { js.update(documentRef.js, field, toJson(value)!!, *moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any)!! }.toTypedArray()) }
            .let { this }

    actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) =
        rethrow { js.update(documentRef.js, fieldPath, toJson(value)!!, *moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any)!! }.toTypedArray()) }
            .let { this }

    actual fun delete(documentRef: DocumentReference) =
        rethrow { js.delete(documentRef.js) }
            .let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        rethrow { DocumentSnapshot(js.get(documentRef.js).await()) }
}

actual class DocumentReference(val js: firebase.firestore.DocumentReference) {

    actual val id: String
        get() =  rethrow { js.id }

    actual suspend fun set(data: Any, merge: Boolean) =
        rethrow { js.set(toJson(data)!!, json("merge" to merge)).await() }

    actual suspend fun set(data: Any, vararg mergeFields: String) =
        rethrow { js.set(toJson(data)!!, json("mergeFields" to mergeFields)).await() }

    actual suspend fun set(data: Any, vararg mergeFieldsPaths: FieldPath) =
        rethrow { js.set(toJson(data)!!, json("mergeFields" to mergeFieldsPaths)).await() }

    actual suspend fun update(data: Map<String, Any>) =
        rethrow { js.update(toJson(data)!!).await() }

    actual suspend fun update(field: String, value: Any?, vararg moreFieldsAndValues: Any) =
        rethrow { js.update(field, toJson(value)!!, *moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any)!! }.toTypedArray()).await() }

    actual suspend fun update(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) =
        rethrow { js.update(fieldPath, toJson(value)!!, *moreFieldsAndValues.mapIndexed { index, any -> if(index%2 == 0) any else toJson(any)!! }.toTypedArray()).await() }

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
    actual fun whereEqualTo(field: String, value: Any?) = rethrow { Query(js.where(field, "==", value)) }
    actual fun whereEqualTo(path: FieldPath, value: Any?) = rethrow { Query(js.where(path, "==", value)) }
    actual fun whereLessThan(field: String, value: Any) = rethrow { Query(js.where(field, "<", value)) }
    actual fun whereLessThan(path: FieldPath, value: Any) = rethrow { Query(js.where(path, "<", value)) }
    actual fun whereGreaterThan(field: String, value: Any) = rethrow { Query(js.where(field, ">", value)) }
    actual fun whereGreaterThan(path: FieldPath, value: Any) = rethrow { Query(js.where(path, ">", value)) }
    actual fun whereArrayContains(field: String, value: Any) = rethrow { Query(js.where(field, "array-contains", value)) }
    actual fun whereArrayContains(path: FieldPath, value: Any) = rethrow { Query(js.where(path, "array-contains", value)) }

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
    actual suspend fun add(data: Map<String, Any>) = rethrow { DocumentReference(js.add(toJson(data)!!).await()) }
    actual suspend fun add(pojo: Any) = rethrow { DocumentReference(js.add(toJson(pojo)!!).await()) }
}

actual class FirebaseFirestoreException(message: String?, code: FirestoreExceptionCode) : FirebaseException(code.toString(), message)

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual class QuerySnapshot(val js: firebase.firestore.QuerySnapshot) {
    actual val documents
        get() = js.docs.map { DocumentSnapshot(it) }
}

actual class DocumentSnapshot(val js: firebase.firestore.DocumentSnapshot) {
    actual val id get() = rethrow { js.id }
    actual val reference get() = rethrow { DocumentReference(js.ref) }
    actual inline fun <reified T> toObject() = rethrow { fromJson(js.data(), T::class) as T? }
    actual inline fun <reified T> get(field: String) = rethrow { fromJson(js.get(field), T::class) as T? }
    actual fun contains(field: String) = rethrow { js.get(field) != undefined }
    actual val exists get() =  rethrow { js.exists }
}

actual typealias FieldPath = firebase.firestore.FieldPath

actual fun FieldPath(vararg fieldNames: String) = rethrow { FieldPath(fieldNames) }

actual typealias FieldValueImpl = firebase.firestore.FieldValue

actual object FieldValue {
    actual fun delete() = rethrow { FieldValueImpl.delete() }
    actual fun arrayUnion(vararg elements: Any) = rethrow { FieldValueImpl.arrayUnion(*elements) }
    actual fun arrayRemove(vararg elements: Any) = rethrow { FieldValueImpl.arrayRemove(*elements) }
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

actual annotation class IgnoreExtraProperties

actual annotation class Exclude

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.teamhub.firebase.firestore.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: Throwable) {
        throw errorToException(e)
    }
}

fun errorToException(e: Throwable) = when(val code = e.asDynamic().code as String?) {
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