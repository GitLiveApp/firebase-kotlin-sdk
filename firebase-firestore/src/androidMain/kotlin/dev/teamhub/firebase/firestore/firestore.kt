@file:JvmName("android")
package dev.teamhub.firebase.firestore

import com.google.firebase.firestore.SetOptions
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.*

actual val Firebase.firestore get() =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())

actual fun Firebase.firestore(app: FirebaseApp) =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance(app.android))

actual class FirebaseFirestore(val android: com.google.firebase.firestore.FirebaseFirestore) {

//    actual var settings: FirebaseFirestoreSettings
//        get() = android.firestoreSettings.run { FirebaseFirestoreSettings(isPersistenceEnabled) }
//        set(value) {
//            android.firestoreSettings = value.run { Builder().setPersistenceEnabled(persistenceEnabled).build() }
//        }

    actual fun collection(collectionPath: String) = CollectionReference(android.collection(collectionPath))

    actual fun document(documentPath: String) = DocumentReference(android.document(documentPath))

    actual fun batch() = WriteBatch(android.batch())

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        android.runTransaction { runBlocking { Transaction(it).func() } }.await()
}

actual class WriteBatch(val android: com.google.firebase.firestore.WriteBatch) {

    actual inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, Mapper.map(data), SetOptions.merge())
        false -> android.set(documentRef.android, Mapper.map(data))
    }.let { this }

    actual inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, vararg mergeFields: String) =
        android.set(documentRef.android, Mapper.map(data), SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, Mapper.map(data), SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, Mapper.map(strategy, data), SetOptions.merge())
        false -> android.set(documentRef.android, Mapper.map(strategy, data))
    }.let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, vararg mergeFields: String) =
        android.set(documentRef.android, Mapper.map(strategy, data), SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, Mapper.map(strategy, data), SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    actual inline fun <reified T: Any> update(documentRef: DocumentReference, data: T) =
        android.update(documentRef.android, Mapper.map(data)).let { this }

    actual inline fun <reified T> update(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>) =
        android.update(documentRef.android, Mapper.map(strategy, data)).let { this }

    @JvmName("updateFields")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.android,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { Mapper.map(value) })
                }.toTypedArray()
            ).let { this }

    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.android,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.flatMap { (field, value) ->
                    listOf(field, value?.let { Mapper.map(value) })
                }.toTypedArray()
            ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun commit() = android.commit().await().run { Unit }

}

actual class Transaction(val android: com.google.firebase.firestore.Transaction) {

    actual inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, Mapper.map(data), SetOptions.merge())
        false -> android.set(documentRef.android, Mapper.map(data))
    }.let { this }

    actual inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, vararg mergeFields: String) =
        android.set(documentRef.android, Mapper.map(data), SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual inline fun <reified T: Any> set(documentRef: DocumentReference, data: T, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, Mapper.map(data), SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, Mapper.map(strategy, data), SetOptions.merge())
        false -> android.set(documentRef.android, Mapper.map(strategy, data))
    }.let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, vararg mergeFields: String) =
        android.set(documentRef.android, Mapper.map(strategy, data), SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual inline fun <reified T> set(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, Mapper.map(strategy, data), SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    actual inline fun <reified T: Any> update(documentRef: DocumentReference, data: T) =
        android.update(documentRef.android, Mapper.map(data)).let { this }

    actual inline fun <reified T> update(documentRef: DocumentReference, data: T, strategy: SerializationStrategy<T>) =
        android.update(documentRef.android, Mapper.map(strategy, data)).let { this }

    @JvmName("updateFields")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.android,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { Mapper.map(value) })
                }.toTypedArray()
            ).let { this }

    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.android,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.flatMap { (field, value) ->
                    listOf(field, value?.let { Mapper.map(value) })
                }.toTypedArray()
            ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        DocumentSnapshot(android.get(documentRef.android))
}

actual class DocumentReference(val android: com.google.firebase.firestore.DocumentReference) {

    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual suspend inline fun <reified T: Any> set(data: T, merge: Boolean) = when(merge) {
        true -> android.set(Mapper.map(data), SetOptions.merge())
        false -> android.set(Mapper.map(data))
    }.await().run { Unit }

    actual suspend inline fun <reified T: Any> set(data: T, vararg mergeFields: String) =
        android.set(Mapper.map(data), SetOptions.mergeFields(*mergeFields))
            .await().run { Unit }

    actual suspend inline fun <reified T: Any> set(data: T, vararg mergeFieldsPaths: FieldPath) =
        android.set(Mapper.map(data), SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .await().run { Unit }

    actual suspend inline fun <reified T> set(data: T, strategy: SerializationStrategy<T>, merge: Boolean) = when(merge) {
        true -> android.set(Mapper.map(strategy, data), SetOptions.merge())
        false -> android.set(Mapper.map(strategy, data))
    }.await().run { Unit }

    actual suspend inline fun <reified T> set(data: T, strategy: SerializationStrategy<T>, vararg mergeFields: String) =
        android.set(Mapper.map(strategy, data), SetOptions.mergeFields(*mergeFields))
            .await().run { Unit }

    actual suspend inline fun <reified T> set(data: T, strategy: SerializationStrategy<T>, vararg mergeFieldsPaths: FieldPath) =
        android.set(Mapper.map(strategy, data), SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .await().run { Unit }

    actual suspend inline fun <reified T: Any> update(data: T) =
        android.update(Mapper.map(data)).await().run { Unit }

    actual suspend inline fun <reified T> update(data: T, strategy: SerializationStrategy<T>) =
        android.update(Mapper.map(strategy, data)).await().run { Unit }

    @JvmName("updateFields")
    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { Mapper.map(value) })
                }.toTypedArray()
            )
            ?.await()
            .run { Unit }

    @JvmName("updateFieldPaths")
    actual suspend fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.flatMap { (field, value) ->
                    listOf(field, value?.let { Mapper.map(value) })
                }.toTypedArray()
            )
            ?.await()
            .run { Unit }

    actual suspend fun delete() =
        android.delete().await().run { Unit }

    actual suspend fun get() =
        DocumentSnapshot(android.get().await())

    actual val snapshots get() = callbackFlow {
        val listener = android.addSnapshotListener { snapshot, exception ->
            snapshot?.let { offer(DocumentSnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }
}

actual open class Query(open val android: com.google.firebase.firestore.Query) {

    actual suspend fun get() = QuerySnapshot(android.get().await())

    internal actual fun _where(field: String, equalTo: Any?) = Query(android.whereEqualTo(field, equalTo))
    internal actual fun _where(path: FieldPath, equalTo: Any?) = Query(android.whereEqualTo(path, equalTo))

    actual val snapshots get() = callbackFlow {
        val listener = android.addSnapshotListener { snapshot, exception ->
            snapshot?.let { offer(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    internal actual fun _where(field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { android.whereLessThan(field, it) } ?: android).let { android ->
            (greaterThan?.let { android.whereGreaterThan(field, it) } ?: android).let { android ->
                arrayContains?.let { android.whereArrayContains(field, it) } ?: android
            }
        }
    )

    internal actual fun _where(path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?) = Query(
        (lessThan?.let { android.whereLessThan(path, it) } ?: android).let { android ->
            (greaterThan?.let { android.whereGreaterThan(path, it) } ?: android).let { android ->
                arrayContains?.let { android.whereArrayContains(path, it) } ?: android
            }
        }
    )
}
actual class CollectionReference(override val android: com.google.firebase.firestore.CollectionReference) : Query(android) {

    actual val path: String
        get() = android.path

    actual suspend inline fun <reified T : Any> add(data: T) =
        DocumentReference(android.add(Mapper.map(data)).await())

    actual suspend inline fun <reified T> add(data: T, strategy: SerializationStrategy<T>) =
        DocumentReference(android.add(Mapper.map(strategy, data)).await())
}

actual typealias FirebaseFirestoreException = com.google.firebase.firestore.FirebaseFirestoreException

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual typealias FirestoreExceptionCode = com.google.firebase.firestore.FirebaseFirestoreException.Code

actual class QuerySnapshot(val android: com.google.firebase.firestore.QuerySnapshot) {
    actual val documents
        get() = android.documents.map { DocumentSnapshot(it) }
}

@Serializable
data class Value<T>(val value: T)

@Suppress("UNCHECKED_CAST")
actual class DocumentSnapshot(val android: com.google.firebase.firestore.DocumentSnapshot) {

    actual val id get() = android.id
    actual val reference get() = DocumentReference(android.reference)

    actual inline fun <reified T: Any> data() = android.data?.let { Mapper.unmap<T>(it) }

    actual inline fun <reified T> data(strategy: DeserializationStrategy<T>) =
        android.data?.let { Mapper.unmap(strategy, it) }

    actual inline fun <reified T: Any> get(field: String) =
        Mapper.unmapNullable<Value<T>>(mapOf("value" to android.get(field))).value

    actual inline fun <reified T> get(field: String, strategy: DeserializationStrategy<T>) =
        object : KSerializer<T>, DeserializationStrategy<T> by strategy {
            override fun serialize(encoder: Encoder, obj: T) = error("not supported")
        }.let {
            Mapper.unmapNullable(Value.serializer(it), mapOf("value" to android.get(field))).value
        }

    actual fun contains(field: String) = android.contains(field)

    actual val exists get() = android.exists()
}

actual typealias FieldPath = com.google.firebase.firestore.FieldPath

actual fun FieldPath(vararg fieldNames: String) = FieldPath.of(*fieldNames)

actual typealias FieldValueImpl = com.google.firebase.firestore.FieldValue

actual object FieldValue {
    actual fun delete() = FieldValueImpl.delete()
    actual fun arrayUnion(vararg elements: Any) = FieldValueImpl.arrayUnion(*elements)
    actual fun arrayRemove(vararg elements: Any) = FieldValueImpl.arrayRemove(*elements)
}

