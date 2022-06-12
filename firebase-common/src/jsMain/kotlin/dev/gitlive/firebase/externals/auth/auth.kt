@file:JsModule("firebase/auth")
@file:JsNonModule

package dev.gitlive.firebase.externals.auth

import dev.gitlive.firebase.Unsubscribe
import dev.gitlive.firebase.externals.app.FirebaseApp
import kotlin.js.Json
import kotlin.js.Promise

external fun applyActionCode(auth: Auth, code: String): Promise<Unit>

external fun checkActionCode(auth: Auth, code: String): Promise<ActionCodeInfo>

external fun confirmPasswordReset(auth: Auth, code: String, newPassword: String): Promise<Unit>

external fun connectAuthEmulator(auth: Auth, url: String, options: Any? = definedExternally)

external fun createUserWithEmailAndPassword(
    auth: Auth,
    email: String,
    password: String
): Promise<AuthResult>

external fun deleteUser(user: User): Promise<Unit>

external fun fetchSignInMethodsForEmail(auth: Auth, email: String): Promise<Array<String>>

external fun getAuth(app: FirebaseApp? = definedExternally): Auth

external fun getIdToken(user: User, forceRefresh: Boolean?): Promise<String>

external fun getIdTokenResult(user: User, forceRefresh: Boolean?): Promise<IdTokenResult>

external fun isSignInWithEmailLink(auth: Auth, link: String): Boolean

external fun linkWithCredential(user: User, credential: AuthCredential): Promise<AuthResult>

external fun multiFactor(user: User): MultiFactorUser

external fun onAuthStateChanged(auth: Auth, nextOrObserver: (User?) -> Unit): Unsubscribe

external fun onIdTokenChanged(auth: Auth, nextOrObserver: (User?) -> Unit): Unsubscribe

external fun sendEmailVerification(user: User, actionCodeSettings: Any?): Promise<Unit>

external fun reauthenticateWithCredential(
    user: User,
    credential: AuthCredential
): Promise<AuthResult>

external fun reload(user: User): Promise<Unit>

external fun sendPasswordResetEmail(
    auth: Auth,
    email: String,
    actionCodeSettings: Any?
): Promise<Unit>

external fun sendSignInLinkToEmail(
    auth: Auth,
    email: String,
    actionCodeSettings: Any?
): Promise<Unit>

external fun signInAnonymously(auth: Auth): Promise<AuthResult>

external fun signInWithCredential(auth: Auth, authCredential: AuthCredential): Promise<AuthResult>

external fun signInWithCustomToken(auth: Auth, token: String): Promise<AuthResult>

external fun signInWithEmailAndPassword(
    auth: Auth,
    email: String,
    password: String
): Promise<AuthResult>

external fun signInWithEmailLink(auth: Auth, email: String, link: String): Promise<AuthResult>

external fun signOut(auth: Auth): Promise<Unit>

external fun unlink(user: User, providerId: String): Promise<User>

external fun updateCurrentUser(auth: Auth, user: User?): Promise<Unit>

external fun updateEmail(user: User, newEmail: String): Promise<Unit>

external fun updatePassword(user: User, newPassword: String): Promise<Unit>

external fun updatePhoneNumber(user: User, phoneCredential: AuthCredential): Promise<Unit>

external fun updateProfile(user: User, profile: ProfileUpdateRequest): Promise<Unit>

external fun verifyBeforeUpdateEmail(
    user: User,
    newEmail: String,
    actionCodeSettings: Any?
): Promise<Unit>

external fun verifyPasswordResetCode(auth: Auth, code: String): Promise<String>

external interface Auth {
    val currentUser: User?
    var languageCode: String?

    fun onAuthStateChanged(nextOrObserver: (User?) -> Unit): Unsubscribe
    fun onIdTokenChanged(nextOrObserver: (User?) -> Unit): Unsubscribe
    fun signOut(): Promise<Unit>
    fun updateCurrentUser(user: User?): Promise<Unit>
}

external interface UserInfo {
    val displayName: String?
    val email: String?
    val phoneNumber: String?
    val photoURL: String?
    val providerId: String
    val uid: String
}

external interface User : UserInfo {
    val emailVerified: Boolean
    val isAnonymous: Boolean
    val metadata: UserMetadata
    val providerData: Array<UserInfo>
    val refreshToken: String
    val tenantId: String?

    fun delete(): Promise<Unit>
    fun getIdToken(forceRefresh: Boolean?): Promise<String>
    fun getIdTokenResult(forceRefresh: Boolean?): Promise<IdTokenResult>
    fun reload(): Promise<Unit>
}

external interface UserMetadata {
    val creationTime: String?
    val lastSignInTime: String?
}

external interface IdTokenResult {
    val authTime: String
    val claims: Json
    val expirationTime: String
    val issuedAtTime: String
    val signInProvider: String?
    val signInSecondFactor: String?
    val token: String
}

external interface ActionCodeInfo {
    val operation: String
    val data: ActionCodeData
}

external interface ActionCodeData {
    val email: String?
    val multiFactorInfo: MultiFactorInfo?
    val previousEmail: String?
}

external interface AuthResult {
    val credential: AuthCredential?
    val operationType: String?
    val user: User?
}

external interface AuthCredential {
    val providerId: String
    val signInMethod: String
}

external interface ProfileUpdateRequest {
    val displayName: String?
    val photoURL: String?
}

external interface MultiFactorUser {
    val enrolledFactors: Array<MultiFactorInfo>

    fun enroll(assertion: MultiFactorAssertion, displayName: String?): Promise<Unit>
    fun getSession(): Promise<MultiFactorSession>
    fun unenroll(option: MultiFactorInfo): Promise<Unit>
    fun unenroll(option: String): Promise<Unit>
}

external interface MultiFactorInfo {
    val displayName: String?
    val enrollmentTime: String
    val factorId: String
    val uid: String
}

external interface MultiFactorAssertion {
    val factorId: String
}

external interface MultiFactorSession

external interface MultiFactorResolver {
    val auth: Auth
    val hints: Array<MultiFactorInfo>
    val session: MultiFactorSession

    fun resolveSignIn(assertion: MultiFactorAssertion): Promise<AuthResult>
}

external interface AuthProvider

external object EmailAuthProvider : AuthProvider {
    fun credential(email: String, password: String): AuthCredential
    fun credentialWithLink(email: String, emailLink: String): AuthCredential
}

external object FacebookAuthProvider : AuthProvider {
    fun credential(token: String): AuthCredential
}

external object GithubAuthProvider : AuthProvider {
    fun credential(token: String): AuthCredential
}

external object GoogleAuthProvider : AuthProvider {
    fun credential(idToken: String?, accessToken: String?): AuthCredential
}

external class OAuthProvider(providerId: String) : AuthProvider {
    val providerId: String
    fun credential(optionsOrIdToken: Any?, accessToken: String?): AuthCredential

    fun addScope(scope: String)
    fun setCustomParameters(customOAuthParameters: Map<String, String>)
}

external interface OAuthCredentialOptions {
    val accessToken: String?
    val idToken: String?
    val rawNonce: String?
}

external class PhoneAuthProvider(auth: Auth?) : AuthProvider {
    companion object {
        fun credential(
            verificationId: String,
            verificationCode: String
        ): AuthCredential
    }

    fun verifyPhoneNumber(
        phoneInfoOptions: String,
        applicationVerifier: ApplicationVerifier
    ): Promise<String>
}

external interface ApplicationVerifier {
    val type: String
    fun verify(): Promise<String>
}

external object TwitterAuthProvider : AuthProvider {
    fun credential(token: String, secret: String): AuthCredential
}
