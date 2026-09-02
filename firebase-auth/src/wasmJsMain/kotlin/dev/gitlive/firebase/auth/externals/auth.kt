@file:JsModule("firebase/auth")

package dev.gitlive.firebase.auth.externals

import dev.gitlive.firebase.Unsubscribe
import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun applyActionCode(auth: Auth, code: String): Promise<JsAny?>

public external fun checkActionCode(auth: Auth, code: String): Promise<ActionCodeInfo>

public external fun confirmPasswordReset(auth: Auth, code: String, newPassword: String): Promise<JsAny?>

public external fun connectAuthEmulator(auth: Auth, url: String, options: JsAny? = definedExternally)

public external fun createUserWithEmailAndPassword(
    auth: Auth,
    email: String,
    password: String,
): Promise<AuthResult>

public external fun deleteUser(user: User): Promise<JsAny?>

public external fun fetchSignInMethodsForEmail(auth: Auth, email: String): Promise<JsArray<JsString>>

public external fun getAuth(app: FirebaseApp? = definedExternally): Auth

public external fun initializeAuth(app: FirebaseApp? = definedExternally, deps: JsAny? = definedExternally): Auth

public external fun getIdToken(user: User, forceRefresh: Boolean?): Promise<JsString>

public external fun getIdTokenResult(user: User, forceRefresh: Boolean?): Promise<IdTokenResult>

public external fun isSignInWithEmailLink(auth: Auth, link: String): Boolean

public external fun linkWithCredential(user: User, credential: AuthCredential): Promise<AuthResult>

public external fun multiFactor(user: User): MultiFactorUser

public external fun onAuthStateChanged(auth: Auth, nextOrObserver: (User?) -> Unit): Unsubscribe

public external fun onIdTokenChanged(auth: Auth, nextOrObserver: (User?) -> Unit): Unsubscribe

public external fun sendEmailVerification(user: User, actionCodeSettings: JsAny?): Promise<JsAny?>

public external fun reauthenticateWithCredential(
    user: User,
    credential: AuthCredential,
): Promise<AuthResult>

public external fun reload(user: User): Promise<JsAny?>

public external fun sendPasswordResetEmail(
    auth: Auth,
    email: String,
    actionCodeSettings: JsAny?,
): Promise<JsAny?>

public external fun sendSignInLinkToEmail(
    auth: Auth,
    email: String,
    actionCodeSettings: JsAny?,
): Promise<JsAny?>

public external fun signInAnonymously(auth: Auth): Promise<AuthResult>

public external fun signInWithCredential(auth: Auth, authCredential: AuthCredential): Promise<AuthResult>

public external fun signInWithCustomToken(auth: Auth, token: String): Promise<AuthResult>

public external fun signInWithEmailAndPassword(
    auth: Auth,
    email: String,
    password: String,
): Promise<AuthResult>

public external fun signInWithEmailLink(auth: Auth, email: String, link: String): Promise<AuthResult>

public external fun signInWithPopup(auth: Auth, provider: AuthProvider): Promise<UserCredential>

public external fun signInWithRedirect(auth: Auth, provider: AuthProvider): Promise<JsAny?>

public external fun getRedirectResult(auth: Auth): Promise<UserCredential?>

public external fun signOut(auth: Auth): Promise<JsAny?>

public external fun unlink(user: User, providerId: String): Promise<User>

public external fun updateCurrentUser(auth: Auth, user: User?): Promise<JsAny?>

public external fun updateEmail(user: User, newEmail: String): Promise<JsAny?>

public external fun updatePassword(user: User, newPassword: String): Promise<JsAny?>

public external fun updatePhoneNumber(user: User, phoneCredential: AuthCredential): Promise<JsAny?>

public external fun updateProfile(user: User, profile: JsAny): Promise<JsAny?>

public external fun verifyBeforeUpdateEmail(
    user: User,
    newEmail: String,
    actionCodeSettings: JsAny?,
): Promise<JsAny?>

public external fun verifyPasswordResetCode(auth: Auth, code: String): Promise<JsString>

public external interface Auth : JsAny {
    public val currentUser: User?
    public var languageCode: String?

    public fun onAuthStateChanged(nextOrObserver: (User?) -> Unit): Unsubscribe
    public fun onIdTokenChanged(nextOrObserver: (User?) -> Unit): Unsubscribe
    public fun signOut(): Promise<JsAny?>
    public fun updateCurrentUser(user: User?): Promise<JsAny?>
}

public external interface UserInfo : JsAny {
    public val displayName: String?
    public val email: String?
    public val phoneNumber: String?
    public val photoURL: String?
    public val providerId: String
    public val uid: String
}

public external interface User : UserInfo {
    public val emailVerified: Boolean
    public val isAnonymous: Boolean
    public val metadata: UserMetadata
    public val providerData: JsArray<UserInfo>
    public val refreshToken: String
    public val tenantId: String?

    public fun delete(): Promise<JsAny?>
    public fun getIdToken(forceRefresh: Boolean?): Promise<JsString>
    public fun getIdTokenResult(forceRefresh: Boolean?): Promise<IdTokenResult>
    public fun reload(): Promise<JsAny?>
}

public external interface UserMetadata : JsAny {
    public val creationTime: String?
    public val lastSignInTime: String?
}

public external interface IdTokenResult : JsAny {
    public val authTime: String
    public val claims: JsAny
    public val expirationTime: String
    public val issuedAtTime: String
    public val signInProvider: String?
    public val signInSecondFactor: String?
    public val token: String
}

public external interface ActionCodeInfo : JsAny {
    public val operation: String
    public val data: ActionCodeData
}

public external interface ActionCodeData : JsAny {
    public val email: String?
    public val multiFactorInfo: MultiFactorInfo?
    public val previousEmail: String?
}

public external interface AuthResult : JsAny {
    public val credential: AuthCredential?
    public val operationType: String?
    public val user: User?
    public val additionalUserInfo: AdditionalUserInfo?
}

public external interface AdditionalUserInfo : JsAny {
    public val providerId: String?
    public val username: String?
    public val profile: JsAny?
    public val newUser: Boolean
}

public external interface AuthCredential : JsAny {
    public val providerId: String
    public val signInMethod: String
}

public external interface OAuthCredential : AuthCredential {
    public val accessToken: String?
    public val idToken: String?
    public val secret: String?
}

public external interface UserCredential : JsAny {
    public val operationType: String
    public val providerId: String?
    public val user: User
}

public external interface ProfileUpdateRequest : JsAny {
    public val displayName: String?
    public val photoURL: String?
}

public external interface MultiFactorUser : JsAny {
    public val enrolledFactors: JsArray<MultiFactorInfo>

    public fun enroll(assertion: MultiFactorAssertion, displayName: String?): Promise<JsAny?>
    public fun getSession(): Promise<MultiFactorSession>
    public fun unenroll(option: MultiFactorInfo): Promise<JsAny?>
    public fun unenroll(option: String): Promise<JsAny?>
}

public external interface MultiFactorInfo : JsAny {
    public val displayName: String?
    public val enrollmentTime: String
    public val factorId: String
    public val uid: String
}

public external interface MultiFactorAssertion : JsAny {
    public val factorId: String
}

public external interface MultiFactorSession : JsAny

public external interface MultiFactorResolver : JsAny {
    public val auth: Auth
    public val hints: JsArray<MultiFactorInfo>
    public val session: MultiFactorSession

    public fun resolveSignIn(assertion: MultiFactorAssertion): Promise<AuthResult>
}

public external interface AuthProvider : JsAny

public external interface AuthError : JsAny

public external object EmailAuthProvider : AuthProvider {
    public fun credential(email: String, password: String): AuthCredential
    public fun credentialWithLink(email: String, emailLink: String): AuthCredential
}

public external object FacebookAuthProvider : AuthProvider {
    public fun credential(token: String): AuthCredential
}

public external object GithubAuthProvider : AuthProvider {
    public fun credential(token: String): AuthCredential
}

public external class GoogleAuthProvider : AuthProvider {
    public fun addScope(scope: String)
    public companion object {
        public fun credential(idToken: String?, accessToken: String?): AuthCredential
        public fun credentialFromResult(userCredential: UserCredential): OAuthCredential?
        public fun credentialFromError(error: AuthError): OAuthCredential?
    }
}

public external class OAuthProvider(providerId: String) : AuthProvider {
    public val providerId: String
    public fun credential(optionsOrIdToken: JsAny?, accessToken: String?): AuthCredential

    public fun addScope(scope: String)
    public fun setCustomParameters(customOAuthParameters: JsAny)
}

public external interface OAuthCredentialOptions : JsAny {
    public val accessToken: String?
    public val idToken: String?
    public val rawNonce: String?
}

public external class PhoneAuthProvider(auth: Auth?) : AuthProvider {
    public companion object {
        public fun credential(
            verificationId: String,
            verificationCode: String,
        ): AuthCredential
    }

    public fun verifyPhoneNumber(
        phoneInfoOptions: String,
        applicationVerifier: ApplicationVerifier,
    ): Promise<JsString>
}

public external interface ApplicationVerifier : JsAny {
    public val type: String
    public fun verify(): Promise<JsString>
}

public external object TwitterAuthProvider : AuthProvider {
    public fun credential(token: String, secret: String): AuthCredential
}

public external interface Persistence : JsAny {
    public val type: String
}

public external val browserLocalPersistence: Persistence
public external val browserSessionPersistence: Persistence
public external val indexedDBLocalPersistence: Persistence
public external val inMemoryPersistence: Persistence

public external fun setPersistence(auth: Auth, persistence: Persistence): Promise<JsAny?>
