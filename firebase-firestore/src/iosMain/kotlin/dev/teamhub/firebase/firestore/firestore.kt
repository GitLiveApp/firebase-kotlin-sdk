package dev.teamhub.firebase.firestore

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.DeserializationStrategy

actual open class FirebaseFirestoreException(message: String) : FirebaseException(message)

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = TODO("not implemented")

/** Returns the [FirebaseFirestore] instance of the default [FirebaseApp]. */
actual val Firebase.firestore: FirebaseFirestore
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

/** Returns the [FirebaseFirestore] instance of a given [FirebaseApp]. */
actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual class FirebaseFirestore {
    actual fun collection(collectionPath: String): CollectionReference {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun document(documentPath: String): DocumentReference {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun batch(): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setLoggingEnabled(loggingEnabled: Boolean) {
    }

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual class Transaction {
    @ImplicitReflectionSerializer
    actual inline fun <reified T : Any> set(
        documentRef: DocumentReference,
        data: T,
        merge: Boolean
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ImplicitReflectionSerializer
    actual inline fun <reified T : Any> set(
        documentRef: DocumentReference,
        data: T,
        vararg mergeFields: String
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ImplicitReflectionSerializer
    actual inline fun <reified T : Any> set(
        documentRef: DocumentReference,
        data: T,
        vararg mergeFieldsPaths: FieldPath
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        merge: Boolean
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        vararg mergeFields: String
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        vararg mergeFieldsPaths: FieldPath
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ImplicitReflectionSerializer
    actual inline fun <reified T : Any> update(
        documentRef: DocumentReference,
        data: T
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> update(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun update(
        documentRef: DocumentReference,
        vararg fieldsAndValues: Pair<String, Any?>
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun update(
        documentRef: DocumentReference,
        vararg fieldsAndValues: Pair<FieldPath, Any?>
    ): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun delete(documentRef: DocumentReference): Transaction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend fun get(documentRef: DocumentReference): DocumentSnapshot {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

actual open class Query {
    internal actual fun _where(field: String, equalTo: Any?): Query {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    internal actual fun _where(
        path: FieldPath,
        equalTo: Any?
    ): Query {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    internal actual fun _where(
        field: String,
        lessThan: Any?,
        greaterThan: Any?,
        arrayContains: Any?
    ): Query {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    internal actual fun _where(
        path: FieldPath,
        lessThan: Any?,
        greaterThan: Any?,
        arrayContains: Any?
    ): Query {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual val snapshots: Flow<QuerySnapshot>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual suspend fun get(): QuerySnapshot {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual class WriteBatch {
    @ImplicitReflectionSerializer
    actual inline fun <reified T : Any> set(
        documentRef: DocumentReference,
        data: T,
        merge: Boolean
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ImplicitReflectionSerializer
    actual inline fun <reified T : Any> set(
        documentRef: DocumentReference,
        data: T,
        vararg mergeFields: String
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ImplicitReflectionSerializer
    actual inline fun <reified T : Any> set(
        documentRef: DocumentReference,
        data: T,
        vararg mergeFieldsPaths: FieldPath
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        merge: Boolean
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        vararg mergeFields: String
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        vararg mergeFieldsPaths: FieldPath
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ImplicitReflectionSerializer
    actual inline fun <reified T : Any> update(
        documentRef: DocumentReference,
        data: T
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> update(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun update(
        documentRef: DocumentReference,
        vararg fieldsAndValues: Pair<String, Any?>
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun update(
        documentRef: DocumentReference,
        vararg fieldsAndValues: Pair<FieldPath, Any?>
    ): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun delete(documentRef: DocumentReference): WriteBatch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend fun commit() {
    }

}

actual class DocumentReference {
    actual val id: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val path: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val snapshots: Flow<DocumentSnapshot>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual suspend fun get(): DocumentSnapshot {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ImplicitReflectionSerializer
    actual suspend inline fun <reified T : Any> set(data: T, merge: Boolean) {
    }

    @ImplicitReflectionSerializer
    actual suspend inline fun <reified T : Any> set(data: T, vararg mergeFields: String) {
    }

    @ImplicitReflectionSerializer
    actual suspend inline fun <reified T : Any> set(
        data: T,
        vararg mergeFieldsPaths: FieldPath
    ) {
    }

    actual suspend inline fun <reified T> set(
        strategy: SerializationStrategy<T>,
        data: T,
        merge: Boolean
    ) {
    }

    actual suspend inline fun <reified T> set(
        strategy: SerializationStrategy<T>,
        data: T,
        vararg mergeFields: String
    ) {
    }

    actual suspend inline fun <reified T> set(
        strategy: SerializationStrategy<T>,
        data: T,
        vararg mergeFieldsPaths: FieldPath
    ) {
    }

    @ImplicitReflectionSerializer
    actual suspend inline fun <reified T : Any> update(data: T) {
    }

    actual suspend inline fun <reified T> update(strategy: SerializationStrategy<T>, data: T) {
    }

    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) {
    }

    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) {
    }

    actual suspend fun delete() {
    }

}

actual class CollectionReference : Query() {
    actual val path: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    @ImplicitReflectionSerializer
    actual suspend inline fun <reified T : Any> add(data: T): DocumentReference {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend inline fun <reified T> add(
        data: T,
        strategy: SerializationStrategy<T>
    ): DocumentReference {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual enum class FirestoreExceptionCode {
    OK, CANCELLED, UNKNOWN, INVALID_ARGUMENT, DEADLINE_EXCEEDED, NOT_FOUND, ALREADY_EXISTS, PERMISSION_DENIED, RESOURCE_EXHAUSTED, FAILED_PRECONDITION, ABORTED, OUT_OF_RANGE, UNIMPLEMENTED, INTERNAL, UNAVAILABLE, DATA_LOSS, UNAUTHENTICATED
}

actual class
QuerySnapshot {
    actual val documents: List<DocumentSnapshot>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

actual class DocumentSnapshot {
    @ImplicitReflectionSerializer
    actual inline fun <reified T> get(field: String): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> get(
        field: String,
        strategy: DeserializationStrategy<T>
    ): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun contains(field: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ImplicitReflectionSerializer
    actual inline fun <reified T : Any> data(): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> data(strategy: DeserializationStrategy<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual val exists: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val id: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val reference: DocumentReference
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}

actual class FieldPath

actual fun FieldPath(vararg fieldNames: String): FieldPath {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual object FieldValue {
    actual fun delete(): FieldValueImpl {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun arrayUnion(vararg elements: Any): FieldValueImpl {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun arrayRemove(vararg elements: Any): FieldValueImpl {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual abstract class FieldValueImpl