@file:JsModule("firebase/database")
@file:JsNonModule

package dev.gitlive.firebase.database.externals

import dev.gitlive.firebase.Unsubscribe
import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun child(parent: DatabaseReference, path: String): DatabaseReference

public external fun connectDatabaseEmulator(
    db: Database,
    host: String,
    port: Int,
    options: Any? = definedExternally,
)

public external fun enableLogging(enabled: Boolean?, persistent: Boolean? = definedExternally)

public external fun endAt(value: Any?, key: String? = definedExternally): QueryConstraint

public external fun endBefore(value: Any?, key: String? = definedExternally): QueryConstraint

public external fun equalTo(value: Any?, key: String? = definedExternally): QueryConstraint

public external fun get(query: Query): Promise<DataSnapshot>

public external fun getDatabase(
    app: FirebaseApp? = definedExternally,
    url: String? = definedExternally,
): Database

public external fun increment(delta: Double): Any

public external fun limitToFirst(limit: Int): QueryConstraint

public external fun limitToLast(limit: Int): QueryConstraint

public external fun off(query: Query, eventType: String?, callback: Any?)

public external fun goOffline(db: Database)

public external fun goOnline(db: Database)

public external fun onChildAdded(
    query: Query,
    callback: ChangeSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

public external fun onChildChanged(
    query: Query,
    callback: ChangeSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

public external fun onChildMoved(
    query: Query,
    callback: ChangeSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

public external fun onChildRemoved(
    query: Query,
    callback: ChangeSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

public external fun onValue(
    query: Query,
    callback: ValueSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

public external fun onDisconnect(ref: DatabaseReference): OnDisconnect

public external fun orderByChild(path: String): QueryConstraint

public external fun orderByKey(): QueryConstraint

public external fun orderByValue(): QueryConstraint

public external fun push(parent: DatabaseReference, value: Any? = definedExternally): ThenableReference

public external fun query(query: Query, vararg queryConstraints: QueryConstraint): Query

public external fun ref(db: Database, path: String? = definedExternally): DatabaseReference

public external fun remove(ref: DatabaseReference): Promise<Unit>

public external fun serverTimestamp(): Any

public external fun set(ref: DatabaseReference, value: Any?): Promise<Unit>

public external fun startAfter(value: Any?, key: String? = definedExternally): QueryConstraint

public external fun startAt(value: Any?, key: String? = definedExternally): QueryConstraint

public external fun update(ref: DatabaseReference, values: Any): Promise<Unit>

public external fun <T> runTransaction(
    ref: DatabaseReference,
    transactionUpdate: (currentData: T) -> T,
    options: Any? = definedExternally,
): Promise<TransactionResult>

public external interface Database {
    public val app: FirebaseApp
}

public external interface Query {
    public val ref: DatabaseReference
}

public external interface QueryConstraint

public external interface DatabaseReference : Query {
    public val key: String?
    public val parent: DatabaseReference?
    public val root: DatabaseReference
}

public external interface ThenableReference : DatabaseReference

public external interface DataSnapshot {
    public val key: String?
    public val size: Int
    public val ref: DatabaseReference
    public fun `val`(): Any
    public fun exists(): Boolean
    public fun forEach(action: (a: DataSnapshot) -> Boolean): Boolean
    public fun child(path: String): DataSnapshot
    public fun hasChildren(): Boolean
}

public external interface OnDisconnect {
    public fun cancel(): Promise<Unit>
    public fun remove(): Promise<Unit>
    public fun set(value: Any?): Promise<Unit>
    public fun update(value: Any): Promise<Unit>
}

public external interface TransactionResult {
    public val committed: Boolean
    public val snapshot: DataSnapshot
}
