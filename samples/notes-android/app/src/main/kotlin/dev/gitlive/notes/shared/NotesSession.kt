// KMP: this file moves to commonMain unchanged.
package dev.gitlive.notes.shared

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/** Who is signed in. */
class NotesSession(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val user: FirebaseUser? get() = auth.currentUser

    /** Signs in anonymously unless a user is already signed in. */
    suspend fun signIn(): FirebaseUser = auth.currentUser ?: auth.signInAnonymously().await().user!!

    /** Signs in with an email and password, creating the account on the first visit. */
    suspend fun signIn(email: String, password: String): FirebaseUser = try {
        auth.signInWithEmailAndPassword(email, password).await().user!!
    } catch (e: FirebaseAuthInvalidUserException) {
        auth.createUserWithEmailAndPassword(email, password).await().user!!
    }

    fun onUserChanged(listener: (FirebaseUser?) -> Unit): FirebaseAuth.AuthStateListener =
        FirebaseAuth.AuthStateListener { listener(it.currentUser) }.also(auth::addAuthStateListener)

    fun signOut() = auth.signOut()
}
