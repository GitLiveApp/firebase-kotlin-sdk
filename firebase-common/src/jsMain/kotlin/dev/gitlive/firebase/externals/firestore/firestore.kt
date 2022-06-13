@file:JsModule("firebase/firestore")
@file:JsNonModule

package dev.gitlive.firebase.externals.firestore

import dev.gitlive.firebase.Unsubscribe
import dev.gitlive.firebase.externals.app.FirebaseApp
import dev.gitlive.firebase.externals.database.QueryConstraint
import kotlin.js.Json
import kotlin.js.Promise

external class FieldPath(vararg fieldNames: String) {
    companion object {
        val documentId: FieldPath
    }
}

external fun addDoc(reference: CollectionReference, data: Any): Promise<DocumentReference>

external fun arrayRemove(vararg elements: Any): FieldValue

external fun arrayUnion(vararg elements: Any): FieldValue

external fun clearIndexedDbPersistence(firestore: Firestore): Promise<Unit>

external fun collection(firestore: Firestore, collectionPath: String): CollectionReference

external fun collection(reference: DocumentReference, collectionPath: String): CollectionReference

external fun collectionGroup(firestore: Firestore, collectionId: String): Query

external fun connectFirestoreEmulator(
    firestore: Firestore,
    host: String,
    port: Int,
    options: Any? = definedExternally
)

external fun deleteDoc(reference: DocumentReference): Promise<Unit>

external fun deleteField(): FieldValue

external fun disableNetwork(firestore: Firestore): Promise<Unit>

external fun doc(firestore: Firestore, documentPath: String): DocumentReference

external fun doc(firestore: CollectionReference, documentPath: String? = definedExternally): DocumentReference

external fun enableIndexedDbPersistence(
    firestore: Firestore,
    persistenceSettings: Any? = definedExternally
): Promise<Unit>

external fun enableNetwork(firestore: Firestore): Promise<Unit>

external fun endAt(document: DocumentSnapshot): QueryConstraint

external fun endAt(vararg fieldValues: Any): QueryConstraint

external fun endBefore(document: DocumentSnapshot): QueryConstraint

external fun endBefore(vararg fieldValues: Any): QueryConstraint

external fun getDoc(
    reference: DocumentReference,
    options: Any? = definedExternally
): Promise<DocumentSnapshot>

external fun getDocs(query: Query): Promise<QuerySnapshot>

external fun getFirestore(app: FirebaseApp? = definedExternally): Firestore

external fun increment(n: Int): FieldValue

external fun initializeFirestore(app: FirebaseApp, settings: Any): Firestore

external fun limit(limit: Number): QueryConstraint

external fun onSnapshot(
    reference: DocumentReference,
    next: (snapshot: DocumentSnapshot) -> Unit,
    error: (error: Throwable) -> Unit
): Unsubscribe

external fun onSnapshot(
    reference: Query,
    next: (snapshot: QuerySnapshot) -> Unit,
    error: (error: Throwable) -> Unit
): Unsubscribe

external fun onSnapshot(
    reference: Query,
    options: Json,
    next: (snapshot: QuerySnapshot) -> Unit,
    error: (error: Throwable) -> Unit
): Unsubscribe

external fun orderBy(field: String, direction: Any): QueryConstraint

external fun orderBy(field: FieldPath, direction: Any): QueryConstraint

external fun query(query: Query, vararg queryConstraints: QueryConstraint): Query

external fun <T> runTransaction(
    firestore: Firestore,
    updateFunction: (transaction: Transaction) -> Promise<T>,
    options: Any? = definedExternally
): Promise<T>

external fun serverTimestamp(): FieldValue

external fun setDoc(
    documentReference: DocumentReference,
    data: Any,
    options: Any? = definedExternally
): Promise<Unit>

external fun setLogLevel(logLevel: String)

external fun startAfter(document: DocumentSnapshot): QueryConstraint

external fun startAfter(vararg fieldValues: Any): QueryConstraint

external fun startAt(document: DocumentSnapshot): QueryConstraint

external fun startAt(vararg fieldValues: Any): QueryConstraint

external fun updateDoc(reference: DocumentReference, data: Any): Promise<Unit>

external fun updateDoc(
    reference: DocumentReference,
    field: String,
    value: Any?,
    vararg moreFieldsAndValues: Any?
): Promise<Unit>

external fun updateDoc(
    reference: DocumentReference,
    field: FieldPath,
    value: Any?,
    vararg moreFieldsAndValues: Any?
): Promise<Unit>

external fun where(field: String, opStr: String, value: Any?): QueryConstraint

external fun where(field: FieldPath, opStr: String, value: Any?): QueryConstraint

external fun writeBatch(firestore: Firestore): WriteBatch

external interface Firestore {
    val app: FirebaseApp
}

external interface CollectionReference : Query {
    val id: String
    val path: String
    val parent: DocumentReference?
}

external interface DocumentChange {
    val doc: DocumentSnapshot
    val newIndex: Int
    val oldIndex: Int
    val type: String
}

external interface DocumentReference {
    val id: String
    val path: String
    val parent: CollectionReference
}

external interface DocumentSnapshot {
    val id: String
    val ref: DocumentReference
    val metadata: SnapshotMetadata
    fun data(options: Any? = definedExternally): Any?
    fun exists(): Boolean
    fun get(fieldPath: String, options: Any? = definedExternally): Any?
    fun get(fieldPath: FieldPath, options: Any? = definedExternally): Any?
}

external interface FieldValue

external interface Query

external interface QueryConstraint

external interface QuerySnapshot {
    val docs: Array<DocumentSnapshot>
    val empty: Boolean
    val metadata: SnapshotMetadata
    fun docChanges(): Array<DocumentChange>
}

external interface SnapshotMetadata {
    val hasPendingWrites: Boolean
    val fromCache: Boolean
}

external interface Transaction {
    fun get(documentReference: DocumentReference): Promise<DocumentSnapshot>

    fun set(
        documentReference: DocumentReference,
        data: Any,
        options: Any? = definedExternally
    ): Transaction

    fun update(documentReference: DocumentReference, data: Any): Transaction

    fun update(
        documentReference: DocumentReference,
        field: String,
        value: Any?,
        vararg moreFieldsAndValues: Any?
    ): Transaction

    fun update(
        documentReference: DocumentReference,
        field: FieldPath,
        value: Any?,
        vararg moreFieldsAndValues: Any?
    ): Transaction

    fun delete(documentReference: DocumentReference): Transaction
}

external interface WriteBatch {
    fun commit(): Promise<Unit>

    fun delete(documentReference: DocumentReference): WriteBatch

    fun set(
        documentReference: DocumentReference,
        data: Any,
        options: Any? = definedExternally
    ): WriteBatch

    fun update(documentReference: DocumentReference, data: Any): WriteBatch

    fun update(
        documentReference: DocumentReference,
        field: String,
        value: Any?,
        vararg moreFieldsAndValues: Any?
    ): WriteBatch

    fun update(
        documentReference: DocumentReference,
        field: FieldPath,
        value: Any?,
        vararg moreFieldsAndValues: Any?
    ): WriteBatch
}
