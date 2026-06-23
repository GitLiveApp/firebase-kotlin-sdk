@file:JsModule("firebase/firestore")

package dev.gitlive.firebase.firestore.externals

import dev.gitlive.firebase.Unsubscribe
import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun documentId(): FieldPath

public external class FieldPath(vararg fieldNames: String) : JsAny {
    public fun isEqual(other: FieldPath): Boolean
}

public external fun refEqual(left: DocumentReference, right: DocumentReference): Boolean

public external fun addDoc(reference: CollectionReference, data: JsAny): Promise<DocumentReference>

public external fun arrayRemove(vararg elements: JsAny?): FieldValue

public external fun arrayUnion(vararg elements: JsAny?): FieldValue

public external fun clearIndexedDbPersistence(firestore: Firestore): Promise<JsAny?>

public external fun collection(firestore: Firestore, collectionPath: String): CollectionReference

public external fun collection(reference: DocumentReference, collectionPath: String): CollectionReference

public external fun collectionGroup(firestore: Firestore, collectionId: String): Query

public external fun connectFirestoreEmulator(
    firestore: Firestore,
    host: String,
    port: Int,
    options: JsAny? = definedExternally,
)

public external fun deleteDoc(reference: DocumentReference): Promise<JsAny?>

public external fun deleteField(): FieldValue

public external fun disableNetwork(firestore: Firestore): Promise<JsAny?>

public external fun doc(firestore: Firestore, documentPath: String): DocumentReference

public external fun doc(firestore: CollectionReference, documentPath: String? = definedExternally): DocumentReference

public external fun enableIndexedDbPersistence(
    firestore: Firestore,
    persistenceSettings: JsAny? = definedExternally,
): Promise<JsAny?>

public external fun enableNetwork(firestore: Firestore): Promise<JsAny?>

public external fun endAt(document: DocumentSnapshot): QueryConstraint

public external fun endAt(vararg fieldValues: JsAny?): QueryConstraint

public external fun endBefore(document: DocumentSnapshot): QueryConstraint

public external fun endBefore(vararg fieldValues: JsAny?): QueryConstraint

public external fun getDoc(
    reference: DocumentReference,
    options: JsAny? = definedExternally,
): Promise<DocumentSnapshot>

public external fun getDocFromCache(
    reference: DocumentReference,
): Promise<DocumentSnapshot>

public external fun getDocFromServer(
    reference: DocumentReference,
): Promise<DocumentSnapshot>

public external fun getDocs(query: Query): Promise<QuerySnapshot>

public external fun getDocsFromCache(query: Query): Promise<QuerySnapshot>

public external fun getDocsFromServer(query: Query): Promise<QuerySnapshot>

public external fun getFirestore(app: FirebaseApp? = definedExternally, databaseId: String? = definedExternally): Firestore

public external fun increment(n: Int): FieldValue

public external fun initializeFirestore(app: FirebaseApp, settings: JsAny? = definedExternally, databaseId: String? = definedExternally): Firestore

public external fun limit(limit: Double): QueryConstraint

public external fun onSnapshot(
    reference: DocumentReference,
    next: (snapshot: DocumentSnapshot) -> Unit,
    error: (error: JsAny) -> Unit,
): Unsubscribe

public external fun onSnapshot(
    reference: DocumentReference,
    options: JsAny,
    next: (snapshot: DocumentSnapshot) -> Unit,
    error: (error: JsAny) -> Unit,
): Unsubscribe

public external fun onSnapshot(
    reference: Query,
    next: (snapshot: QuerySnapshot) -> Unit,
    error: (error: JsAny) -> Unit,
): Unsubscribe

public external fun onSnapshot(
    reference: Query,
    options: JsAny,
    next: (snapshot: QuerySnapshot) -> Unit,
    error: (error: JsAny) -> Unit,
): Unsubscribe

public external fun orderBy(field: String, direction: String): QueryConstraint

public external fun orderBy(field: FieldPath, direction: String): QueryConstraint

public external fun query(query: Query, vararg queryConstraints: QueryConstraint): Query

public external fun runTransaction(
    firestore: Firestore,
    updateFunction: (transaction: Transaction) -> Promise<JsAny?>,
    options: JsAny? = definedExternally,
): Promise<JsAny?>

public external fun serverTimestamp(): FieldValue

public external fun setDoc(
    documentReference: DocumentReference,
    data: JsAny,
    options: JsAny? = definedExternally,
): Promise<JsAny?>

public external fun setLogLevel(logLevel: String)

public external fun startAfter(document: DocumentSnapshot): QueryConstraint

public external fun startAfter(vararg fieldValues: JsAny?): QueryConstraint

public external fun startAt(document: DocumentSnapshot): QueryConstraint

public external fun startAt(vararg fieldValues: JsAny?): QueryConstraint

public external fun updateDoc(reference: DocumentReference, data: JsAny): Promise<JsAny?>

public external fun updateDoc(
    reference: DocumentReference,
    field: String,
    value: JsAny?,
    vararg moreFieldsAndValues: JsAny?,
): Promise<JsAny?>

public external fun updateDoc(
    reference: DocumentReference,
    field: FieldPath,
    value: JsAny?,
    vararg moreFieldsAndValues: JsAny?,
): Promise<JsAny?>

public external fun where(field: String, opStr: String, value: JsAny?): QueryConstraint

public external fun where(field: FieldPath, opStr: String, value: JsAny?): QueryConstraint

public external fun and(vararg queryConstraints: QueryConstraint): QueryConstraint

public external fun or(vararg queryConstraints: QueryConstraint): QueryConstraint

public external fun writeBatch(firestore: Firestore): WriteBatch

public external interface Firestore : JsAny {
    public val app: FirebaseApp
}

public external class GeoPoint(latitude: Double, longitude: Double) : JsAny {
    public val latitude: Double
    public val longitude: Double
    public fun isEqual(other: GeoPoint): Boolean
}

public external interface CollectionReference : Query {
    public val id: String
    public val path: String
    public val parent: DocumentReference?
}

public external interface DocumentChange : JsAny {
    public val doc: DocumentSnapshot
    public val newIndex: Int
    public val oldIndex: Int
    public val type: String
}

public external class DocumentReference : JsAny {
    public val id: String
    public val path: String
    public val parent: CollectionReference
}

public external interface DocumentSnapshot : JsAny {
    public val id: String
    public val ref: DocumentReference
    public val metadata: SnapshotMetadata
    public fun data(options: JsAny? = definedExternally): JsAny?
    public fun exists(): Boolean
    public fun get(fieldPath: String, options: JsAny? = definedExternally): JsAny?
    public fun get(fieldPath: FieldPath, options: JsAny? = definedExternally): JsAny?
}

public external class FieldValue : JsAny {
    public fun isEqual(other: FieldValue): Boolean
}

public external interface Query : JsAny

public external interface QueryConstraint : JsAny

public external interface QuerySnapshot : JsAny {
    public val docs: JsArray<DocumentSnapshot>
    public val empty: Boolean
    public val metadata: SnapshotMetadata
    public fun docChanges(): JsArray<DocumentChange>
}

public external interface SnapshotMetadata : JsAny {
    public val hasPendingWrites: Boolean
    public val fromCache: Boolean
}

public external interface Transaction : JsAny {
    public fun get(documentReference: DocumentReference): Promise<DocumentSnapshot>

    public fun set(
        documentReference: DocumentReference,
        data: JsAny,
        options: JsAny? = definedExternally,
    ): Transaction

    public fun update(documentReference: DocumentReference, data: JsAny): Transaction

    public fun update(
        documentReference: DocumentReference,
        field: String,
        value: JsAny?,
        vararg moreFieldsAndValues: JsAny?,
    ): Transaction

    public fun update(
        documentReference: DocumentReference,
        field: FieldPath,
        value: JsAny?,
        vararg moreFieldsAndValues: JsAny?,
    ): Transaction

    public fun delete(documentReference: DocumentReference): Transaction
}

public external interface WriteBatch : JsAny {
    public fun commit(): Promise<JsAny?>

    public fun delete(documentReference: DocumentReference): WriteBatch

    public fun set(
        documentReference: DocumentReference,
        data: JsAny,
        options: JsAny? = definedExternally,
    ): WriteBatch

    public fun update(documentReference: DocumentReference, data: JsAny): WriteBatch

    public fun update(
        documentReference: DocumentReference,
        field: String,
        value: JsAny?,
        vararg moreFieldsAndValues: JsAny?,
    ): WriteBatch

    public fun update(
        documentReference: DocumentReference,
        field: FieldPath,
        value: JsAny?,
        vararg moreFieldsAndValues: JsAny?,
    ): WriteBatch
}

public external class Timestamp(seconds: Double, nanoseconds: Double) : JsAny {
    public companion object {
        public fun now(): Timestamp
    }

    public val seconds: Double
    public val nanoseconds: Double
    public fun toMillis(): Double

    public fun isEqual(other: Timestamp): Boolean
}

public external interface FirestoreLocalCache : JsAny {
    public val kind: String
}

public external interface MemoryLocalCache : FirestoreLocalCache
public external interface PersistentLocalCache : FirestoreLocalCache

public external interface MemoryCacheSettings : JsAny {
    public val garbageCollector: MemoryGarbageCollector
}

public external interface MemoryGarbageCollector : JsAny {
    public val kind: String
}

public external interface MemoryLruGarbageCollector : MemoryGarbageCollector
public external interface MemoryEagerGarbageCollector : MemoryGarbageCollector

public external interface PersistentCacheSettings : JsAny {
    public val cacheSizeBytes: Int
    public val tabManager: PersistentTabManager
}

public external interface PersistentTabManager : JsAny {
    public val kind: String
}

public external fun memoryLocalCache(settings: JsAny): MemoryLocalCache
public external fun memoryEagerGarbageCollector(): MemoryEagerGarbageCollector
public external fun memoryLruGarbageCollector(settings: JsAny? = definedExternally): MemoryLruGarbageCollector
public external fun persistentLocalCache(settings: JsAny): PersistentLocalCache
public external fun persistentSingleTabManager(settings: JsAny? = definedExternally): PersistentTabManager
public external fun persistentMultipleTabManager(): PersistentTabManager
