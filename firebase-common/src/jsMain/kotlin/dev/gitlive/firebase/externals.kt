/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JsModule("firebase/compat/app")

package dev.gitlive.firebase

import kotlin.js.Json
import kotlin.js.Promise

@JsName("default")
external object firebase {

    open class App {
        fun functions(region: String? = definedExternally): functions.Functions
        fun firestore(): firestore.Firestore
    }

    fun app(name: String? = definedExternally): App

    interface FirebaseError {
        var code: String
        var message: String
        var name: String
    }

    fun functions(app: App? = definedExternally): functions.Functions

    object functions {
        class Functions {
            fun httpsCallable(name: String, options: Json?): HttpsCallable
            fun useFunctionsEmulator(origin: String)
            fun useEmulator(host: String, port: Int)
        }
        interface HttpsCallableResult {
            val data: Any?
        }
        interface HttpsCallable {
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
            fun collectionGroup(collectionId: String): Query
            fun doc(documentPath: String): DocumentReference
            fun settings(settings: Json)
            fun enablePersistence(): Promise<Unit>
            fun clearPersistence(): Promise<Unit>
            fun useEmulator(host: String, port: Int)
            fun disableNetwork(): Promise<Unit>
            fun enableNetwork(): Promise<Unit>
        }

        open class Timestamp {
            val seconds: Double
            val nanoseconds: Double
            fun toMillis(): Double
        }

        open class Query {
            fun get(options: Any? = definedExternally): Promise<QuerySnapshot>
            fun where(field: String, opStr: String, value: Any?): Query
            fun where(field: FieldPath, opStr: String, value: Any?): Query
            fun onSnapshot(next: (snapshot: QuerySnapshot) -> Unit, error: (error: Error) -> Unit): () -> Unit
            fun onSnapshot(options: Json, next: (snapshot: QuerySnapshot) -> Unit, error: (error: Error) -> Unit): () -> Unit
            fun limit(limit: Double): Query
            fun orderBy(field: String, direction: Any): Query
            fun orderBy(field: FieldPath, direction: Any): Query
            fun startAfter(document: DocumentSnapshot): Query
            fun startAfter(vararg fieldValues: Any): Query
            fun startAt(document: DocumentSnapshot): Query
            fun startAt(vararg fieldValues: Any): Query
            fun endBefore(document: DocumentSnapshot): Query
            fun endBefore(vararg fieldValues: Any): Query
            fun endAt(document: DocumentSnapshot): Query
            fun endAt(vararg fieldValues: Any): Query
        }

        open class CollectionReference : Query {
            val path: String
            val parent: DocumentReference?
            fun doc(path: String = definedExternally): DocumentReference
            fun add(data: Any): Promise<DocumentReference>
        }

        open class QuerySnapshot {
            val docs: Array<DocumentSnapshot>
            fun docChanges(): Array<DocumentChange>
            val empty: Boolean
            val metadata: SnapshotMetadata
        }

        open class DocumentChange {
            val doc: DocumentSnapshot
            val newIndex: Int
            val oldIndex: Int
            val type: String
        }

        open class DocumentSnapshot {
            val id: String
            val ref: DocumentReference
            val exists: Boolean
            val metadata: SnapshotMetadata
            fun data(options: Any? = definedExternally): Any?
            fun get(fieldPath: String, options: Any? = definedExternally): Any?
            fun get(fieldPath: FieldPath, options: Any? = definedExternally): Any?
        }

        open class SnapshotMetadata {
            val hasPendingWrites: Boolean
            val fromCache: Boolean
        }

        open class DocumentReference {
            val id: String
            val path: String
            val parent: CollectionReference

            fun collection(path: String): CollectionReference
            fun get(options: Any? = definedExternally): Promise<DocumentSnapshot>
            fun set(data: Any, options: Any? = definedExternally): Promise<Unit>
            fun update(data: Any): Promise<Unit>
            fun update(field: String, value: Any?, vararg moreFieldsAndValues: Any?): Promise<Unit>
            fun update(field: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Promise<Unit>
            fun delete(): Promise<Unit>
            fun onSnapshot(next: (snapshot: DocumentSnapshot) -> Unit, error: (error: Error) -> Unit): ()->Unit
        }

        open class WriteBatch {
            fun commit(): Promise<Unit>
            fun delete(documentReference: DocumentReference): WriteBatch
            fun set(documentReference: DocumentReference, data: Any, options: Any? = definedExternally): WriteBatch
            fun update(documentReference: DocumentReference, data: Any): WriteBatch
            fun update(documentReference: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch
            fun update(documentReference: DocumentReference, field: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch
        }

        open class Transaction {
            fun get(documentReference: DocumentReference): Promise<DocumentSnapshot>
            fun set(documentReference: DocumentReference, data: Any, options: Any? = definedExternally): Transaction
            fun update(documentReference: DocumentReference, data: Any): Transaction
            fun update(documentReference: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): Transaction
            fun update(documentReference: DocumentReference, field: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Transaction
            fun delete(documentReference: DocumentReference): Transaction
        }

        open class FieldPath(vararg fieldNames: String) {
            companion object {
                val documentId: FieldPath
            }
        }

        abstract class FieldValue {
            companion object {
                fun serverTimestamp(): FieldValue
                fun delete(): FieldValue
                fun increment(value: Int): FieldValue
                fun arrayRemove(vararg elements: Any): FieldValue
                fun arrayUnion(vararg elements: Any): FieldValue
            }
        }
    }

    fun remoteConfig(app: App? = definedExternally): remoteConfig.RemoteConfig

    object remoteConfig {
        interface RemoteConfig {
            var defaultConfig: Any
            var fetchTimeMillis: Long
            var lastFetchStatus: String
            val settings: Settings
            fun activate(): Promise<Boolean>
            fun ensureInitialized(): Promise<Unit>
            fun fetch(): Promise<Unit>
            fun fetchAndActivate(): Promise<Boolean>
            fun getAll(): Json
            fun getBoolean(key: String): Boolean
            fun getNumber(key: String): Number
            fun getString(key: String): String?
            fun getValue(key: String): Value
        }

        interface Settings {
            var fetchTimeoutMillis: Number
            var minimumFetchIntervalMillis: Number
        }

        interface Value {
            fun asBoolean(): Boolean
            fun asNumber(): Number
            fun asString(): String?
            fun getSource(): String
        }
    }

    fun installations(app: App? = definedExternally): installations.Installations

    object installations {
        interface Installations {
            fun delete(): Promise<Unit>
            fun getId(): Promise<String>
            fun getToken(forceRefresh: Boolean): Promise<String>
        }
    }
}
