package dev.teamhub.firebase

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("firebase/functions")
external object functions

@JsModule("firebase/auth")
external object auth

@JsModule("firebase/database")
external object database

@JsModule("firebase/firestore")
external object firestore

typealias SnapshotCallback = (data: firebase.database.DataSnapshot, b: String?) -> Unit

@JsModule("firebase/app")
external object firebase {

    open class App {
        val name: String
        val options: Options
        fun functions(region: String? = definedExternally): functions.Functions
        fun database(url: String? = definedExternally): database.Database
        fun firestore(): firestore.Firestore
    }

    interface Options {
        val applicationId: String
        val apiKey: String
        val databaseUrl: String?
        val gaTrackingId: String?
        val storageBucket: String?
        val projectId: String?
    }

    val apps : Array<App>
    fun app(name: String? = definedExternally): App
    fun initializeApp(options: Any, name: String? = definedExternally) : App

    interface FirebaseError {
        var code: String
        var message: String
        var name: String
    }

    fun auth(app: App? = definedExternally): auth.Auth

    object auth {
        open class Auth {
            val currentUser: user.User?

            fun signInWithCustomToken(token: String): Promise<AuthResult>
            fun signInAnonymously(): Promise<AuthResult>
            fun signOut(): Promise<Unit>

            fun onAuthStateChanged(nextOrObserver: (user.User?) -> Unit): () -> Unit
        }
        interface AuthResult {
            val user: user.User?
        }
    }

    fun User(a: Any,b: Any,c: Any): user.User

    object user {
        abstract class User {
            val uid: String
            val isAnonymous: Boolean

            fun delete(): Promise<Unit>
            fun reload(): Promise<Unit>
        }
    }

    fun functions(app: App? = definedExternally): functions.Functions

    object functions {
        class Functions {
            fun httpsCallable(name: String, options: Json?): HttpsCallable
        }
        interface HttpsCallableResult {
            val data: Any?
        }
        interface HttpsCallable {
            operator fun invoke(data: Any? = definedExternally): Promise<HttpsCallableResult>
        }

    }

    fun database(app: App? = definedExternally): database.Database

    object database {
        fun enableLogging(logger: Boolean?, persistent: Boolean? = definedExternally)

        open class Database {
            fun ref(path: String? = definedExternally): Reference
        }
        open class ThenableReference : Reference


        open class Query {
            fun on(eventType: String?, callback: SnapshotCallback, cancelCallbackOrContext: (error: Error) -> Unit? = definedExternally, context: Any? = definedExternally): SnapshotCallback
            fun off(eventType: String?, callback: SnapshotCallback?, context: Any? = definedExternally)
            fun once(eventType: String, callback: SnapshotCallback, failureCallbackOrContext: (error: Error) -> Unit? = definedExternally, context: Any? = definedExternally): SnapshotCallback
            fun orderByChild(path: String): Query
            fun orderByKey(): Query
            fun startAt(value: Any, key: String? = definedExternally): Query
        }

        open class Reference: Query {
            val key: String?
            fun child(path: String): Reference
            fun remove(): Promise<Unit>
            fun onDisconnect(): OnDisconnect
            fun update(value: Any?): Promise<Unit>
            fun set(value: Any?): Promise<Unit>
            fun push(): ThenableReference
        }

        open class DataSnapshot {
            val key: String?
            fun `val`(): Any
            fun exists(): Boolean
            fun forEach(action: (a: DataSnapshot) -> Boolean): Boolean
            fun numChildren(): Int
            fun child(path: String): DataSnapshot
        }

        open class OnDisconnect {
            fun update(value: Any?): Promise<Unit>
            fun remove(): Promise<Unit>
            fun cancel(): Promise<Unit>
            fun set(value: Any?): Promise<Unit>
        }

        object ServerValue {
            val TIMESTAMP: Any
        }
    }

    fun firestore(): firestore.Firestore

    object firestore {
        fun setLogLevel(level: String)

        open class PersistenceSettings {
            var experimentalTabSynchronization: Boolean
        }

        open class Firestore {
            fun <T> runTransaction(func: (transaction: Transaction) -> Promise<T>): Promise<T>
            fun batch(): WriteBatch
            fun collection(collectionPath: String): CollectionReference
            fun doc(documentPath: String): DocumentReference
            fun settings(settings: Json)
            fun enablePersistence(): Promise<Unit>
        }

        open class FieldPath constructor(fieldNames: Array<out String>)

        open class Query {
            fun get(options: Any? = definedExternally): Promise<QuerySnapshot>
            fun where(field: String, opStr: String, value: Any?): Query
            fun where(path: FieldPath, opStr: String, value: Any?): Query
            fun onSnapshot(next: (snapshot: QuerySnapshot) -> Unit, error: (error: Error) -> Unit): ()->Unit
        }

        open class CollectionReference : Query {
            val path: String
            fun add(data: Any): Promise<DocumentReference>
        }

        open class QuerySnapshot {
            val docs: Array<DocumentSnapshot>
            val empty: Boolean
        }

        open class DocumentSnapshot {
            val id: String
            val ref: DocumentReference
            val exists: Boolean
            fun data(options: Any? = definedExternally): Any?
            fun get(fieldPath: Any, options: Any? = definedExternally): Any?
        }

        open class DocumentReference {
            val id: String
            val path: String

            fun get(options: Any? = definedExternally): Promise<DocumentSnapshot>
            fun set(data: Any, options: Any? = definedExternally): Promise<Unit>
            fun update(data: Any): Promise<Unit>
            fun update(field: Any, value: Any?, vararg moreFieldsAndValues: Any?): Promise<Unit>
            fun delete(): Promise<Unit>
            fun onSnapshot(next: (snapshot: DocumentSnapshot) -> Unit, error: (error: Error) -> Unit): ()->Unit
        }

        open class WriteBatch {
            fun commit(): Promise<Unit>
            fun delete(documentReference: DocumentReference): WriteBatch
            fun set(documentReference: DocumentReference, data: Any, options: Any? = definedExternally): WriteBatch
            fun update(documentReference: DocumentReference, data: Any): WriteBatch
            fun update(documentReference: DocumentReference, field: Any, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch
        }

        open class Transaction {
            fun get(documentReference: DocumentReference): Promise<DocumentSnapshot>
            fun set(documentReference: DocumentReference, data: Any, options: Any? = definedExternally): Transaction
            fun update(documentReference: DocumentReference, data: Any): Transaction
            fun update(documentReference: DocumentReference, field: Any, value: Any?, vararg moreFieldsAndValues: Any?): Transaction
            fun delete(documentReference: DocumentReference): Transaction
        }

        abstract class FieldValue {
            companion object {
                fun delete(): FieldValue
                fun arrayRemove(vararg elements: Any): FieldValue
                fun arrayUnion(vararg elements: Any): FieldValue
            }
        }
    }
}
