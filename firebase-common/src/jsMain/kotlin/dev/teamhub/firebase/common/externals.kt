package dev.teamhub.firebase.common

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("firebase/functions")
external object functions

@JsModule("firebase/auth")
external object auth

@JsModule("firebase/database")
external object database

@JsModule("firebase/app")
external object firebase {
    object firestore {
        abstract class FieldValue
    }

    open class App {
        val name: String
        val options: Options
        fun functions(region: String? = definedExternally): functions.Functions
        fun database(url: String? = definedExternally): database.Database
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

    @JsModule("firebase/auth")
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
            val data: Any
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

        open class Reference {
            fun remove(): Promise<Unit>
            fun onDisconnect(): OnDisconnect

            fun update(value: Any?): Promise<Unit>
            fun set(value: Any?): Promise<Unit>
            fun on(eventType: String?, callback: (data: DataSnapshot) -> Unit, cancelCallbackOrContext: (error: Error) -> Unit? = definedExternally, context: Any? = definedExternally): (DataSnapshot) -> Unit
            fun off(eventType: String?, callback: (data: DataSnapshot) -> Unit, context: Any? = definedExternally)
            fun once(eventType: String, callback: (data: DataSnapshot) -> Unit, failureCallbackOrContext: (error: Error) -> Unit? = definedExternally, context: Any? = definedExternally): (DataSnapshot)->Unit
            fun push(): ThenableReference
        }
        open class DataSnapshot {
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
            val TIMESTAMP: Map<String, String>
        }
    }
}
