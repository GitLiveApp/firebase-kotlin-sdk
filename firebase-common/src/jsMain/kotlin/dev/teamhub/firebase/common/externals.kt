package dev.teamhub.firebase.common

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("firebase/functions")
external object functions

@JsModule("firebase/auth")
external object auth

@JsModule("firebase/app")
external object firebase {
    object firestore {
        abstract class FieldValue
    }

    open class App {
        val name: String
        val options: Options
        fun functions(region: String? = definedExternally): functions.Functions
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

}
