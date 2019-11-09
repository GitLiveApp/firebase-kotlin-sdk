package dev.teamhub.firebase.firestore

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlin.reflect.KClass

actual fun getFirebaseFirestore() = FirebaseFirestore.getInstance()

actual typealias FirebaseFirestore = com.google.firebase.firestore.FirebaseFirestore

actual suspend fun <T> FirebaseFirestore.awaitRunTransaction(func: suspend (transaction: Transaction) -> T)
        = runTransaction { runBlocking { func(it) } }.await()

actual typealias Transaction = com.google.firebase.firestore.Transaction

actual suspend fun DocumentReference.awaitSet(data: Map<String, Any>) = set(data).await().run { Unit }

actual suspend fun DocumentReference.awaitSet(pojo: Any) = set(pojo).await().run { Unit }

actual suspend fun DocumentReference.awaitSet(data: Map<String, Any>, options: SetOptions) = set(data, options).await().run { Unit }

actual suspend fun DocumentReference.awaitSet(pojo: Any, options: SetOptions) = set(pojo, options).await().run { Unit }

actual suspend fun DocumentReference.awaitUpdate(data: Map<String, Any>) = update(data).await().run { Unit }

actual fun DocumentReference.addSnapshotListener(listener: (snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) -> Unit)
        = addSnapshotListener { s, e -> listener(s, e) }

actual typealias CollectionReference = com.google.firebase.firestore.CollectionReference

actual val CollectionReference.path: String
    get() = path

actual suspend fun CollectionReference.awaitAdd(data: Map<String, Any>) = add(data).await()

actual suspend fun CollectionReference.awaitAdd(pojo: Any) = add(pojo).await()

actual typealias FirebaseFirestoreException = com.google.firebase.firestore.FirebaseFirestoreException

actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual typealias FirestoreExceptionCode = com.google.firebase.firestore.FirebaseFirestoreException.Code

actual typealias QuerySnapshot =  com.google.firebase.firestore.QuerySnapshot

actual val QuerySnapshot.documents: List<DocumentSnapshot>
    get() = documents

actual typealias DocumentSnapshot = com.google.firebase.firestore.DocumentSnapshot

actual val DocumentSnapshot.id: String
    get() = id

actual val DocumentSnapshot.reference: DocumentReference
    get() = reference

actual fun <T : Any> DocumentSnapshot.toObject(valueType: KClass<T>) = toObject(valueType.java) as T

actual typealias EventListener<T> = com.google.firebase.firestore.EventListener<T>

actual typealias ListenerRegistration = com.google.firebase.firestore.ListenerRegistration

actual typealias Query = com.google.firebase.firestore.Query

actual suspend fun Query.awaitGet() = get().await()

actual fun Query.addSnapshotListener(listener: (snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) -> Unit) = addSnapshotListener { s, e -> listener(s, e) }

actual typealias FieldPath = com.google.firebase.firestore.FieldPath

actual fun fieldPathOf(vararg fieldNames: String) = FieldPath.of(*fieldNames)

actual typealias FirebaseFirestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings

actual typealias FirebaseFirestoreSettingsBuilder =  com.google.firebase.firestore.FirebaseFirestoreSettings.Builder

actual typealias DocumentReference = com.google.firebase.firestore.DocumentReference

actual typealias SetOptions = com.google.firebase.firestore.SetOptions

actual fun mergeSetOptions(): SetOptions = SetOptions.merge()

actual suspend fun DocumentReference.awaitDelete() = delete().await().run { Unit }

actual val DocumentReference.id: String
    get() = id

actual val DocumentReference.path: String
    get() = path

actual typealias WriteBatch = com.google.firebase.firestore.WriteBatch

actual suspend fun WriteBatch.awaitCommit() = commit().await().run { Unit }

actual suspend fun DocumentReference.awaitGet() = get().await()

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestore.getFirestoreSettings(): FirebaseFirestoreSettings {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestore.setFirestoreSettings(settings: FirebaseFirestoreSettings) {
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestore.collection(collectionPath: String): CollectionReference {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestore.document(documentPath: String): DocumentReference {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestore.batch(): WriteBatch {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Transaction.set(documentRef: DocumentReference, data: Map<String, Any>): Transaction {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Transaction.set(documentRef: DocumentReference, data: Map<String, Any>, options: SetOptions): Transaction {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Transaction.set(documentRef: DocumentReference, pojo: Any): Transaction {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Transaction.set(documentRef: DocumentReference, pojo: Any, options: SetOptions): Transaction {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Transaction.update(documentRef: DocumentReference, data: Map<String, Any>): Transaction {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Transaction.update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any): Transaction {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Transaction.update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any): Transaction {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Transaction.delete(documentRef: DocumentReference): Transaction {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual suspend fun Transaction.awaitGet(documentRef: DocumentReference) = get(documentRef)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestoreSettingsBuilder.setPersistenceEnabled(enabled: Boolean): FirebaseFirestoreSettingsBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestoreSettingsBuilder.setTimestampsInSnapshotsEnabled(enabled: Boolean): FirebaseFirestoreSettingsBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseFirestoreSettingsBuilder.build(): FirebaseFirestoreSettings {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Query.whereEqualTo(field: String, value: Any?): Query {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Query.whereEqualTo(path: FieldPath, value: Any?): Query {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Query.whereLessThan(field: String, value: Any): Query {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Query.whereLessThan(path: FieldPath, value: Any): Query {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Query.whereGreaterThan(field: String, value: Any): Query {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Query.whereGreaterThan(path: FieldPath, value: Any): Query {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Query.whereArrayContains(field: String, value: Any): Query {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Query.whereArrayContains(path: FieldPath, value: Any): Query {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Query.addSnapshotListener(listener: EventListener<QuerySnapshot>): ListenerRegistration {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun WriteBatch.set(documentRef: DocumentReference, data: Map<String, Any>): WriteBatch {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun WriteBatch.set(documentRef: DocumentReference, data: Map<String, Any>, options: SetOptions): WriteBatch {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun WriteBatch.set(documentRef: DocumentReference, pojo: Any): WriteBatch {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun WriteBatch.set(documentRef: DocumentReference, pojo: Any, options: SetOptions): WriteBatch {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun WriteBatch.update(documentRef: DocumentReference, data: Map<String, Any>): WriteBatch {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun WriteBatch.update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any): WriteBatch {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun WriteBatch.update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any): WriteBatch {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun WriteBatch.delete(documentRef: DocumentReference): WriteBatch {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DocumentReference.addSnapshotListener(listener: EventListener<DocumentSnapshot>): ListenerRegistration {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DocumentSnapshot.get(field: String): Any? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DocumentSnapshot.getString(field: String): String? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DocumentSnapshot.contains(field: String): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun ListenerRegistration.remove() {
}

actual fun FirebaseFirestore.setLoggingEnabled(loggingEnabled: Boolean) = FirebaseFirestore.setLoggingEnabled(true)

actual typealias IgnoreExtraProperties = IgnoreExtraProperties

actual typealias Exclude = Exclude

actual typealias FieldValue = com.google.firebase.firestore.FieldValue

actual fun deleteFieldValue() = FieldValue.delete()

actual fun arrayUnionFieldValue(vararg elements: Any) = FieldValue.arrayUnion(*elements)

actual fun arrayRemoveFieldValue(vararg elements: Any) = FieldValue.arrayRemove(*elements)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun DocumentSnapshot.exists(): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual suspend fun DocumentReference.awaitUpdate(field: String, value: Any?, vararg moreFieldsAndValues: Any) = update(field, value, *moreFieldsAndValues).await().run { Unit }

actual suspend fun DocumentReference.awaitUpdate(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any) = update(fieldPath, value, *moreFieldsAndValues).await().run { Unit }