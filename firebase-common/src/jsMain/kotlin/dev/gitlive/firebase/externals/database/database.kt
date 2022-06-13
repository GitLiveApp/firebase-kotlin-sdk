@file:JsModule("firebase/database")
@file:JsNonModule

package dev.gitlive.firebase.externals.database

import dev.gitlive.firebase.*
import dev.gitlive.firebase.externals.app.FirebaseApp
import kotlin.js.Promise

external fun child(parent: DatabaseReference, path: String): DatabaseReference

external fun connectDatabaseEmulator(
    db: Database,
    host: String,
    port: Int,
    options: Any? = definedExternally
)

external fun enableLogging(enabled: Boolean?, persistent: Boolean? = definedExternally)

external fun endAt(value: Any?, key: String? = definedExternally): QueryConstraint

external fun endBefore(value: Any?, key: String? = definedExternally): QueryConstraint

external fun equalTo(value: Any?, key: String? = definedExternally): QueryConstraint

external fun get(query: Query): Promise<DataSnapshot>

external fun getDatabase(
    app: FirebaseApp? = definedExternally,
    url: String? = definedExternally
): Database

external fun increment(delta: Long): Any

external fun limitToFirst(limit: Int): QueryConstraint

external fun limitToLast(limit: Int): QueryConstraint

external fun off(query: Query, eventType: String?, callback: Any?)

external fun goOffline(db: Database)

external fun goOnline(db: Database)

external fun onChildAdded(
    query: Query,
    callback: ChangeSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

external fun onChildChanged(
    query: Query,
    callback: ChangeSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

external fun onChildMoved(
    query: Query,
    callback: ChangeSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

external fun onChildRemoved(
    query: Query,
    callback: ChangeSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

external fun onValue(
    query: Query,
    callback: ValueSnapshotCallback,
    cancelCallback: CancelCallback? = definedExternally,
): Unsubscribe

external fun onDisconnect(ref: DatabaseReference): OnDisconnect

external fun orderByChild(path: String): QueryConstraint

external fun orderByKey(): QueryConstraint

external fun orderByValue(): QueryConstraint

external fun push(parent: DatabaseReference, value: Any? = definedExternally): ThenableReference

external fun query(query: Query, vararg queryConstraints: QueryConstraint): Query

external fun ref(db: Database, path: String? = definedExternally): DatabaseReference

external fun remove(ref: DatabaseReference): Promise<Unit>

external fun serverTimestamp(): Any

external fun set(ref: DatabaseReference, value: Any?): Promise<Unit>

external fun startAfter(value: Any?, key: String? = definedExternally): QueryConstraint

external fun startAt(value: Any?, key: String? = definedExternally): QueryConstraint

external fun update(ref: DatabaseReference, values: Any): Promise<Unit>

external interface Database {
    val app: FirebaseApp
}

external interface Query {
    val ref: DatabaseReference
}

external interface QueryConstraint

external interface DatabaseReference : Query {
    val key: String?
    val parent: DatabaseReference?
    val root: DatabaseReference
}

external interface ThenableReference : DatabaseReference

external interface DataSnapshot {
    val key: String?
    val size: Int
    fun `val`(): Any
    fun exists(): Boolean
    fun forEach(action: (a: DataSnapshot) -> Boolean): Boolean
    fun child(path: String): DataSnapshot
}

external interface OnDisconnect {
    fun cancel(): Promise<Unit>
    fun remove(): Promise<Unit>
    fun set(value: Any?): Promise<Unit>
    fun update(value: Any): Promise<Unit>
}
