package dev.teamhub.firebase.firestore

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestoreSettings.Builder
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.SetOptions
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

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

    actual fun set(documentRef: DocumentReference, data: Any, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, data, SetOptions.merge())
        false -> android.set(documentRef.android, data)
    }.let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String) =
        android.set(documentRef.android, data, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, data, SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    actual fun update(documentRef: DocumentReference, data: Map<String, Any>) =
        android.update(documentRef.android, data).let { this }

    actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any) =
        android.update(documentRef.android, field, value, *moreFieldsAndValues).let { this }

    actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) =
        android.update(documentRef.android, fieldPath, value, *moreFieldsAndValues).let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun commit() = android.commit().await().run { Unit }

}

actual class Transaction(val android: com.google.firebase.firestore.Transaction) {

    actual fun set(documentRef: DocumentReference, data: Any, merge: Boolean) = when(merge) {
        true -> android.set(documentRef.android, data, SetOptions.merge())
        false -> android.set(documentRef.android, data)
    }.let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String) =
        android.set(documentRef.android, data, SetOptions.mergeFields(*mergeFields))
            .let { this }

    actual fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldsPaths: FieldPath) =
        android.set(documentRef.android, data, SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .let { this }

    actual fun update(documentRef: DocumentReference, data: Map<String, Any>) =
        android.update(documentRef.android, data).let { this }

    actual fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any) =
        android.update(documentRef.android, field, value, *moreFieldsAndValues).let { this }

    actual fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) =
        android.update(documentRef.android, fieldPath, value, *moreFieldsAndValues).let { this }

    actual fun delete(documentRef: DocumentReference) =
        android.delete(documentRef.android).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        DocumentSnapshot(android.get(documentRef.android))
}

actual class DocumentReference(val android: com.google.firebase.firestore.DocumentReference) {

    actual val id: String
        get() = android.id

    actual suspend fun set(data: Any, merge: Boolean) = when(merge) {
        true -> android.set(data, SetOptions.merge())
        false -> android.set(data)
    }.await().run { Unit }

    actual suspend fun set(data: Any, vararg mergeFields: String) =
        android.set(data, SetOptions.mergeFields(*mergeFields))
            .await().run { Unit }

    actual suspend fun set(data: Any, vararg mergeFieldsPaths: FieldPath) =
        android.set(data, SetOptions.mergeFieldPaths(mergeFieldsPaths.toList()))
            .await().run { Unit }

    actual suspend fun update(data: Map<String, Any>) =
        android.update(data).await().run { Unit }

    actual suspend fun update(field: String, value: Any?, vararg moreFieldsAndValues: Any) =
        android.update(field, value, *moreFieldsAndValues)
            .await().run { Unit }

    actual suspend fun update(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) =
        android.update(fieldPath, value, *moreFieldsAndValues)
            .await().run { Unit }

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
    actual fun whereEqualTo(field: String, value: Any?) = Query(android.whereEqualTo(field, value))
    actual fun whereEqualTo(path: FieldPath, value: Any?) = Query(android.whereEqualTo(path, value))
    actual fun whereLessThan(field: String, value: Any) =  Query(android.whereLessThan(field, value))
    actual fun whereLessThan(path: FieldPath, value: Any) = Query(android.whereLessThan(path, value))
    actual fun whereGreaterThan(field: String, value: Any) = Query(android.whereGreaterThan(field, value))
    actual fun whereGreaterThan(path: FieldPath, value: Any) = Query(android.whereGreaterThan(path, value))
    actual fun whereArrayContains(field: String, value: Any) = Query(android.whereArrayContains(field, value))
    actual fun whereArrayContains(path: FieldPath, value: Any) = Query(android.whereArrayContains(path, value))

    actual val snapshots get() = callbackFlow {
        val listener = android.addSnapshotListener { snapshot, exception ->
            snapshot?.let { offer(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }
}
actual class CollectionReference(override val android: com.google.firebase.firestore.CollectionReference) : Query(android) {
    actual suspend fun add(data: Map<String, Any>) = DocumentReference(android.add(data).await())
    actual suspend fun add(pojo: Any) = DocumentReference(android.add(pojo).await())
}

actual typealias FirebaseFirestoreException = com.google.firebase.firestore.FirebaseFirestoreException

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual typealias FirestoreExceptionCode = com.google.firebase.firestore.FirebaseFirestoreException.Code

actual class QuerySnapshot(val android: com.google.firebase.firestore.QuerySnapshot) {
    actual val documents
        get() = android.documents.map { DocumentSnapshot(it) }
}

actual class DocumentSnapshot(val android: com.google.firebase.firestore.DocumentSnapshot) {
    actual val id get() = android.id
    actual val reference get() = DocumentReference(android.reference)
    actual inline fun <reified T> toObject() = android.toObject(T::class.java)
    actual inline fun <reified T> get(field: String) = android.get(field, T::class.java)

    actual fun contains(field: String) = android.contains(field)

    actual val exists get() = android.exists()
}

actual typealias FieldPath = com.google.firebase.firestore.FieldPath

actual fun FieldPath(vararg fieldNames: String) = FieldPath.of(*fieldNames)

actual typealias IgnoreExtraProperties = IgnoreExtraProperties

actual typealias Exclude = Exclude

actual typealias FieldValueImpl = com.google.firebase.firestore.FieldValue

actual object FieldValue {
    actual fun delete() = FieldValueImpl.delete()
    actual fun arrayUnion(vararg elements: Any) = FieldValueImpl.arrayUnion(*elements)
    actual fun arrayRemove(vararg elements: Any) = FieldValueImpl.arrayRemove(*elements)
}

