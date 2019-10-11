package dev.teamhub.firebase.auth

import kotlin.js.Promise

@JsModule("firebase/auth")
external object auth

@JsModule("firebase/app")
external object firebase {

    open class App
    fun auth(): auth.Auth
    object auth {
        open class Auth {
            val currentUser: user.User?

            fun signInWithCustomToken(token: String): Promise<AuthResult>
            fun signInAnonymously(): Promise<AuthResult>
            fun signOut(): Promise<Unit>

            fun onAuthStateChanged(nextOrObserver: (user.User) -> Unit): () -> Unit
        }
        interface AuthResult {
            val user: user.User
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
}