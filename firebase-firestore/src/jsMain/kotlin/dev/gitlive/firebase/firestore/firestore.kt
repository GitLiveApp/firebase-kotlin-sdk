/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import dev.gitlive.firebase.firestore.externals.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.promise
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.js.json
import dev.gitlive.firebase.firestore.externals.CollectionReference as JsCollectionReference
import dev.gitlive.firebase.firestore.externals.DocumentChange as JsDocumentChange
import dev.gitlive.firebase.firestore.externals.DocumentReference as JsDocumentReference
import dev.gitlive.firebase.firestore.externals.DocumentSnapshot as JsDocumentSnapshot
import dev.gitlive.firebase.firestore.externals.FieldPath as JsFieldPath
import dev.gitlive.firebase.firestore.externals.Query as JsQuery
import dev.gitlive.firebase.firestore.externals.QuerySnapshot as JsQuerySnapshot
import dev.gitlive.firebase.firestore.externals.SnapshotMetadata as JsSnapshotMetadata
import dev.gitlive.firebase.firestore.externals.Transaction as JsTransaction
import dev.gitlive.firebase.firestore.externals.WriteBatch as JsWriteBatch
import dev.gitlive.firebase.firestore.externals.arrayRemove as jsArrayRemove
import dev.gitlive.firebase.firestore.externals.arrayUnion as jsArrayUnion
import dev.gitlive.firebase.firestore.externals.endAt as jsEndAt
import dev.gitlive.firebase.firestore.externals.endBefore as jsEndBefore
import dev.gitlive.firebase.firestore.externals.increment as jsIncrement
import dev.gitlive.firebase.firestore.externals.limit as jsLimit
import dev.gitlive.firebase.firestore.externals.startAfter as jsStartAfter
import dev.gitlive.firebase.firestore.externals.startAt as jsStartAt
import dev.gitlive.firebase.firestore.externals.updateDoc as jsUpdate
import dev.gitlive.firebase.firestore.externals.where as jsWhere

actual val Firebase.firestore get() =
    rethrow { FirebaseFirestore(getFirestore()) }

actual fun Firebase.firestore(app: FirebaseApp) =
    rethrow { FirebaseFirestore(getFirestore(app.js)) }

/** Helper method to perform an update operation. */
private fun <R> performUpdate(
    fieldsAndValues: Array<out Pair<String, Any?>>,
    update: (String, Any?, Array<Any?>) -> R
) = performUpdate(fieldsAndValues, { it }, { encode(it, true) }, update)

/** Helper method to perform an update operation. */
private fun <R> performUpdate(
    fieldsAndValues: Array<out Pair<FieldPath, Any?>>,
    update: (dev.gitlive.firebase.firestore.externals.FieldPath, Any?, Array<Any?>) -> R
) = performUpdate(fieldsAndValues, { it.js }, { encode(it, true) }, update)

actual class FirebaseFirestore(jsFirestore: Firestore) {

    var js: Firestore = jsFirestore
        private set

    actual fun collection(collectionPath: String) = rethrow { CollectionReference(collection(js, collectionPath)) }

    actual fun collectionGroup(collectionId: String) = rethrow { Query(collectionGroup(js, collectionId)) }

    actual fun document(documentPath: String) = rethrow { DocumentReference(doc(js, documentPath)) }

    actual fun batch() = rethrow { WriteBatch(writeBatch(js)) }

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        rethrow { setLogLevel( if(loggingEnabled) "error" else "silent") }

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        rethrow { runTransaction(js, { GlobalScope.promise { Transaction(it).func() } } ).await() }

    actual suspend fun clearPersistence() =
        rethrow { clearIndexedDbPersistence(js).await() }

    actual fun useEmulator(host: String, port: Int) = rethrow { connectFirestoreEmulator(js, host, port) }

    actual fun setSettings(persistenceEnabled: Boolean?, sslEnabled: Boolean?, host: String?, cacheSizeBytes: Long?) {
        if(persistenceEnabled == true) enableIndexedDbPersistence(js)

        val settings = json().apply {
            sslEnabled?.let { set("ssl", it) }
            host?.let { set("host", it) }
            cacheSizeBytes?.let { set("cacheSizeBytes", it) }
        }
        js = initializeFirestore(js.app, settings)
    }

    actual suspend fun disableNetwork() {
        rethrow { disableNetwork(js).await() }
    }

    actual suspend fun enableNetwork() {
        rethrow { enableNetwork(js).await() }
    }
}

actual class WriteBatch(val js: JsWriteBatch) {

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("merge" to merge)) }
            .let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths.map { it.js }.toTypedArray())) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("merge" to merge)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths.map { it.js }.toTypedArray())) }
            .let { this }

    actual inline fun <reified T> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean) =
        rethrow { js.update(documentRef.js, encode(data, encodeDefaults)!!) }
            .let { this }

    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        rethrow { js.update(documentRef.js, encode(strategy, data, encodeDefaults)!!) }
            .let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) = rethrow {
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            js.update(documentRef.js, field, value, *moreFieldsAndValues)
        }
    }.let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) = rethrow {
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            js.update(documentRef.js, field, value, *moreFieldsAndValues)
        }
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        rethrow { js.delete(documentRef.js) }
            .let { this }

    actual suspend fun commit() = rethrow { js.commit().await() }

}

actual class Transaction(val js: JsTransaction) {

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("merge" to merge)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(documentRef.js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths.map { it.js }.toTypedArray())) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("merge" to merge)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFields)) }
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { js.set(documentRef.js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths.map { it.js }.toTypedArray())) }
            .let { this }

    actual fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean) =
        rethrow { js.update(documentRef.js, encode(data, encodeDefaults)!!) }
            .let { this }

    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        rethrow { js.update(documentRef.js, encode(strategy, data, encodeDefaults)!!) }
            .let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) = rethrow {
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            js.update(documentRef.js, field, value, *moreFieldsAndValues)
        }
    }.let { this }

    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) = rethrow {
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            js.update(documentRef.js, field, value, *moreFieldsAndValues)
        }
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        rethrow { js.delete(documentRef.js) }
            .let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        rethrow { DocumentSnapshot(js.get(documentRef.js).await()) }
}

actual class DocumentReference(val js: JsDocumentReference) {

    actual val id: String
        get() = rethrow { js.id }

    actual val path: String
        get() = rethrow { js.path }

    actual val parent: CollectionReference
        get() = rethrow { CollectionReference(js.parent) }

    actual fun collection(collectionPath: String) = rethrow { CollectionReference(collection(js, collectionPath)) }

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { setDoc(js, encode(data, encodeDefaults)!!, json("merge" to merge)).await() }

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { setDoc(js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFields)).await() }

    actual suspend inline fun <reified T> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { setDoc(js, encode(data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths.map { it.js }.toTypedArray())).await() }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
        rethrow { setDoc(js, encode(strategy, data, encodeDefaults)!!, json("merge" to merge)).await() }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
        rethrow { setDoc(js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFields)).await() }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
        rethrow { setDoc(js, encode(strategy, data, encodeDefaults)!!, json("mergeFields" to mergeFieldPaths.map { it.js }.toTypedArray())).await() }

    actual suspend inline fun <reified T> update(data: T, encodeDefaults: Boolean) =
        rethrow { jsUpdate(js, encode(data, encodeDefaults)!!).await() }

    actual suspend fun <T> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        rethrow { jsUpdate(js, encode(strategy, data, encodeDefaults)!!).await() }

    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) = rethrow {
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            jsUpdate(js, field, value, *moreFieldsAndValues)
        }?.await()
    }.run { Unit }

    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) = rethrow {
        performUpdate(fieldsAndValues) { field, value, moreFieldsAndValues ->
            jsUpdate(js, field, value, *moreFieldsAndValues)
        }?.await()
    }.run { Unit }

    actual suspend fun delete() = rethrow { deleteDoc(js).await() }

    actual suspend fun get() = rethrow { DocumentSnapshot(getDoc(js).await()) }

    actual val snapshots get() = callbackFlow<DocumentSnapshot> {
        val unsubscribe = onSnapshot(
            js,
            { trySend(DocumentSnapshot(it)) },
            { close(errorToException(it)) }
        )
        awaitClose { unsubscribe() }
    }
}

actual open class Query(open val js: JsQuery) {

    actual suspend fun get() =  rethrow { QuerySnapshot(getDocs(js).await()) }

    actual fun limit(limit: Number) = Query(query(js, jsLimit(limit)))

    internal actual fun _where(field: String, equalTo: Any?) = rethrow { Query(query(js, jsWhere(field, "==", equalTo))) }
    internal actual fun _where(path: FieldPath, equalTo: Any?) = rethrow { Query(query(js, jsWhere(path.js, "==", equalTo))) }

    internal actual fun _where(field: String, equalTo: DocumentReference) = rethrow { Query(query(js, jsWhere(field, "==", equalTo.js))) }
    internal actual fun _where(path: FieldPath, equalTo: DocumentReference) = rethrow { Query(query(js, jsWhere(path.js, "==", equalTo.js))) }

    internal actual fun _where(field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = rethrow {
        Query(
            (lessThan?.let { query(js, jsWhere(field, "<", it)) } ?: js).let { js2 ->
                (greaterThan?.let { query(js2, jsWhere(field, ">", it)) } ?: js2).let { js3 ->
                    arrayContains?.let { query(js3, jsWhere(field, "array-contains", it)) } ?: js3
                }
            }
        )
    }

    internal actual fun _where(path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = rethrow {
        Query(
            (lessThan?.let { query(js, jsWhere(path.js, "<", it)) } ?: js).let { js2 ->
                (greaterThan?.let { query(js2, jsWhere(path.js, ">", it)) } ?: js2).let { js3 ->
                    arrayContains?.let { query(js3, jsWhere(path.js, "array-contains", it)) } ?: js3
                }
            }
        )
    }

    internal actual fun _where(field: String, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { query(js, jsWhere(field, "in", it.toTypedArray())) } ?: js).let { js2 ->
            arrayContainsAny?.let { query(js2, jsWhere(field, "array-contains-any", it.toTypedArray())) } ?: js2
        }
    )

    internal actual fun _where(path: FieldPath, inArray: List<Any>?, arrayContainsAny: List<Any>?) = Query(
        (inArray?.let { query(js, jsWhere(path.js, "in", it.toTypedArray())) } ?: js).let { js2 ->
            arrayContainsAny?.let { query(js2, jsWhere(path.js, "array-contains-any", it.toTypedArray())) } ?: js2
        }
    )

    internal actual fun _orderBy(field: String, direction: Direction) = rethrow {
        Query(query(js, orderBy(field, direction.jsString)))
    }

    internal actual fun _orderBy(field: FieldPath, direction: Direction) = rethrow {
        Query(query(js, orderBy(field.js, direction.jsString)))
    }

    internal actual fun _startAfter(document: DocumentSnapshot) = rethrow { Query(query(js, jsStartAfter(document.js))) }

    internal actual fun _startAfter(vararg fieldValues: Any) = rethrow { Query(query(js, jsStartAfter(*fieldValues))) }

    internal actual fun _startAt(document: DocumentSnapshot) = rethrow { Query(query(js, jsStartAt(document.js))) }

    internal actual fun _startAt(vararg fieldValues: Any) = rethrow { Query(query(js, jsStartAt(*fieldValues))) }

    internal actual fun _endBefore(document: DocumentSnapshot) = rethrow { Query(query(js, jsEndBefore(document.js))) }

    internal actual fun _endBefore(vararg fieldValues: Any) = rethrow { Query(query(js, jsEndBefore(*fieldValues))) }

    internal actual fun _endAt(document: DocumentSnapshot) = rethrow { Query(query(js, jsEndAt(document.js))) }

    internal actual fun _endAt(vararg fieldValues: Any) = rethrow { Query(query(js, jsEndAt(*fieldValues))) }

    actual val snapshots get() = callbackFlow<QuerySnapshot> {
        val unsubscribe = rethrow {
            onSnapshot(
                js,
                { trySend(QuerySnapshot(it)) },
                { close(errorToException(it)) }
            )
        }
        awaitClose { rethrow { unsubscribe() } }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow<QuerySnapshot> {
        val unsubscribe = rethrow {
            onSnapshot(
                js,
                json("includeMetadataChanges" to includeMetadataChanges),
                { trySend(QuerySnapshot(it)) },
                { close(errorToException(it)) }
            )
        }
        awaitClose { rethrow { unsubscribe() } }
    }
}

actual class CollectionReference(override val js: JsCollectionReference) : Query(js) {

    actual val path: String
        get() =  rethrow { js.path }

    actual val document get() = rethrow { DocumentReference(doc(js)) }

    actual val parent get() = rethrow { js.parent?.let{DocumentReference(it)} }

    actual fun document(documentPath: String) = rethrow { DocumentReference(doc(js, documentPath)) }

    actual suspend inline fun <reified T> add(data: T, encodeDefaults: Boolean) =
        rethrow { DocumentReference(addDoc(js, encode(data, encodeDefaults)!!).await()) }

    actual suspend fun <T> add(data: T, strategy: SerializationStrategy<T>, encodeDefaults: Boolean) =
        rethrow { DocumentReference(addDoc(js, encode(strategy, data, encodeDefaults)!!).await()) }
    actual suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        rethrow { DocumentReference(addDoc(js, encode(strategy, data, encodeDefaults)!!).await()) }
}

actual class FirebaseFirestoreException(cause: Throwable, val code: FirestoreExceptionCode) : FirebaseException(code.toString(), cause)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual class QuerySnapshot(val js: JsQuerySnapshot) {
    actual val documents
        get() = js.docs.map { DocumentSnapshot(it) }
    actual val documentChanges
        get() = js.docChanges().map { DocumentChange(it) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(js.metadata)
}

actual class DocumentChange(val js: JsDocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(js.doc)
    actual val newIndex: Int
        get() = js.newIndex
    actual val oldIndex: Int
        get() = js.oldIndex
    actual val type: ChangeType
        get() = ChangeType.values().first { it.jsString == js.type }
}

actual class DocumentSnapshot(val js: JsDocumentSnapshot) {

    actual val id get() = rethrow { js.id }
    actual val reference get() = rethrow { DocumentReference(js.ref) }

    actual inline fun <reified T : Any> data(serverTimestampBehavior: ServerTimestampBehavior): T =
        rethrow { decode(value = js.data(getTimestampsOptions(serverTimestampBehavior))) }

    actual fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior): T =
        rethrow { decode(strategy, js.data(getTimestampsOptions(serverTimestampBehavior))) }

    actual inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior) =
        rethrow { decode<T>(value = js.get(field, getTimestampsOptions(serverTimestampBehavior))) }

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior) =
        rethrow { decode(strategy, js.get(field, getTimestampsOptions(serverTimestampBehavior))) }

    actual fun contains(field: String) = rethrow { js.get(field) != undefined }
    actual val exists get() = rethrow { js.exists() }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(js.metadata)

    fun getTimestampsOptions(serverTimestampBehavior: ServerTimestampBehavior) =
        json("serverTimestamps" to serverTimestampBehavior.name.lowercase())
}

actual class SnapshotMetadata(val js: JsSnapshotMetadata) {
    actual val hasPendingWrites: Boolean get() = js.hasPendingWrites
    actual val isFromCache: Boolean get() = js.fromCache
}

actual class FieldPath private constructor(val js: JsFieldPath) {
    actual constructor(vararg fieldNames: String) : this(dev.gitlive.firebase.firestore.rethrow {
        js("Reflect").construct(JsFieldPath, fieldNames).unsafeCast<JsFieldPath>()
    })
    actual val documentId: FieldPath get() = FieldPath(JsFieldPath.documentId)
}

/** Represents a platform specific Firebase FieldValue. */
private typealias NativeFieldValue = dev.gitlive.firebase.firestore.externals.FieldValue

/** Represents a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
actual class FieldValue internal actual constructor(internal actual val nativeValue: Any) {
    init {
        require(nativeValue is NativeFieldValue)
    }
    override fun equals(other: Any?): Boolean =
        this === other || other is FieldValue &&
                (nativeValue as NativeFieldValue).isEqual(other.nativeValue as NativeFieldValue)
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    actual companion object {
        actual val serverTimestamp: FieldValue get() = rethrow { FieldValue(serverTimestamp()) }
        actual val delete: FieldValue get() = rethrow { FieldValue(deleteField()) }
        actual fun increment(value: Int): FieldValue = rethrow { FieldValue(jsIncrement(value)) }
        actual fun arrayUnion(vararg elements: Any): FieldValue = rethrow { FieldValue(jsArrayUnion(*elements)) }
        actual fun arrayRemove(vararg elements: Any): FieldValue = rethrow { FieldValue(jsArrayRemove(*elements)) }
    }
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

actual enum class Direction(internal val jsString : String) {
    ASCENDING("asc"),
    DESCENDING("desc");
}

actual enum class ChangeType(internal val jsString : String) {
    ADDED("added"),
    MODIFIED("modified"),
    REMOVED("removed");
}

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.firestore.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: dynamic) {
        throw errorToException(e)
    }
}

fun errorToException(e: dynamic) = (e?.code ?: e?.message ?: "")
    .toString()
    .lowercase()
    .let {
        when {
            "cancelled" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.CANCELLED)
            "invalid-argument" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.INVALID_ARGUMENT)
            "deadline-exceeded" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.DEADLINE_EXCEEDED)
            "not-found" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.NOT_FOUND)
            "already-exists" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.ALREADY_EXISTS)
            "permission-denied" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.PERMISSION_DENIED)
            "resource-exhausted" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.RESOURCE_EXHAUSTED)
            "failed-precondition" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.FAILED_PRECONDITION)
            "aborted" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.ABORTED)
            "out-of-range" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.OUT_OF_RANGE)
            "unimplemented" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNIMPLEMENTED)
            "internal" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.INTERNAL)
            "unavailable" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNAVAILABLE)
            "data-loss" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.DATA_LOSS)
            "unauthenticated" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNAUTHENTICATED)
            "unknown" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNKNOWN)
            else -> {
                println("Unknown error code in ${JSON.stringify(e)}")
                FirebaseFirestoreException(e, FirestoreExceptionCode.UNKNOWN)
            }
        }
}

// from: https://discuss.kotlinlang.org/t/how-to-access-native-js-object-as-a-map-string-any/509/8
fun entriesOf(jsObject: dynamic): List<Pair<String, Any?>> =
    (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
        .invoke(jsObject)
        .map { entry -> entry[0] as String to entry[1] }

// from: https://discuss.kotlinlang.org/t/how-to-access-native-js-object-as-a-map-string-any/509/8
fun mapOf(jsObject: dynamic): Map<String, Any?> =
    entriesOf(jsObject).toMap()
