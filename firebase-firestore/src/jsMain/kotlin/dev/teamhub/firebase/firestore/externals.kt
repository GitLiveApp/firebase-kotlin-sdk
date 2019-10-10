package dev.teamhub.firebase.firestore

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("firebase/app")
external object firebase {

    open class App
    fun firestore(): firestore.Firestore
    object firestore {
        fun setLogLevel(level: String)

        open class PersistenceSettings {
            var experimentalTabSynchronization: Boolean
        }

        open class Firestore {
            var _th_settings: dynamic
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
            fun where(fieldPath: Any, opStr: String, value: Any?): Query
            fun onSnapshot(next: (snapshot: QuerySnapshot) -> Unit, error: (error: Error) -> Unit): ()->Unit
        }

        open class CollectionReference : Query {
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

            fun get(options: Any? = definedExternally): Promise<DocumentSnapshot>
            fun set(data: Any, options: Any? = definedExternally): Promise<Unit>
            fun update(data: Any): Promise<Unit>
//            fun update(field: Any, value: Any?, vararg moreFieldsAndValues: Any?): Promise<Unit>
            fun delete(): Promise<Unit>
            fun onSnapshot(next: (snapshot: DocumentSnapshot) -> Unit, error: (error: Error) -> Unit): ()->Unit
        }

        open class WriteBatch {
            fun commit(): Promise<Unit>
//            fun delete(documentReference: DocumentReference): WriteBatch
//            fun set(documentReference: DocumentReference, data: Any, options: Any? = definedExternally): WriteBatch
//            fun update(documentReference: DocumentReference, data: Any): WriteBatch
//            fun update(documentReference: DocumentReference, field: Any, value: Any?, vararg moreFieldsAndValues: Any): WriteBatch
        }

        open class Transaction {
            fun get(documentRefence: DocumentReference): Promise<DocumentSnapshot>
//            fun set(documentReference: DocumentReference, data: Any, options: Any? = definedExternally): Transaction
//            fun update(documentReference: DocumentReference, data: Any): Transaction
//            fun update(documentReference: DocumentReference, field: Any, value: Any?, vararg moreFieldsAndValues: Any): Transaction
//            fun delete(documentReference: DocumentReference): Transaction
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