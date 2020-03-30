/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")
package dev.teamhub.firebase.firestore

import com.google.firebase.firestore.SetOptions
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.decode
import dev.teamhub.firebase.encode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy

@InternalSerializationApi
actual val Firebase.firestore get() =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())

@InternalSerializationApi
actual fun Firebase.firestore(app: FirebaseApp) =
    FirebaseFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance(app.android))

@InternalSerializationApi
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

@InternalSerializationApi
actual class WriteBatch(val android: com.google.firebase.firestore.WriteBatch) {

    actual fun set(documentRef: DocumentReference, data: Any, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, encode(data)!!, SetOptions.merge())
        false -> android.set(documentRef.android, encode(data)!!)
    }.let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String) =
        android.set(documentRef.android, encode(data)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, encode(data)!!, SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, encode(strategy, data)!!, SetOptions.merge())
        false -> android.set(documentRef.android, encode(strategy, data)!!)
    }.let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String) =
        android.set(documentRef.android, encode(strategy, data)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, encode(strategy, data)!!, SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun update(documentRef: DocumentReference, data: Any) =
        android.update(documentRef.android, encode(data) as Map<String, Any>).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T) =
        android.update(documentRef.android, encode(strategy, data) as Map<String, Any>).let { this }

    @JvmName("updateFields")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.android,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value) })
                }.toTypedArray()
            ).let { this }

    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.android,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value) })
                }.toTypedArray()
            ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun commit() = android.commit().await().run { Unit }

}

@InternalSerializationApi
actual class Transaction(val android: com.google.firebase.firestore.Transaction) {

    actual inline fun set(documentRef: DocumentReference, data: Any, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, encode(data)!!, SetOptions.merge())
        false -> android.set(documentRef.android, encode(data)!!)
    }.let { this }

    actual inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String) =
        android.set(documentRef.android, encode(data)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, encode(data)!!, SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    actual fun <T> set(
        documentRef: DocumentReference,
        strategy: SerializationStrategy<T>,
        data: T,
        merge: Boolean
    ) = when(merge) {
        true -> android.set(documentRef.android, encode(strategy, data)!!, SetOptions.merge())
        false -> android.set(documentRef.android, encode(strategy, data)!!)
    }.let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String) =
        android.set(documentRef.android, encode(strategy, data)!!, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun <T> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, encode(strategy, data)!!, SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun update(documentRef: DocumentReference, data: Any) =
        android.update(documentRef.android, encode(data) as Map<String, Any>).let { this }

    @Suppress("UNCHECKED_CAST")
    actual fun <T> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T) =
        android.update(documentRef.android, encode(strategy, data) as Map<String, Any>).let { this }

    @JvmName("updateFields")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.android,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value) })
                }.toTypedArray()
            ).let { this }

    @JvmName("updateFieldPaths")
    actual fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                documentRef.android,
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value) })
                }.toTypedArray()
            ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        DocumentSnapshot(android.get(documentRef.android))
}

@InternalSerializationApi
actual class DocumentReference(val android: com.google.firebase.firestore.DocumentReference) {

    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual suspend fun set(data: Any, merge: Boolean) = when(merge) {
        true -> android.set(encode(data)!!, SetOptions.merge())
        false -> android.set(encode(data)!!)
    }.await().run { Unit }

    actual suspend fun set(data: Any, vararg mergeFields: String) =
        android.set(encode(data)!!, SetOptions.mergeFields(*mergeFields))
            .await().run { Unit }

    actual suspend fun set(data: Any, vararg mergeFieldsPaths: FieldPath) =
        android.set(encode(data)!!, SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .await().run { Unit }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, merge: Boolean) = when(merge) {
        true -> android.set(encode(strategy, data)!!, SetOptions.merge())
        false -> android.set(encode(strategy, data)!!)
    }.await().run { Unit }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String) =
        android.set(encode(strategy, data)!!, SetOptions.mergeFields(*mergeFields))
            .await().run { Unit }

    actual suspend fun <T> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFieldsPaths: FieldPath) =
        android.set(encode(strategy, data)!!, SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .await().run { Unit }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun update(data: Any) =
        android.update(encode(data) as Map<String, Any>).await().run { Unit }

    @Suppress("UNCHECKED_CAST")
    actual suspend fun <T> update(strategy: SerializationStrategy<T>, data: T) =
        android.update(encode(strategy, data) as Map<String, Any>).await().run { Unit }

    @JvmName("updateFields")
    actual suspend fun update(vararg fieldsAndValues: Pair<String, Any?>) =
        android.takeUnless { fieldsAndValues.isEmpty() }
            ?.update(
                fieldsAndValues[0].first,
                fieldsAndValues[0].second,
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value) })
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
                *fieldsAndValues.drop(1).flatMap { (field, value) ->
                    listOf(field, value?.let { encode(value) })
                }.toTypedArray()
            )
            ?.await()
            .run { Unit }

    actual suspend fun delete() =
        android.delete().await().run { Unit }

    actual suspend fun get() =
        DocumentSnapshot(android.get().await())

    actual val snapshots get() = callbackFlow {
        println("adding snapshot listener to ${this@DocumentReference}: $path")
        val listener = android.addSnapshotListener { snapshot, exception ->
            snapshot?.let { offer(DocumentSnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }
}

@InternalSerializationApi
actual open class Query(open val android: com.google.firebase.firestore.Query) {

    actual suspend fun get() = QuerySnapshot(android.get().await())

    internal actual fun _where(field: String, equalTo: Any?) = Query(android.whereEqualTo(field, equalTo))
    internal actual fun _where(path: FieldPath, equalTo: Any?) = Query(android.whereEqualTo(path, equalTo))

    actual val snapshots get() = callbackFlow {
        println("adding snapshot listener to query ${this@Query}")
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
@InternalSerializationApi
actual class CollectionReference(override val android: com.google.firebase.firestore.CollectionReference) : Query(android) {

    actual val path: String
        get() = android.path

    actual suspend fun add(data: Any) =
        DocumentReference(android.add(encode(data)!!).await())

    actual suspend fun <T> add(data: T, strategy: SerializationStrategy<T>) =
        DocumentReference(android.add(encode(strategy, data)!!).await())
}

actual typealias FirebaseFirestoreException = com.google.firebase.firestore.FirebaseFirestoreException

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual typealias FirestoreExceptionCode = com.google.firebase.firestore.FirebaseFirestoreException.Code

@InternalSerializationApi
actual class QuerySnapshot(val android: com.google.firebase.firestore.QuerySnapshot) {
    actual val documents
        get() = android.documents.map { DocumentSnapshot(it) }
}

@InternalSerializationApi
@Suppress("UNCHECKED_CAST")
actual class DocumentSnapshot(val android: com.google.firebase.firestore.DocumentSnapshot) {

    actual val id get() = android.id
    actual val reference get() = DocumentReference(android.reference)

    actual inline fun <reified T: Any> data() = decode<T>(value = android.data)

    actual inline fun <T> data(strategy: DeserializationStrategy<T>) = decode(strategy, android.data)

    actual inline fun <reified T> get(field: String) = decode<T>(value = android.get(field))

    actual inline fun <T> get(field: String, strategy: DeserializationStrategy<T>) =
        decode(strategy, android.get(field))

    actual fun contains(field: String) = android.contains(field)

    actual val exists get() = android.exists()
}

actual typealias FieldPath = com.google.firebase.firestore.FieldPath

actual fun FieldPath(vararg fieldNames: String) = FieldPath.of(*fieldNames)

actual object FieldValue {
    actual fun delete(): Any = com.google.firebase.firestore.FieldValue.delete()
    actual fun arrayUnion(vararg elements: Any): Any = com.google.firebase.firestore.FieldValue.arrayUnion(*elements)
    actual fun arrayRemove(vararg elements: Any): Any = com.google.firebase.firestore.FieldValue.arrayRemove(*elements)
}

