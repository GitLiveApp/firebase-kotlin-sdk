package dev.teamhub.firebase.auth

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException

actual val Firebase.auth: FirebaseAuth
    get() = kotlin.TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

actual fun Firebase.auth(app: FirebaseApp): FirebaseAuth {
    kotlin.TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual class FirebaseAuth {
    actual val currentUser: FirebaseUser?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val authStateChanged: kotlinx.coroutines.flow.Flow<FirebaseUser?>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual suspend fun signInWithCustomToken(token: String): AuthResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend fun signInAnonymously(): AuthResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend fun signOut() {
    }
}

actual class AuthResult {
    actual val user: FirebaseUser?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

actual class FirebaseUser {
    actual val uid: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val isAnonymous: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual suspend fun delete() {
    }

    actual suspend fun reload() {
    }
}

actual open class FirebaseAuthException : FirebaseException()
actual class FirebaseAuthActionCodeException : FirebaseAuthException()
actual class FirebaseAuthEmailException : FirebaseAuthException()
actual class FirebaseAuthInvalidCredentialsException : FirebaseAuthException()
actual class FirebaseAuthInvalidUserException : FirebaseAuthException()
actual class FirebaseAuthRecentLoginRequiredException : FirebaseAuthException()
actual class FirebaseAuthUserCollisionException : FirebaseAuthException()
actual class FirebaseAuthWebException : FirebaseAuthException()