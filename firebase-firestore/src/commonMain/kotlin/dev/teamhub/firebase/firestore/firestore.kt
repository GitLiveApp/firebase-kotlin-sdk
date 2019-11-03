@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")
package dev.teamhub.firebase.firestore

import dev.teamhub.firebase.FirebaseException
import kotlin.reflect.KClass

expect fun getFirebaseFirestore(): FirebaseFirestore

expect class FirebaseFirestore

expect fun FirebaseFirestore.getFirestoreSettings(): FirebaseFirestoreSettings
expect fun FirebaseFirestore.setFirestoreSettings(settings: FirebaseFirestoreSettings)
expect fun FirebaseFirestore.collection(collectionPath: String): CollectionReference
expect fun FirebaseFirestore.document(documentPath: String): DocumentReference
expect fun FirebaseFirestore.batch(): WriteBatch
expect fun FirebaseFirestore.setLoggingEnabled(loggingEnabled: Boolean)

expect annotation class IgnoreExtraProperties()
expect annotation class Exclude()


expect suspend fun <T> FirebaseFirestore.awaitRunTransaction(func: suspend (transaction: Transaction) -> T): T

expect class Transaction
expect fun Transaction.set(documentRef: DocumentReference, data: Map<String, Any>): Transaction
expect fun Transaction.set(documentRef: DocumentReference, data: Map<String, Any>, options: SetOptions): Transaction
expect fun Transaction.set(documentRef: DocumentReference, pojo: Any): Transaction
expect fun Transaction.set(documentRef: DocumentReference, pojo: Any, options: SetOptions): Transaction
expect fun Transaction.update(documentRef: DocumentReference, data: Map<String, Any>): Transaction
expect fun Transaction.update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any): Transaction
expect fun Transaction.update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any): Transaction
expect fun Transaction.delete(documentRef: DocumentReference): Transaction
expect suspend fun Transaction.awaitGet(documentRef: DocumentReference): DocumentSnapshot


expect class FirebaseFirestoreSettingsBuilder constructor() {
    constructor(settings: FirebaseFirestoreSettings)
}

expect fun FirebaseFirestoreSettingsBuilder.setPersistenceEnabled(enabled: Boolean): FirebaseFirestoreSettingsBuilder
expect fun FirebaseFirestoreSettingsBuilder.setTimestampsInSnapshotsEnabled(enabled: Boolean): FirebaseFirestoreSettingsBuilder
expect fun FirebaseFirestoreSettingsBuilder.build(): FirebaseFirestoreSettings


expect class FirebaseFirestoreSettings

expect open class Query
expect fun Query.whereEqualTo(field: String, value: Any?): Query
expect fun Query.whereEqualTo(path: FieldPath, value: Any?): Query
expect fun Query.whereLessThan(field: String, value: Any): Query
expect fun Query.whereLessThan(path: FieldPath, value: Any): Query
expect fun Query.whereGreaterThan(field: String, value: Any): Query
expect fun Query.whereGreaterThan(path: FieldPath, value: Any): Query
expect fun Query.whereArrayContains(field: String, value: Any): Query
expect fun Query.whereArrayContains(path: FieldPath, value: Any): Query
expect fun Query.addSnapshotListener(listener: EventListener<QuerySnapshot>): ListenerRegistration
expect fun Query.addSnapshotListener(listener: (snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) -> Unit): ListenerRegistration

expect suspend fun Query.awaitGet(): QuerySnapshot

expect class WriteBatch

expect fun WriteBatch.set(documentRef: DocumentReference, data: Map<String, Any>): WriteBatch
expect fun WriteBatch.set(documentRef: DocumentReference, data: Map<String, Any>, options: SetOptions): WriteBatch
expect fun WriteBatch.set(documentRef: DocumentReference, pojo: Any): WriteBatch
expect fun WriteBatch.set(documentRef: DocumentReference, pojo: Any, options: SetOptions): WriteBatch
expect fun WriteBatch.update(documentRef: DocumentReference, data: Map<String, Any>): WriteBatch
expect fun WriteBatch.update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any): WriteBatch
expect fun WriteBatch.update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any): WriteBatch
expect fun WriteBatch.delete(documentRef: DocumentReference): WriteBatch

expect suspend fun WriteBatch.awaitCommit()

expect class DocumentReference

expect fun DocumentReference.addSnapshotListener(listener: EventListener<DocumentSnapshot>): ListenerRegistration

expect val DocumentReference.id: String

expect val DocumentReference.path: String

expect fun DocumentReference.addSnapshotListener(listener: (snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) -> Unit): ListenerRegistration

expect suspend fun DocumentReference.awaitGet(): DocumentSnapshot

expect suspend fun DocumentReference.awaitSet(data: Map<String, Any>)

expect suspend fun DocumentReference.awaitSet(pojo: Any)

expect suspend fun DocumentReference.awaitSet(data: Map<String, Any>, options: SetOptions)

expect suspend fun DocumentReference.awaitSet(pojo: Any, options: SetOptions)

expect suspend fun DocumentReference.awaitUpdate(data: Map<String, Any>)

expect suspend fun DocumentReference.awaitUpdate(field: String, value: Any?, vararg moreFieldsAndValues: Any)

expect suspend fun DocumentReference.awaitUpdate(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any)

expect suspend fun DocumentReference.awaitDelete()

expect class CollectionReference : Query

expect suspend fun CollectionReference.awaitAdd(data: Map<String, Any>): DocumentReference

expect suspend fun CollectionReference.awaitAdd(pojo: Any): DocumentReference

expect class FirebaseFirestoreException : FirebaseException

expect val FirebaseFirestoreException.code: FirestoreExceptionCode

expect enum class FirestoreExceptionCode {
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

expect class QuerySnapshot

expect val QuerySnapshot.documents: List<DocumentSnapshot>

expect class DocumentSnapshot

expect fun DocumentSnapshot.get(field: String): Any?
expect fun DocumentSnapshot.getString(field: String): String?
expect fun DocumentSnapshot.contains(field: String): Boolean
expect fun <T: Any> DocumentSnapshot.toObject(valueType: KClass<T>): T
expect fun DocumentSnapshot.exists(): Boolean

expect val DocumentSnapshot.id: String
expect val DocumentSnapshot.reference: DocumentReference

expect interface ListenerRegistration

expect fun ListenerRegistration.remove()


expect interface EventListener<T> {
    fun onEvent(snapshot: T?, exception: FirebaseFirestoreException?)
}

expect class SetOptions

expect fun mergeSetOptions(): SetOptions

expect fun fieldPathOf(vararg fieldNames: String): FieldPath

expect class FieldPath

expect abstract class FieldValue

expect fun deleteFieldValue(): FieldValue

expect fun arrayUnionFieldValue(vararg elements: Any): FieldValue

expect fun arrayRemoveFieldValue(vararg elements: Any): FieldValue

